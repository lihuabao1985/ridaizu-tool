package test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import common.ExcelUtil;
import dao.ExecDao;

public class GetUpdatedDataTest2 {

    public static final String tmp1 = "=";
    public static final String tmp2 = "COUNT";
    public static final String FORMAT_TABLE_FILEPATH = "output\\%s.xlsx";
    public static final List<String> tmpList = Arrays.asList("SELECT", "UPDATE", "INSERT", "DELETE");
    public static Multimap<String, String> valueMultimap = ArrayListMultimap.create();
//    public static LinkedHashMultimap<String, String> valueMultimap = LinkedHashMultimap.create();
    public static Table<Integer, Integer, String> toukeiTable = HashBasedTable.create();

    public static void main(String[] args) throws IOException, SQLException {
        readLogData();

        File output = new File("output");
        if (output.exists() && output.isDirectory() && output.listFiles().length != 0) {
            File[] listFiles = output.listFiles();
            for (File file : listFiles) {
                file.delete();
            }
        }

        Map<String, Collection<String>> valueMap = valueMultimap.asMap();

        ExecDao execDao = new ExecDao();

        int rowSize = toukeiTable.rowKeySet().size();
        for (int i = 0; i < rowSize; i++) {
            String tableName = toukeiTable.get(i, 0);
//            String optType = toukeiTable.get(i, 1);
//            int optCount = Integer.parseInt(toukeiTable.get(i, 2));

//            System.out.println(String.format("%s\t%s\t%s", tableName, optType, optCount));

            for (Entry<String, Collection<String>> entry : valueMap.entrySet()) {
                String key2 = entry.getKey();

                String tableName2 = getTableName(key2);

                if (!tableName.equals(tableName2)) {
                    continue;
                }

                String sql = null;
                if (key2.contains("UPDATE")) {
                    String zyoken = getZyoken(key2);
                    int charCount = getCharCount(zyoken, "[?]");

                    ArrayList<String> newArrayList = Lists.newArrayList(entry.getValue());
                    for (int j = charCount; j > 0; j--) {
                        zyoken = zyoken.replaceFirst("[?]", String.format("'%s'", newArrayList.get(newArrayList.size() - j)));
                    }

                    sql = String.format("SELECT * FROM %s WHERE %s", tableName2, zyoken);
//                    System.out.println(sql);
                } else if (key2.contains("INSERT")) {
                    List<String> primaryKeyList = execDao.getPrimaryKeyList(tableName2);
                    String zyoken = getPrimaryKeyList(primaryKeyList);
                    int charCount = getCharCount(zyoken, "[?]");

                    ArrayList<String> newArrayList = Lists.newArrayList(entry.getValue());
                    for (int j = 0; j < charCount; j++) {
                        zyoken = zyoken.replaceFirst("[?]", String.format("'%s'", newArrayList.get(j)));
                    }

                    sql = String.format("SELECT * FROM %s WHERE %s", tableName2, zyoken);
//                    System.out.println(sql);
                }

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
        }
    }

    private static void readLogData() throws IOException {
        Path path = Paths.get("console.log");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        boolean bUpdate = false;
        boolean bInsert = false;

        int toukeiRowNo = 0;
        String key = null;

        for (String string : lines) {
            if ((string.contains("UPDATE") && !string.contains("外部SQL") && !string.contains("SELECT"))) {
                System.out.println(string);
                bUpdate = true;
                key = string;

                if (string.contains(tmp2)) {
                    string = string.substring(string.indexOf("INFO   - ")+ "INFO   - ".length());
                    string = clearSpace(string);
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

                if (string.contains(tmp2)) {
                    string = string.substring(string.indexOf("INFO   - ")+ "INFO   - ".length());
                    string = clearSpace(string);
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
//            		System.out.println(string.substring(string.lastIndexOf("[") + 1, string.lastIndexOf("]")));
                    valueMultimap.put(key, string.substring(string.lastIndexOf("[") + 1, string.lastIndexOf("]")));
                }
            }

            if (bInsert) {
                if (!check(string)) {
                    bInsert = false;
                } else {
//            		System.out.println(string.substring(string.lastIndexOf("[") + 1, string.lastIndexOf("]")));
                    valueMultimap.put(key, string.substring(string.lastIndexOf("[") + 1, string.lastIndexOf("]")));
                }
            }
        }

        System.out.println("---------------------------------------------------");

//        Map<String, Collection<String>> valueMap = valueMultimap.asMap();
//        for (Entry<String, Collection<String>> entry : valueMap.entrySet()) {
//            String key2 = entry.getKey();
//
//            String tableName = getTableName(key2);
//            if (key2.contains("UPDATE")) {
//                String zyoken = getZyoken(key2);
//                int charCount = getCharCount(zyoken, "[?]");
//
//                ArrayList<String> newArrayList = Lists.newArrayList(entry.getValue());
//                for (int i = charCount; i > 0; i--) {
//                    zyoken = zyoken.replaceFirst("[?]", String.format("'%s'", newArrayList.get(newArrayList.size() - i)));
//                }
//
//                System.out.println(String.format("SELECT * FROM %s WHERE %s", tableName, zyoken));
//            } else if (key2.contains("INSERT")) {
//                String zyoken = getPrimaryKeyList();
//                int charCount = getCharCount(zyoken, "[?]");
//
//                ArrayList<String> newArrayList = Lists.newArrayList(entry.getValue());
//                for (int i = 0; i < charCount; i++) {
//                    zyoken = zyoken.replaceFirst("[?]", String.format("'%s'", newArrayList.get(i)));
//                }
//
//                System.out.println(String.format("SELECT * FROM %s WHERE %s", tableName, zyoken));
//            }
//        }
//
//        System.out.println("---------------------------------------------------");

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

        if (value.contains(tmp2)) {
            return false;
        }

        return value.contains(tmp1);
    }

    private static String getTableName(String value) {
        value = value.substring(value.indexOf("INFO   - ")+ "INFO   - ".length());
        value = clearSpace(value);
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

    private static String clearSpace(String value) {
        String keyword = "  ";
        while(value.contains(keyword)) {
            value = value.replaceAll(keyword, " ");
        }

        return value;
    }

    /**
     * 通过正则表达式的方式获取字符串中指定字符的个数
     * @param text 指定的字符串
     * @return 指定字符的个数
     */
    private static int getCharCount(String text, String sChar) {
        // 根据指定的字符构建正则
        Pattern pattern = Pattern.compile(sChar);
        // 构建字符串和正则的匹配
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        // 循环依次往下匹配
        while (matcher.find()){ // 如果匹配,则数量+1
            count++;
        }
        return  count;
    }

}
