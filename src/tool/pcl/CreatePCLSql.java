package tool.pcl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Strings;
import com.google.common.collect.Table;

import common.Common;
import common.ExcelUtil;
import config.Config;

public class CreatePCLSql {

    private static final String OK = "OK";
    private static final String NG = "NG";

    private static final String ARI = "有";
    private static final String NASI = "無";
    private static final String NASI_MSG = "当該SQL文が実行されない";

    // ファイル出力フォルダー
    public static final String OUTPUT_FILEPATH = Config.getString("OUTPUT_FILEPATH", "output");
    // SQL文置き場所
    private static final String SQL_FILE_BASE_FOLDER_FILEPATH = Config.getString("SQL_FILE_BASE_FOLDER_FILEPATH", "template\\sql");
    // COBOL解析結果置き場所
    private static final String COBOL_ANALYSIS_RESULT_BASE_FOLDER_FILEPATH = Config.getString("COBOL_ANALYSIS_RESULT_BASE_FOLDER_FILEPATH", "template\\COBOL解析結果");
    // COBOL解析結果ファイル名フォーマット
    private static final String COBOL_ANALYSIS_RESULT_FILENAME_FORMAT = Config.getString("COBOL_ANALYSIS_RESULT_FILENAME_FORMAT");
    // ファイル出力フォルダー
    private static final String TEMPLATE_FILEPATH = Config.getString("TEMPLATE_FILEPATH", "template");

    private static final String FILE_TEMPLATE = TEMPLATE_FILEPATH + File.separator + "sql_template.xlsm";

    public static void main(String... strings) throws IOException {
        System.out.println("Start CreatePCLSql.");
        String pgmId = Config.getString("PGM_ID");
        String pgmName = Config.getString("PGM_NAME");
        Workbook templateWorkbook = ExcelUtil.getWorkbook(FILE_TEMPLATE);

        // 追加観点_SQL実行確認作成
        List<String> sqlList = getSqlList(pgmId, pgmName);

        setSqlListInfo(templateWorkbook, pgmId, pgmName, sqlList);
        System.out.println("End CreatePCLSql.");
    }

    private static void setSqlListInfo(Workbook templateWorkbook, String pgmId, String pgmName, List<String> sqlList) throws IOException {
        System.out.println("Start setSqlListInfo.");

        // 追加観点_SQL実行確認作成
        if (sqlList != null && !sqlList.isEmpty()) {
            Sheet tmpSheet = templateWorkbook.getSheet("追加観点_SQL実行確認");
            // 項番	SQLファイル		実行有無	確認結果	備考

            Map<String, String> sqlMap2 = getSqlMap(pgmId);
            int startRowNo = 5;
            int no = 0;

            for (Entry<String, String> entry : sqlMap2.entrySet()) {
                // 項番
                no++;

                if (no != sqlMap2.size()) {
                    ExcelUtil.copyRow(templateWorkbook, "追加観点_SQL実行確認", startRowNo, templateWorkbook, "追加観点_SQL実行確認", startRowNo + no, true, true);
                }

                // SQL文ファイル名
                String filename = entry.getKey();
                // スクリプト
                String sql = entry.getValue();
                String[] strArray = sql.split(" ");

                // テーブル対象
                List<String> tableList = new ArrayList<String>();
                for (String str : strArray) {
                    // PS, PT, PV
                    str = str.replaceAll("\"", "");
                    if (str.startsWith("PS") || str.startsWith("PT") || str.startsWith("PV")) {
                        tableList.add(str);
                    }
                }

                List<String> colValueList = new ArrayList<String>();

                String sql2 = getSql(sqlList, sql);

                // 項番	SQLファイル		実行有無	確認結果	備考
                if (Strings.isNullOrEmpty(sql2)) {
                    colValueList.add(String.valueOf(no));
                    colValueList.add(filename);
                    colValueList.add(NASI);
                    colValueList.add(null);
                    colValueList.add(NASI_MSG);
                } else {
                    colValueList.add(String.valueOf(no));
                    colValueList.add(filename);
                    colValueList.add(ARI);
                    colValueList.add(null);
                    colValueList.add(null);
                }

                // 項番	SQLファイル		実行有無	確認結果	備考
                List<Integer> colIndexList = Arrays.asList(0, 1, 3, 4, 5);
                Row row = tmpSheet.getRow(startRowNo + no - 1);
                if (row == null) {
                    row = tmpSheet.createRow(startRowNo + no - 1);
                }

                ExcelUtil.setRowValue(row, colIndexList, colValueList);
            }


            ExcelUtil.setValidationData(tmpSheet, startRowNo, startRowNo + sqlMap2.size() - 1, 3, 3, new String[] {ARI, NASI});
            ExcelUtil.setValidationData(tmpSheet, startRowNo, startRowNo + sqlMap2.size() - 1, 4, 4, new String[] {OK, NG});
        }

        // エクセル起動する時、公式を実行するように
        ExcelUtil.setForceFormulaRecalculation(templateWorkbook);

        String filePath = String.format(OUTPUT_FILEPATH + File.separator + "%s_%s_SQL.xlsm", pgmId, pgmName);
        ExcelUtil.save(filePath, templateWorkbook);
        templateWorkbook.close();

        System.out.println("End setSqlListInfo.");
    }

    private static String getSql(List<String> sqlList, String sql) {
        for (String string : sqlList) {
            String[] strArray = string.split("####");
            // スクリプト
            if (sql.equals(strArray[0])) {
                return string;
            }
        }

        return null;
    }

    private static List<String> getSqlList(String pgmId, String pgmName) {
        String filepath = String.format(COBOL_ANALYSIS_RESULT_BASE_FOLDER_FILEPATH + File.separator + COBOL_ANALYSIS_RESULT_FILENAME_FORMAT, pgmId, pgmName);
        System.out.println("Start read " + filepath);

        Table<Integer, Integer, String> table = null;
        try {
            table = ExcelUtil.getTableBySXSSF(filepath, "呼出階層");
            if (table == null || table.rowKeySet().size() == 0) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        // SELECT
        String selectKeyword = "検索SQL";
        // INSERT
        String insertKeyword = "登録SQL";
        // UPDATE
        String updateKeyword = "更新SQL";
        // DELETE
        String deleteKeyword = "削除SQL";
        // FETCH
        String fetchKeyword = "カーソル定義";

        int rowSize = table.rowKeySet().size();

        boolean bRead = false;
        boolean isSearch = false;
        boolean searchOne = false;
        boolean isCursor = false;
        boolean cursorOne = false;
        List<String> list = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        for (int rowNo = 0; rowNo < rowSize; rowNo++) {

            StringBuffer cellValue = new StringBuffer();

            Map<Integer, String> row = table.row(rowNo);
            for (Entry<Integer, String> entry : row.entrySet()) {
                if (entry.getKey() < 5) {
                    continue;
                }

                String value = entry.getValue();

                if (Strings.isNullOrEmpty(value)) {
                    continue;
                }

                cellValue.append(value);
            }

            if (cellValue.toString().contains(selectKeyword)) {
                // 検索
                bRead = true;
                isSearch = true;
                isCursor = false;

            } else if (cellValue.toString().contains(insertKeyword)) {
                // 登録
                bRead = true;
                isSearch = false;
                isCursor = false;

            } else if (cellValue.toString().contains(updateKeyword)) {
                // 更新
                bRead = true;
                isSearch = false;
                isCursor = false;

            } else if (cellValue.toString().contains(deleteKeyword)) {
                // 削除
                bRead = true;
                isSearch = false;
                isCursor = false;

            } else if (cellValue.toString().contains(fetchKeyword)) {
                // カーソル定義
                bRead = true;
                isSearch = false;
                isCursor = true;
            }


            if (bRead) {
                if (isSearch) {

                    if (cellValue.toString().contains("-------")) {
                        if (searchOne) {
                            bRead = false;
                            searchOne = false;

                            sb.append("####SELECT");
                            list.add(Common.changeSql(sb.toString()).replaceAll("  ", " ").replaceAll("  ", " ").replaceAll("  ", " ").replace(" )", ")").replace("( ", "(").replace("WHERE(", "WHERE ("));
                            sb = new StringBuffer();
                        } else {
                            searchOne = true;
                        }
                    } else {
                        if (!(cellValue.toString().contains(selectKeyword) || cellValue.toString().contains(insertKeyword) || cellValue.toString().contains(updateKeyword) || cellValue.toString().contains(deleteKeyword) || cellValue.toString().contains(fetchKeyword))) {
                            sb.append(cellValue);
                        }
                    }

                } else if (isCursor) {

                    if (cellValue.toString().contains("-------")) {
                        if (cursorOne) {
                            bRead = false;
                            cursorOne = false;

                            sb.append("####FETCH");
                            list.add(Common.changeSql(sb.toString()).replaceAll("  ", " ").replaceAll("  ", " ").replaceAll("  ", " ").replace(" )", ")").replace("( ", "(").replace("WHERE(", "WHERE ("));
                            sb = new StringBuffer();
                        } else {
                            cursorOne = true;
                        }
                    } else {
                        if (!(cellValue.toString().contains(selectKeyword) || cellValue.toString().contains(insertKeyword) || cellValue.toString().contains(updateKeyword) || cellValue.toString().contains(deleteKeyword) || cellValue.toString().contains(fetchKeyword))) {
                            sb.append(cellValue);
                        }
                    }

                } else {
                    if (cellValue.toString().contains("-------")) {
                        bRead = false;

                        if (sb.toString().contains("INSERT")) {
                            sb.append("####INSERT");
                        } else if (sb.toString().contains("UPDATE")) {
                            sb.append("####UPDATE");
                        } else if (sb.toString().contains("DELETE")) {
                            sb.append("####DELETE");
                        }

                        list.add(Common.changeSql(sb.toString()).replaceAll("  ", " ").replaceAll("  ", " ").replaceAll("  ", " ").replace(" )", ")").replace("( ", "(").replace("WHERE(", "WHERE ("));
                        sb = new StringBuffer();
                    } else {
                        if (!(cellValue.toString().contains(selectKeyword) || cellValue.toString().contains(insertKeyword) || cellValue.toString().contains(updateKeyword) || cellValue.toString().contains(deleteKeyword) || cellValue.toString().contains(fetchKeyword))) {
                            sb.append(cellValue);
                        }
                    }
                }

            }

        }

        System.out.println("End read " + filepath);
        return list;
    }

    private static Map<String, String> getSqlMap(String pgmId) throws IOException {

        Map<String, String> sqlMap = new LinkedHashMap<String, String>();

        File file = new File(SQL_FILE_BASE_FOLDER_FILEPATH + File.separator + pgmId);
        if (!file.isDirectory()) {
            return null;
        }

        File[] listFiles = file.listFiles();
        for (File file2 : listFiles) {
            String fileName = file2.getName();
            if (!fileName.contains(".sql")) {
                continue;
            }

            Path path = Paths.get(file2.getAbsolutePath());
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            StringBuffer sb = new StringBuffer();
            for (String line : lines) {

                if (Strings.isNullOrEmpty(line.trim())) {
                    continue;
                }

                sb.append(line.trim() + " ");
            }

            String sql = sb.toString().replaceAll(" ,", ", ").replaceAll("  ", " ").replaceAll("  ", " ").replaceAll("  ", " ").replace(" )", ")").replace("( ", "(").replace("WHERE(", "WHERE (").trim();
            sqlMap.put(fileName,  sql);
        }

        return sqlMap;
    }

}
