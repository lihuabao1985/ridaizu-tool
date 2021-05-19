package tool.backup.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import common.Common;
import common.ExcelUtil;
import dao.ExecDao;

public class GetUpdatedDataByLog {


    public static final String KEYWORD_FETCH = "CURRENT OF";
    public static final String KEYWORD_INFO_MAIN = "INFO 	[main]	";
    public static final String KEYWORD_INFO_ = "INFO   - ";
    public static final String KEYWORD_FOR_UPDATE = "FOR UPDATE";
    public static final String KEYWORD_ORDER_BY = "ORDER BY";
    public static final String KEYWORD_WHERE = "WHERE";

    public static final String KEYWORD_DENGYU = "=";
    public static final String KEYWORD_COUNT = "COUNT";
    public static final String tmp3 = "前回と同じSQL実行";
    public static final String FORMAT_TABLE_FILEPATH = "output\\%s.xlsx";
    public static final List<String> tmpList = Arrays.asList("SELECT", "UPDATE", "INSERT", "DELETE");
    public static Multimap<String, String> valueMultimap = ArrayListMultimap.create();
    public static Map<String, List<String>> valueMap = new LinkedHashMap<String, List<String>>();
    public static Table<Integer, Integer, String> toukeiTable = HashBasedTable.create();
    public static String logFilePath = null;

    public static void main(String[] args) throws IOException, SQLException {
        System.out.println("処理開始。");

        Scanner sc = new Scanner(System.in);
        System.out.println("ログファイルパスを入力してください。");
        logFilePath = sc.nextLine();
        sc.close();

        if (Strings.isNullOrEmpty(logFilePath)) {
            System.out.println("処理終了。");
            System.exit(0);
        }

        File logFile = new File(logFilePath);
        if (!logFile.exists()) {
            System.out.println("入力されたパスは存在しません。");
            System.out.println("処理終了。");
            System.exit(0);
        }


        Path path = Paths.get(logFilePath);
        readLogData(path);

        File output = new File("output");

        if (!output.exists()) {
            output.mkdirs();
        }

        if (output.listFiles().length != 0) {
            File[] listFiles = output.listFiles();
            for (File file : listFiles) {
                file.delete();
            }
        }

        ExecDao execDao = new ExecDao();


        System.out.println("---------------------------------------------------");

        List<String> sqlList = new ArrayList<String>();
        int rowSize = toukeiTable.rowKeySet().size();
        for (int i = 0; i < rowSize; i++) {
            String tableName = toukeiTable.get(i, 0);
//            String optType = toukeiTable.get(i, 1);
            int optCount = Integer.parseInt(toukeiTable.get(i, 2).trim());

            if (optCount == 0) {
                continue;
            }

//            System.out.println(String.format("%s\t%s\t%s", tableName, optType, optCount));

            boolean isFetch = true;

            for (Entry<String, List<String>> entry : valueMap.entrySet()) {
                String key2 = entry.getKey();

                String tableName2 = getTableName(key2);

                if (!tableName.equals(tableName2)) {
                    continue;
                }

                isFetch = false;

                String sql = null;
                if (key2.contains("UPDATE")) {
                    String zyoken = getZyoken(key2);
                    int charCount = Common.getCharCount(zyoken, "[?]");

                    ArrayList<String> newArrayList = Lists.newArrayList(entry.getValue());
                    for (int j = charCount; j > 0; j--) {
                        zyoken = zyoken.replaceFirst("[?]", String.format("'%s'", newArrayList.get(newArrayList.size() - j)));
                    }

                    sql = String.format("SELECT * FROM %s WHERE %s", tableName2, zyoken);
//                    System.out.println(sql);
                } else if (key2.contains("INSERT")) {
                    List<String> primaryKeyList = execDao.getPrimaryKeyList(tableName2);
                    String zyoken = getPrimaryKeyList(primaryKeyList);
                    int charCount = Common.getCharCount(zyoken, "[?]");

                    ArrayList<String> newArrayList = Lists.newArrayList(entry.getValue());
                    for (int j = 0; j < charCount; j++) {
                        zyoken = zyoken.replaceFirst("[?]", String.format("'%s'", newArrayList.get(j)));
                    }

                    sql = String.format("SELECT * FROM %s WHERE %s", tableName2, zyoken);
//                    System.out.println(sql);
                }

                if (sqlList.contains(sql)) {
                    continue;
                }

                sqlList.add(sql);
                System.out.println(sql);

                List<String> columnNameList = execDao.getColumnNameList(tableName2);
                List<List<String>> dataList = execDao.getDataList(sql, columnNameList);

                Workbook workbook = null;

                File tableFile = new File(String.format(FORMAT_TABLE_FILEPATH, tableName2));
                if (tableFile.exists()) {
                    workbook = ExcelUtil.getWorkbook(String.format(FORMAT_TABLE_FILEPATH, tableName2));
                    Sheet sheet = workbook.getSheetAt(0);
                    int startRowNum = sheet.getLastRowNum() + 1;

                    for (int j = 0; j < dataList.size(); j++) {
                        ExcelUtil.createRow(sheet, startRowNum + j, dataList.get(j));
                    }
                } else {
                    workbook = ExcelUtil.getWorkbook();
                    Sheet sheet = workbook.createSheet();
                    ExcelUtil.createRow(sheet, 0, columnNameList);

                    for (int j = 0; j < dataList.size(); j++) {
                        ExcelUtil.createRow(sheet, j + 1, dataList.get(j));
                    }
                }

                ExcelUtil.save(String.format("output\\%s.xlsx", tableName2), workbook);
            }

            if(isFetch) {
                List<String> valueList = new ArrayList<String>();
                fetch(tableName, valueList);

                if (valueList.isEmpty()) {
                    continue;
                }

                String updateZyouken = valueList.get(2);
                int columnValueSize = Integer.parseInt(valueList.get(3));
                Map<String, String> zyoukenValueByUpdateMap = getZyoukenValueByUpdate(path, valueList.get(0), columnValueSize);
//                List<String> zyoukenValueList = getZyoukenValueByFetch(path, valueList.get(1));

                List<String> keyList = Lists.newArrayList(zyoukenValueByUpdateMap.keySet().iterator());
                String keyword = String.format("[%s]=", keyList.get(0));

                List<Map<String, String>> zyoukenValueMapList = getZyoukenValueMapListByUpdate(path, keyword, columnValueSize);


                List<String> zyoukenColumnNameList = getColumnNameList(updateZyouken);

                List<List<String>> tmpZyoukenValueList = new ArrayList<List<String>>();

                for (Map<String, String> tmpZyoukenValueByUpdateMap : zyoukenValueMapList) {
                    List<String> tmpZyoukenValueList1 = new ArrayList<String>();

                    for (int j = 0; j < zyoukenColumnNameList.size(); j++) {
                        String columnName = zyoukenColumnNameList.get(j);
                        String value = tmpZyoukenValueByUpdateMap.get(columnName);
                        tmpZyoukenValueList1.add(value);
                    }

                    tmpZyoukenValueList.add(tmpZyoukenValueList1);
                }


                List<List<String>> keyValueConditionsList = new ArrayList<List<String>>();

                for (List<String> list : tmpZyoukenValueList) {

                    List<String> tmpKeyValueConditionsList = new ArrayList<String>();
                    for (int j = 0; j < zyoukenColumnNameList.size(); j++) {
                        tmpKeyValueConditionsList.add(String.format("\"%s\"='%s'", zyoukenColumnNameList.get(j), list.get(j)));
                    }

                    keyValueConditionsList.add(tmpKeyValueConditionsList);
                }

                for (List<String> list : keyValueConditionsList) {
                    String sql = String.format("SELECT * FROM %s WHERE %s", tableName, Joiner.on(" AND ").join(list));

                    if (sqlList.contains(sql)) {
                        continue;
                    }

                    sqlList.add(sql);
                    System.out.println(sql);

                    List<String> columnNameList = execDao.getColumnNameList(tableName);
                    List<List<String>> dataList = execDao.getDataList(sql, columnNameList);

                    Workbook workbook = null;

                    File tableFile = new File(String.format(FORMAT_TABLE_FILEPATH, tableName));
                    if (tableFile.exists()) {
                        workbook = ExcelUtil.getWorkbook(String.format(FORMAT_TABLE_FILEPATH, tableName));
                        Sheet sheet = workbook.getSheetAt(0);
                        int startRowNum = sheet.getLastRowNum() + 1;

                        for (int j = 0; j < dataList.size(); j++) {
                            ExcelUtil.createRow(sheet, startRowNum + j, dataList.get(j));
                        }
                    } else {
                        workbook = ExcelUtil.getWorkbook();
                        Sheet sheet = workbook.createSheet();
                        ExcelUtil.createRow(sheet, 0, columnNameList);

                        for (int j = 0; j < dataList.size(); j++) {
                            ExcelUtil.createRow(sheet, j + 1, dataList.get(j));
                        }
                    }

                    ExcelUtil.save(String.format("output\\%s.xlsx", tableName), workbook);
                }
            }
        }

        System.out.println("---------------------------------------------------");
        System.out.println("ファイルはフォルダ「output」に保存されました。");

        System.out.println("処理終了。");
    }

    private static List<String> getColumnNameList(String str) {
        List<String> list = new ArrayList<String>();

        String splitChar = "\"";

        while(str.contains(splitChar)) {
            int startIndex = str.indexOf("\"") + 1;
            str = str.substring(startIndex);
            int endIndex = str.indexOf("\"");
            list.add(str.substring(0, endIndex));
            str = str.replaceFirst(splitChar, "");
        }

        return list;
    }

    private static void fetch(String tableName, List<String> valueList) throws IOException {
        File sqlFile = new File("sql");
        if (!sqlFile.exists()) {
            return ;
        }

        File[] listFiles = sqlFile.listFiles();
        String fetchZyouken = getFetchZyouken(listFiles, tableName);

        if (Strings.isNullOrEmpty(fetchZyouken)) {
            return ;
        }

        String[] fetchZyoukenArray = fetchZyouken.split("####");

        String updateZyouken = getUpdateZyouken(listFiles, fetchZyoukenArray[1]);

        if(Strings.isNullOrEmpty(updateZyouken)) {
            return ;
        }

        String[] updateZyoukenArray = updateZyouken.split("####");



        valueList.add(fetchZyoukenArray[0]);
        valueList.add(updateZyoukenArray[0]);
        valueList.add(updateZyoukenArray[1]);

        int charCount = Common.getCharCount(getSql(String.format("sql\\%s", fetchZyoukenArray[0])), "[?]");
        valueList.add(String.valueOf(charCount));
        ;
    }



    private static Map<String, String> getZyoukenValueByUpdate(Path path, String sqlFilepath, int valueSize) throws IOException {
        Map<String, String> valueMap = new LinkedHashMap<String, String>();

        List<String> lines = Common.readAllLines(path);
        boolean bUpdate = false;

        for (String string : lines) {
            if (string.contains(sqlFilepath)) {
                System.out.println(string);
                bUpdate = true;
                continue;
            }

            if (bUpdate) {
                if (!checkByFetch(string)) {
                    bUpdate = false;
                } else {
                    if (string.contains(tmp3)) {
                        continue;
                    }
                    if (string.contains("SELECT") && string.contains("WHERE")) {
                        continue;
                    }
                    if (string.contains("UPDATE")) {
                        continue;
                    }

                    String[] split = string.split(KEYWORD_DENGYU);
                    String key = split[0];
                    String value = split[1];

                    if (valueMap.size() < valueSize) {
                        valueMap.put(key.substring(key.lastIndexOf("[") + 1, key.lastIndexOf("]")),
                                value.substring(value.lastIndexOf("[") + 1, value.lastIndexOf("]")));
                    }
                }
            }

        }

        return valueMap;
    }

    private static List<Map<String, String>> getZyoukenValueMapListByUpdate(Path path, String keyword, int valueSize) throws IOException {
        List<Map<String, String>> valueMapList = new ArrayList<Map<String,String>>();

        List<String> lines = Common.readAllLines(path);
        boolean bUpdate = false;
        Map<String, String> valueMap = new LinkedHashMap<String, String>();

        for (String string : lines) {
            if (bUpdate || string.contains(keyword)) {
                bUpdate = true;

                if (!checkByFetch(string)) {
                    bUpdate = false;
                    valueMapList.add(valueMap);
                    valueMap = new LinkedHashMap<String, String>();
                } else {
                    if (string.contains(tmp3)) {
                        continue;
                    }
                    if (string.contains("SELECT") && string.contains("WHERE")) {
                        continue;
                    }
                    if (string.contains("UPDATE")) {
                        continue;
                    }

                    String[] split = string.split(KEYWORD_DENGYU);
                    String key = split[0];
                    String value = split[1];
                    if (valueMap.size() < valueSize) {
                        valueMap.put(key.substring(key.lastIndexOf("[") + 1, key.lastIndexOf("]")),
                                value.substring(value.lastIndexOf("[") + 1, value.lastIndexOf("]")));
                    }
                }
            }

        }

        return valueMapList;
    }


    private static String getFetchZyouken(File[] listFiles, String tableName) throws IOException {
        String fetchZyouken = null;
        String sqlFilepath = null;

        for (File file : listFiles) {
            String sql = getSql(file.getAbsolutePath());
            if (sql.contains(tableName) && sql.contains(KEYWORD_FETCH)) {
                fetchZyouken = sql.substring(sql.lastIndexOf(KEYWORD_FETCH) + KEYWORD_FETCH.length()).replaceAll("-", "_").trim();
                sqlFilepath = file.getName();

                break;
            }
        }

        if (sqlFilepath == null) {
            return null;
        }

        return sqlFilepath + "####" + fetchZyouken;
    }

    private static String getUpdateZyouken(File[] listFiles, String fetchZyouken) throws IOException {
        String updateZyouken = null;
        String sqlFilepath = null;

        for (File file : listFiles) {
            if (file.getAbsolutePath().contains(fetchZyouken)) {

                String sql = getSql(file.getAbsolutePath());
                sql = sql.replaceAll(KEYWORD_FOR_UPDATE, "");

                if (sql.contains(KEYWORD_ORDER_BY)) {
                    updateZyouken = sql.substring(sql.lastIndexOf(KEYWORD_WHERE) + KEYWORD_WHERE.length(), sql.lastIndexOf(KEYWORD_ORDER_BY));
                } else {
                    updateZyouken = sql.substring(sql.lastIndexOf(KEYWORD_WHERE) + KEYWORD_WHERE.length());
                }

                sqlFilepath = file.getName();

                break;
            }
        }

        if (sqlFilepath == null) {
            return null;
        }

        return sqlFilepath + "####" + updateZyouken;
    }

    private static String getSql(String filepath) throws IOException {
        Path path = Paths.get(filepath);
        List<String> lines = Files.readAllLines(path);
        StringBuffer sqlSb = new StringBuffer();
        for (String line : lines) {
            sqlSb.append(line).append(" ");
        }

        return sqlSb.toString();
    }

    private static void readLogData(Path path) throws IOException {
        List<String> lines = Common.readAllLines(path);

        boolean bUpdate = false;
        boolean bInsert = false;

        int toukeiRowNo = 0;
        String key = null;
        int currentSqlWenhaoCount = 0;

        for (String string : lines) {
            if ((string.contains("UPDATE") && !string.contains("外部SQL") && !string.contains("SELECT"))) {
                System.out.println(string);
                bUpdate = true;
                key = string;
                currentSqlWenhaoCount = Common.getCharCount(key, "[?]");

                if (string.contains(KEYWORD_COUNT)) {
//                    string = string.substring(string.indexOf("INFO   - ")+ "INFO   - ".length());

                    if (string.contains(KEYWORD_INFO_)) {
                        string = string.substring(string.indexOf(KEYWORD_INFO_)+ KEYWORD_INFO_.length());
                    } else if (string.contains(KEYWORD_INFO_MAIN)) {
                        string = string.substring(string.indexOf(KEYWORD_INFO_MAIN)+ KEYWORD_INFO_MAIN.length());
                    }

                    string = Common.clearSpace(string);
                    String[] valueArray = string.split(" ");
                    if (!"0".equals(valueArray[4])) {
                        toukeiTable.put(toukeiRowNo, 0, valueArray[0]);
                        toukeiTable.put(toukeiRowNo, 1, valueArray[1]);
                        toukeiTable.put(toukeiRowNo, 2, valueArray[4]);
                        toukeiRowNo++;
                    }
                }

                continue;
            }

            if (string.contains("INSERT") && !string.contains("外部SQL")) {
                System.out.println(string);
                bInsert = true;
                key = string;
                currentSqlWenhaoCount = Common.getCharCount(key, "[?]");


                if (string.contains(KEYWORD_COUNT)) {
//                    string = string.substring(string.indexOf("INFO   - ")+ "INFO   - ".length());

                    if (string.contains(KEYWORD_INFO_)) {
                        string = string.substring(string.indexOf(KEYWORD_INFO_)+ KEYWORD_INFO_.length());
                    } else if (string.contains(KEYWORD_INFO_MAIN)) {
                        string = string.substring(string.indexOf(KEYWORD_INFO_MAIN)+ KEYWORD_INFO_MAIN.length());
                    }

                    string = Common.clearSpace(string);
                    String[] valueArray = string.split(" ");
                    if (!"0".equals(valueArray[4])) {
                        toukeiTable.put(toukeiRowNo, 0, valueArray[0]);
                        toukeiTable.put(toukeiRowNo, 1, valueArray[1]);
                        toukeiTable.put(toukeiRowNo, 2, valueArray[4]);
                        toukeiRowNo++;
                    }
                }

                continue;
            }

            if (bUpdate) {
                if (!check(string)) {
                    bUpdate = false;
                } else {
                    if (string.contains(tmp3)) {
                        continue;
                    }

//            		System.out.println(string.substring(string.lastIndexOf("[") + 1, string.lastIndexOf("]")));
                    if (!valueMap.containsKey(key)) {
                        valueMap.put(key, new ArrayList<String> ());
                    }

                    if (valueMap.get(key).size() < currentSqlWenhaoCount) {
                        valueMap.get(key).add(string.substring(string.lastIndexOf("[") + 1, string.lastIndexOf("]")));
                    }
                }
            }

            if (bInsert) {
                if (!check(string)) {
                    bInsert = false;
                } else {
                    if (string.contains(tmp3)) {
                        continue;
                    }

//            		System.out.println(string.substring(string.lastIndexOf("[") + 1, string.lastIndexOf("]")));
                    if (!valueMap.containsKey(key)) {
                        valueMap.put(key, new ArrayList<String> ());
                    }

                    if (valueMap.get(key).size() < currentSqlWenhaoCount) {
                        valueMap.get(key).add(string.substring(string.lastIndexOf("[") + 1, string.lastIndexOf("]")));
                    }
                }
            }

        }
    }

    private static String getPrimaryKeyList(List<String> primaryKeyList) {
        String keyValueConditionsFormat = "%s=?";

        List<String> keyValueConditionsList = new ArrayList<String>();

        for (String key : primaryKeyList) {
            keyValueConditionsList.add(String.format(keyValueConditionsFormat, key));
        }

        return Joiner.on(" AND ").join(keyValueConditionsList);
    }

    public static boolean check(String value) {
        for (String string : tmpList) {
            if (value.contains(string)) {
                return false;
            }
        }

        if (value.contains(KEYWORD_COUNT)) {
            return false;
        }

        if (value.contains(tmp3)) {
            return true;
        }

        return value.contains(KEYWORD_DENGYU);
    }

    public static boolean checkByFetch(String value) {

        return value.contains(KEYWORD_DENGYU);
    }

    private static String getTableName(String value) {
//        value = value.substring(value.indexOf("INFO   - ")+ "INFO   - ".length());

        if (value.contains(KEYWORD_INFO_)) {
            value = value.substring(value.indexOf(KEYWORD_INFO_)+ KEYWORD_INFO_.length());
        } else if (value.contains(KEYWORD_INFO_MAIN)) {
            value = value.substring(value.indexOf(KEYWORD_INFO_MAIN)+ KEYWORD_INFO_MAIN.length());
        }

        value = Common.clearSpace(value);
        String[] valueArray = value.split(" ");
        String tableName = null;
        for (String str : valueArray) {
            // PS, PT, PV
            str = str.replaceAll("\"", "");
            if (str.startsWith("PS") || str.startsWith("PT") || str.startsWith("PV")) {
                tableName = str;
                break;
            }
        }

        return tableName;
    }

    private static String getZyoken(String value) {
        return value.substring(value.lastIndexOf("WHERE") + "WHERE".length());
    }




}
