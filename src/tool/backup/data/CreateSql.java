package tool.backup.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Table;

import common.Def;
import common.ExcelUtil;
import config.Config;

public class CreateSql {

    public static final String DEFALUT_FOLDER_PATH = Config.getString("OUTPUT_FILEPATH", "output");

    /**
     * @param args
    * @throws IOException
     * @throws SQLException
     */
    public static void main(String[] args) throws IOException, SQLException {
        System.out.println("処理開始。");

        if (args.length == 0) {
            System.out.println("ファイルまたはフォルダを指定してください。");
            System.exit(0);
        }

        String filePath = args[0];
        System.out.println(String.format("指定filePath: %s", filePath));
        List<String> filePathList = getFilePathList(filePath);

        if (filePathList == null) {
            System.out.println("ファイルまたはフォルダは存在しません。");
            System.exit(0);
        }

        for (String tmpFilePath : filePathList) {
            System.out.println(String.format("filePath: %s", tmpFilePath));

            Workbook workbook = ExcelUtil.getWorkbook(filePath);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                System.out.println("----------------------------------------------------------------------------------------------------");

                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();
                System.out.println(String.format("SheetName: %s", sheetName));

                if (Def.TEMPLATE_SHEET_EXCLUSION_LIST.contains(sheetName)) {
                    System.out.println("----------------------------------------------------------------------------------------------------");
                    continue;
                }

                Table<Integer, Integer, String> table = ExcelUtil.getTable(tmpFilePath, sheetName);
                String tableName = getTableName(table);
                List<String> primaryKeyList = getPrimaryKeyList(table);
                List<String> columnList = getColumnList(table);
                List<List<String>> rowsValueList = getDataList(table, columnList.size());

                if (primaryKeyList.isEmpty() || rowsValueList.isEmpty()) {
                    continue;
                }


                List<String> selectSqlList = getSelectSqlList(tableName, primaryKeyList, columnList, rowsValueList);
                if (selectSqlList != null) {
                    for (String selectSql : selectSqlList) {
                        System.out.println(selectSql);
                    }

                    writeFile(DEFALUT_FOLDER_PATH + File.separator + String.format(Def.FORMAT_FILENAME_SELECT, tableName),
                            selectSqlList);
                }

                List<String> insertSqlList = getInserSqlList(tableName, columnList, rowsValueList);
                for (String insertSql : insertSqlList) {
                    System.out.println(insertSql);
                }
                writeFile(DEFALUT_FOLDER_PATH + File.separator + String.format(Def.FORMAT_FILENAME_INSERT, tableName),
                        insertSqlList);

                List<String> updateSqlList = getUpdateSqlList(tableName, primaryKeyList, columnList, rowsValueList);
                if (updateSqlList != null) {
                    for (String updateSql : updateSqlList) {
                        System.out.println(updateSql);
                    }
                    writeFile(DEFALUT_FOLDER_PATH + File.separator + String.format(Def.FORMAT_FILENAME_UPDATE, tableName),
                                updateSqlList);
                }

                List<String> deteleSqlList = getDeteleSqlList(tableName, primaryKeyList, columnList, rowsValueList);
                if (deteleSqlList != null) {
                    for (String deteleSql : deteleSqlList) {
                        System.out.println(deteleSql);
                    }

                    writeFile(DEFALUT_FOLDER_PATH + File.separator + String.format(Def.FORMAT_FILENAME_DELETE, tableName),
                            deteleSqlList);
                }

                System.out.println("----------------------------------------------------------------------------------------------------");
            }

        }

        System.out.println("処理終了。");
    }

    private static List<String> getFilePathList(String path) {
        List<String> filePathList = new ArrayList<String>();

        File file = new File(path);
        if (file.isFile()) {
            filePathList.add(path);
        } else if (file.isDirectory()) {
            File[] fileArray = file.listFiles();
            for (File tmpFile : fileArray) {
                filePathList.add(tmpFile.getAbsolutePath());
            }
        } else {
            filePathList = null;
        }

        return filePathList;
    }

    private static List<String> getSelectSqlList(String tableName, List<String> primaryKeyList, List<String> columnList,
            List<List<String>> rowsValueList) {
        if (primaryKeyList.isEmpty()) {
            return null;
        }

        List<String> sqlList = new ArrayList<String>();
        String keyValueConditionsFormat = "%s=%s";
        for (List<String> valueList : rowsValueList) {
            padData(columnList, valueList);

            List<String> keyValueConditionsList = new ArrayList<String>();
            for (String primaryKey : primaryKeyList) {
                keyValueConditionsList.add(String.format(keyValueConditionsFormat, primaryKey,
                        valueList.get(columnList.indexOf(primaryKey))));
            }
            sqlList.add(String.format(Def.FORMAT_SELECT_SQL, tableName, Joiner.on(" AND ").join(keyValueConditionsList)));
        }
        return sqlList;
    }

    private static List<String> getInserSqlList(String tableName, List<String> columnList,
            List<List<String>> rowsValueList) {
        List<String> sqlList = new ArrayList<String>();
        for (List<String> valueList : rowsValueList) {
            padData(columnList, valueList);
            sqlList.add(String.format(Def.FORMAT_INSERT_SQL, tableName, Joiner.on(",").join(columnList), Joiner.on(",").join(valueList)));
        }
        return sqlList;
    }

    private static List<String> getUpdateSqlList(String tableName, List<String> primaryKeyList, List<String> columnList,
            List<List<String>> rowsValueList) {
        if (primaryKeyList.isEmpty()) {
            return null;
        }

        List<String> sqlList = new ArrayList<String>();
        String keyValueConditionsFormat = "%s=%s";
        for (List<String> valueList : rowsValueList) {
            padData(columnList, valueList);
            List<String> setKeyValueList1 = new ArrayList<String>();
            for (int i = 0; i < columnList.size(); i++) {
                if (primaryKeyList.contains(columnList.get(i))) {
                    continue;
                }

                setKeyValueList1.add(String.format(keyValueConditionsFormat, columnList.get(i), valueList.get(i)));
            }
            List<String> keyValueConditionsList = new ArrayList<String>();
            for (String primaryKey : primaryKeyList) {
                keyValueConditionsList.add(String.format(keyValueConditionsFormat, primaryKey,
                        valueList.get(columnList.indexOf(primaryKey))));
            }
            sqlList.add(String.format(Def.FORMAT_UPDATE_SQL, tableName, Joiner.on(", ").join(setKeyValueList1),
                    Joiner.on(" AND ").join(keyValueConditionsList)));
        }
        return sqlList;
    }

    private static List<String> getDeteleSqlList(String tableName, List<String> primaryKeyList, List<String> columnList,
            List<List<String>> rowsValueList) {
        if (primaryKeyList.isEmpty()) {
            return null;
        }

        List<String> sqlList = new ArrayList<String>();
        String keyValueConditionsFormat = "%s=%s";
        for (List<String> valueList : rowsValueList) {
            padData(columnList, valueList);

            List<String> keyValueConditionsList = new ArrayList<String>();
            for (String primaryKey : primaryKeyList) {
                keyValueConditionsList.add(String.format(keyValueConditionsFormat, primaryKey,
                        valueList.get(columnList.indexOf(primaryKey))));
            }
            sqlList.add(String.format(Def.FORMAT_DELETE_SQL, tableName, Joiner.on(" AND ").join(keyValueConditionsList)));
        }
        return sqlList;
    }

    private static String getTableName(Table<Integer, Integer, String> table) {
        return table.get(Def.TABLE_NAME_ROW_NO, Def.TABLE_NAME_COLUMN_NO);
    }

    private static List<String> getPrimaryKeyList(Table<Integer, Integer, String> table) {
        List<String> primaryKeyList = new ArrayList<String>();

        int colCount = table.row(Def.PRIMARY_KEY_ROW_NO).size();
        for (int i = 0; i < colCount; i++) {
            if (!Strings.isNullOrEmpty(table.get(Def.PRIMARY_KEY_ROW_NO, i + 1))) {
                primaryKeyList.add(String.format("\"%s\"", table.get(Def.PRIMARY_KEY_ROW_NO, i + 1)));
            }
        }

        return primaryKeyList;
    }

    private static List<String> getColumnList(Table<Integer, Integer, String> table) {
        List<String> columnList = new ArrayList<String>();
        int colCount = table.row(Def.COLUMN_NAME_ROW_NO).size();
        for (int i = 0; i < colCount; i++) {
            columnList.add(String.format("\"%s\"", table.get(Def.COLUMN_NAME_ROW_NO, i)));
//            columnList.add(table.get(COLUMN_NAME_ROW_NO, i));
        }
        return columnList;
    }

    private static List<List<String>> getDataList(Table<Integer, Integer, String> table, int colCount) {
        List<List<String>> rowsValueList = new ArrayList<List<String>>();
        for (int rowNo : table.rowKeySet()) {
            if (rowNo < Def.DATA_START_ROW_NO || Strings.isNullOrEmpty(table.get(rowNo, 0))) {
                continue;
            }
            List<String> rowValueList = new ArrayList<String>();
            for (int i = 0; i < colCount; i++) {
                String value = table.get(rowNo, i);
                if (Strings.isNullOrEmpty(value)) {
                    rowValueList.add(Def.NULL_STRING);
                } else {
                    rowValueList.add(String.format("'%s'", value));
                }
                //    System.out.println(String.format("rowNo[%d], cowNo[%d]->%s", rowNo, i, value));
            }
            rowsValueList.add(rowValueList);
        }
        return rowsValueList;
    }

    private static void padData(List<String> columnList, List<String> valueList) {
        int diffLength = columnList.size() - valueList.size();
        for (int i = 0; i < diffLength; i++) {
            valueList.add(Def.NULL_STRING);
        }
    }

    private static void writeFile(String filePath, List<String> valueList) {
        BufferedWriter bw = null;
        try {

            File folder = new File(DEFALUT_FOLDER_PATH);
            if (!folder.isDirectory()) {
                folder.mkdir();
            }

            // ファイル入出力（MS932からUTF8へ変換）
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF8"));
            for (String value : valueList) {
                // ファイルへ書き込み
                bw.write(value);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ie) {
                }
            }
        }
    }
}
