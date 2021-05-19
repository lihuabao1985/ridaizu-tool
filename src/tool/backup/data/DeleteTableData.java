package tool.backup.data;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Table;

import common.Def;
import common.ExcelUtil;
import dao.ExecDao;

public class DeleteTableData {

    public static final String FORMAT_DELETE_SQL = "DELETE FROM %s WHERE %s";

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
                List<String> searchColumnList = getSearchColumnList(table);
                List<String> searchConditionsList = getSearchConditionsList(table);
                List<String> searchValueList = getSearchValueList(table);

                if (searchColumnList.isEmpty() || searchConditionsList.isEmpty() || searchValueList.isEmpty()) {
                    continue;
                }

                if (!(searchColumnList.size() == searchConditionsList.size() &&
                    searchColumnList.size() == searchValueList.size())) {
                    continue;
                }

                String deleteSql = getDeleteSql(tableName, searchColumnList, searchConditionsList, searchValueList);

                Scanner sc = new Scanner(System.in);
                System.out.println(String.format("テーブル「%s」のデータを削除してもよろしいでしょうか？Y/N\nSQL文：%s", tableName, deleteSql));
                String comfig = sc.nextLine();
                sc.close();
                if (!"y".equals(comfig.toLowerCase())) {
                    System.out.println("処理終了。");
                    System.exit(0);
                }

                System.out.println(String.format("Delete start. SQL: %s", deleteSql));
                int deleteCount = dao.deleteData(deleteSql);
                if (deleteCount < 0) {
                    System.out.println(String.format("Delete error. SQL: %s", deleteSql));
                }
                System.out.println(String.format("Delete end. SQL: %s", deleteSql));
                System.out.println(String.format("Table[%s], delete success count[%d]", tableName, deleteCount));

                System.out.println("----------------------------------------------------------------------------------------------------");
            }
        }

        System.out.println("処理終了。");
    }

    public static String getTableName(Table<Integer, Integer, String> table) {
        return table.get(Def.TABLE_NAME_ROW_NO, Def.TABLE_NAME_COLUMN_NO);
    }

    private static List<String> getSearchColumnList(Table<Integer, Integer, String> table) {
        List<String> columnList = new ArrayList<String>();
        int colCount = table.row(Def.SEARCH_COLUMN_ROW_NO).size();
        for (int i = Def.SEARCH_COLUMN_COLUMN_NO; i < colCount; i++) {
            if (!Strings.isNullOrEmpty(table.get(Def.SEARCH_COLUMN_ROW_NO, i))) {
                columnList.add(String.format("\"%s\"", table.get(Def.SEARCH_COLUMN_ROW_NO, i)));
            }
        }
        return columnList;
    }

    private static List<String> getSearchConditionsList(Table<Integer, Integer, String> table) {
        List<String> columnList = new ArrayList<String>();
        int colCount = table.row(Def.SEARCH_CONDITIONS_ROW_NO).size();
        for (int i = Def.SEARCH_CONDITIONS_COLUMN_NO; i < colCount; i++) {
            if (!Strings.isNullOrEmpty(table.get(Def.SEARCH_CONDITIONS_ROW_NO, i))) {
                columnList.add(table.get(Def.SEARCH_CONDITIONS_ROW_NO, i));
            }
        }
        return columnList;
    }

    private static List<String> getSearchValueList(Table<Integer, Integer, String> table) {
        List<String> columnList = new ArrayList<String>();
        int colCount = table.row(Def.SEARCH_VALUE_ROW_NO).size();
        for (int i = Def.SEARCH_VALUE_COLUMN_NO; i < colCount; i++) {
            if (!Strings.isNullOrEmpty(table.get(Def.SEARCH_VALUE_ROW_NO, i))) {
                columnList.add(table.get(Def.SEARCH_VALUE_ROW_NO, i));
            }
        }
        return columnList;
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

    private static String getDeleteSql(String tableName, List<String> searchColumnList, List<String> searchConditionsList, List<String> searchValueList) {

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

        return String.format(FORMAT_DELETE_SQL, tableName, Joiner.on(" AND ").join(keyValueConditionsList));
    }
}
