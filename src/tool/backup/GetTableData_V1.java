package tool.backup;

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
import config.Config;
import dao.ExecDao;

public class GetTableData_V1 {

    public static final String FORMAT_SELECT_SQL = "SELECT * FROM %s WHERE %s";

    public static final String DEFALUT_FILENAME = Config.getString("GET_TABALE_DATA_OUTPUT_FILENAME", "table.xlsx");

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
                String tableName = getTableName(table);
                List<String> primaryKeyList = dao.getPrimaryKeyList(tableName);
                List<String> columnList = dao.getColumnNameList(tableName);

                List<String> searchColumnList = getSearchColumnList(table);
                List<String> searchConditionsList = getSearchConditionsList(table);
                List<String> searchValueList = getSearchValueList(table);
                String searchFreeConditions = table.get(Def.SEARCH_FREE_CONDITIONS_ROW_NO, Def.SEARCH_FREE_CONDITIONS_COLUMN_NO);

                ExcelUtil.setCellValue(ExcelUtil.createCell(sheet.getRow(Def.PRIMARY_KEY_ROW_NO), Def.PRIMARY_KEY_COLUMN_NO), Joiner.on(",").join(primaryKeyList));
                ExcelUtil.createRow(sheet, Def.COLUMN_NAME_ROW_NO, columnList);


                if (Strings.isNullOrEmpty(searchFreeConditions) && (searchColumnList.isEmpty() || searchConditionsList.isEmpty() || searchValueList.isEmpty())) {
                    continue;
                }

                String selectSql = null;
                if (Strings.isNullOrEmpty(searchFreeConditions)) {
                    if (!(searchColumnList.size() == searchConditionsList.size() &&
                        searchColumnList.size() == searchValueList.size())) {
                        continue;
                    }

                    selectSql = getSelectSql(tableName, searchColumnList, searchConditionsList, searchValueList);
                } else {
                    selectSql = getSelectSqlByFreeConditions(tableName, searchFreeConditions);
                }

                System.out.println(selectSql);
                List<List<String>> dataList = dao.getDataList(selectSql, columnList);
                int startRowNo = Def.DATA_START_ROW_NO;

                for (List<String> list : dataList) {
                    ExcelUtil.createRow(sheet, startRowNo++, list);
                }

                System.out.println("----------------------------------------------------------------------------------------------------");
            }

            ExcelUtil.save(DEFALUT_FILENAME, workbook);
        }

        System.out.println("処理終了。");
    }

    public static String getTableName(Table<Integer, Integer, String> table) {
        return table.get(Def.TABLE_NAME_ROW_NO, Def.TABLE_NAME_COLUMN_NO);
    }

    public static List<String> getSearchColumnList(Table<Integer, Integer, String> table) {
        List<String> columnList = new ArrayList<String>();
        int colCount = table.row(Def.SEARCH_COLUMN_ROW_NO).size();
        for (int i = Def.SEARCH_COLUMN_COLUMN_NO; i < colCount; i++) {
            if (!Strings.isNullOrEmpty(table.get(Def.SEARCH_COLUMN_ROW_NO, i))) {
                columnList.add(String.format("\"%s\"", table.get(Def.SEARCH_COLUMN_ROW_NO, i)));
            }
        }
        return columnList;
    }
    public static List<String> getSearchConditionsList(Table<Integer, Integer, String> table) {
        List<String> columnList = new ArrayList<String>();
        int colCount = table.row(Def.SEARCH_CONDITIONS_ROW_NO).size();
        for (int i = Def.SEARCH_CONDITIONS_COLUMN_NO; i < colCount; i++) {
            if (!Strings.isNullOrEmpty(table.get(Def.SEARCH_CONDITIONS_ROW_NO, i))) {
                columnList.add(table.get(Def.SEARCH_CONDITIONS_ROW_NO, i));
            }
        }
        return columnList;
    }
    public static List<String> getSearchValueList(Table<Integer, Integer, String> table) {
        List<String> columnList = new ArrayList<String>();
        int colCount = table.row(Def.SEARCH_VALUE_ROW_NO).size();
        for (int i = Def.SEARCH_VALUE_COLUMN_NO; i < colCount; i++) {
            if (!Strings.isNullOrEmpty(table.get(Def.SEARCH_VALUE_ROW_NO, i))) {
                columnList.add(table.get(Def.SEARCH_VALUE_ROW_NO, i));
            }
        }
        return columnList;
    }

    public static List<String> getValueList(Table<Integer, Integer, String> table) {
        List<String> valueList = new ArrayList<String>();
        String tmpValue = table.get(Def.SEARCH_VALUE_ROW_NO, Def.SEARCH_VALUE_COLUMN_NO);
        if (Strings.isNullOrEmpty(tmpValue)) {
            return valueList;
        }

        String[] valueArray = tmpValue.split(",");
        for (String value : valueArray) {
            valueList.add(String.format("'%s'", value));
        }
        return valueList;
    }

    public static List<String> getColumnList(Table<Integer, Integer, String> table) {
        List<String> columnList = new ArrayList<String>();
        int colCount = table.row(Def.COLUMN_NAME_ROW_NO).size();
        for (int i = 0; i < colCount; i++) {
//            columnList.add(String.format("\"%s\"", table.get(COLUMN_NAME_ROW_NO, i)));
            columnList.add(table.get(Def.COLUMN_NAME_ROW_NO, i));
        }
        return columnList;
    }

    public static List<String> getFilePathList(String path) {
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

    private static String getSelectSql(String tableName, List<String> searchColumnList, List<String> searchConditionsList, List<String> searchValueList) {

        List<String> keyValueConditionsList = new ArrayList<String>();

        for (int i = 0; i < searchColumnList.size(); i++) {
            String searchColumn = searchColumnList.get(i);
            String searchConditions = searchConditionsList.get(i);
            String searchValue = searchValueList.get(i);

            if ("in".equals(searchConditions.toLowerCase())) {
                keyValueConditionsList.add(String.format("%s %s %s", searchColumn, searchConditions, searchValue));
            } else {
                keyValueConditionsList.add(String.format("%s %s '%s'", searchColumn, searchConditions, searchValue));
            }
        }

        return String.format(FORMAT_SELECT_SQL, tableName, Joiner.on(" AND ").join(keyValueConditionsList));
    }

    private static String getSelectSqlByFreeConditions(String tableName, String freeConditions) {

        return String.format(FORMAT_SELECT_SQL, tableName, freeConditions);
    }
}
