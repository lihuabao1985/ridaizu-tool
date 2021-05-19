package tool.backup.data;

import java.io.File;
import java.io.IOException;
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
import dao.ExecDao;

public class UpdateTableData {

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

        ExecDao dao = new ExecDao();

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
                // テーブル名を取得
                String tableName = getTableName(table);
                // 主キーを取得
                List<String> primaryKeyList = getPrimaryKeyList(table);
                // 削除条件を取得
                String deleteCondition = getDeleteCondition(table);
                // カラム名一覧を取得
                List<String> columnList = getColumnList(table);
                // データ一覧を取得
                List<List<String>> rowsValueList = getDataList(table, columnList.size());

                if (primaryKeyList.isEmpty() || rowsValueList.isEmpty()) {
                    continue;
                }

                int insertSeccueeCount = 0;
                int insertErrorCount = 0;
                int updateSeccueeCount = 0;
                int updateErrorCount = 0;
                int deleteSeccueeCount = 0;

                if (!Strings.isNullOrEmpty(deleteCondition)) {
                    String deleteSql = getDeleteSql(tableName, deleteCondition);
                    System.out.println(String.format("Delete start. SQL: %s", deleteSql));
                    int deleteCount = dao.deleteData(deleteSql);
                    deleteSeccueeCount += deleteCount;
                }

                for (List<String> valueList : rowsValueList) {
                    String selectSql = getSelectSql(tableName, primaryKeyList, columnList, valueList);
                    System.out.println(String.format("SELECT SQL: %s", selectSql));

                    if (dao.isExistData(selectSql)) {
                        // データが存在する場合、更新処理を行う
                        String updateSql = getUpdateSql(tableName, primaryKeyList, columnList, valueList);
                        System.out.println(String.format("Update start. SQL: %s", updateSql));
                        boolean updateReslut = dao.updateData(updateSql);
                        if (!updateReslut) {
                            System.out.println(String.format("Update error. SQL: %s", updateSql));
                            updateErrorCount++;
                        } else {
                            updateSeccueeCount++;
                        }
                        System.out.println(String.format("Update end. SQL: %s", updateSql));
                    } else {
                        // データが存在しない場合、登録処理を行う
                        String inserSql = getInserSql(tableName, columnList, valueList);
                        System.out.println(String.format("Insert start. SQL: %s", inserSql));
                        boolean insertReslut = dao.insertData(inserSql);
                        if (!insertReslut) {
                            System.out.println(String.format("Insert error. SQL: %s", inserSql));
                            insertErrorCount++;
                        } else {
                            insertSeccueeCount++;
                        }
                        System.out.println(String.format("Insert end. SQL: %s", inserSql));
                    }
                }

                System.out.println(String.format("Table[%s], insert success count[%d], error count[%d], update success count[%d], error count[%d], delete success count[%d]",
                        tableName, insertSeccueeCount, insertErrorCount, updateSeccueeCount, updateErrorCount, deleteSeccueeCount));

                System.out.println("----------------------------------------------------------------------------------------------------");
            }

            workbook.close();
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

    private static String getSelectSql(String tableName, List<String> primaryKeyList, List<String> columnList, List<String> valueList) {
        padData(columnList, valueList);

        String keyValueConditionsFormat = "%s=%s";
        List<String> keyValueConditionsList = new ArrayList<String>();
        for (String primaryKey : primaryKeyList) {
            keyValueConditionsList.add(String.format(keyValueConditionsFormat, primaryKey,
                    valueList.get(columnList.indexOf(primaryKey))));
        }

        return String.format(Def.FORMAT_SELECT_SQL, tableName, Joiner.on(" AND ").join(keyValueConditionsList));
    }

    private static String getInserSql(String tableName, List<String> columnList,
            List<String> valueList) {
        padData(columnList, valueList);
        return String.format(Def.FORMAT_INSERT_SQL, tableName, Joiner.on(",").join(columnList), Joiner.on(",").join(valueList));
    }

    private static String getUpdateSql(String tableName, List<String> primaryKeyList, List<String> columnList, List<String> valueList) {

        padData(columnList, valueList);

        String keyValueConditionsFormat = "%s=%s";
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

        return String.format(Def.FORMAT_UPDATE_SQL, tableName, Joiner.on(", ").join(setKeyValueList1), Joiner.on(" AND ").join(keyValueConditionsList));
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

    private static String getDeleteCondition(Table<Integer, Integer, String> table) {
        return table.get(Def.DELETE_FREE_CONDITIONS_ROW_NO, Def.DELETE_FREE_CONDITIONS_COLUMN_NO);
    }

    private static String getDeleteSql(String tableName, String condition) {

        return String.format(Def.FORMAT_DELETE_SQL, tableName, condition);
    }

    private static List<String> getColumnList(Table<Integer, Integer, String> table) {
        List<String> columnList = new ArrayList<String>();
        int colCount = table.row(Def.COLUMN_NAME_ROW_NO).size();
        for (int i = 0; i < colCount; i++) {
            if (!Strings.isNullOrEmpty(table.get(Def.COLUMN_NAME_ROW_NO, i))) {
                columnList.add(String.format("\"%s\"", table.get(Def.COLUMN_NAME_ROW_NO, i)));
            }
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
//                System.out.println(String.format("rowNo[%d], cowNo[%d]->%s", rowNo, i, value));
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

}
