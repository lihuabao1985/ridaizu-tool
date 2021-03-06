package tool.backup.data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import common.Common;
import common.Def;
import common.ExcelUtil;
import config.Config;
import dao.ExecDao;

public class GetTableDataByPgmId {

    public static final String FORMAT_SELECT_SQL = "SELECT * FROM %s WHERE %s";
    public static final int TABLE_NAME_ROW_NO = 0;
    public static final int TABLE_NAME_COLUMN_NO = 1;
    public static final int PRIMARY_KEY_ROW_NO = 1;
    public static final int PRIMARY_KEY_COLUMN_NO = 1;
    public static final int SEARCH_COLUMN_ROW_NO = 2;
    public static final int SEARCH_COLUMN_COLUMN_NO = 1;
    public static final int SEARCH_CONDITIONS_ROW_NO = 3;
    public static final int SEARCH_CONDITIONS_COLUMN_NO = 1;
    public static final int SEARCH_VALUE_ROW_NO = 4;
    public static final int SEARCH_VALUE_COLUMN_NO = 1;
    public static final int COLUMN_NAME_ROW_NO = 6;
    public static final int DATA_START_ROW_NO = 7;

    public static final String DEFALUT_FILENAME = Config.getString("GET_TABALE_DATA_OUTPUT_FILENAME", "table.xlsx");
    // ?????????????????????????????????
    public static final String TEMPLATE_FILEPATH = Config.getString("TEMPLATE_FILEPATH", "template");
    static final String FILE_P_SUB_ACCESS_DB = TEMPLATE_FILEPATH + File.separator + "P_???SUB_ACCSESS_DB.xlsx";
    // SQL???????????????
    public static final String SQL_FILE_BASE_FOLDER_FILEPATH = Config.getString("SQL_FILE_BASE_FOLDER_FILEPATH", "template\\sql");
    // COBOL????????????????????????
    public static final String COBOL_ANALYSIS_RESULT_BASE_FOLDER_FILEPATH = Config.getString("COBOL_ANALYSIS_RESULT_BASE_FOLDER_FILEPATH", "template\\COBOL????????????");
    // COBOL?????????????????????????????????????????????
    public static final String COBOL_ANALYSIS_RESULT_FILENAME_FORMAT = Config.getString("COBOL_ANALYSIS_RESULT_FILENAME_FORMAT");
    // ?????????????????????????????????????????????????????????????????????
    public static final int GET_TABLE_DATA_MAX_COUNT = Integer.parseInt(Config.getString("GET_TABLE_DATA_MAX_COUNT", "10"));


    /**
     * @param args
    * @throws IOException
     * @throws SQLException
     */
    public static void main(String[] args) throws IOException, SQLException {
        System.out.println("???????????????");

        Scanner sc = new Scanner(System.in);
        System.out.println("??????ID??????????????????????????????");
        String inPgmId = sc.nextLine();
        sc.close();

        if (Strings.isNullOrEmpty(inPgmId)) {
            System.out.println("???????????????");
            System.exit(0);
        }

        Table<Integer, Integer, String> table = getPgmTable(inPgmId);
        String pgmName = table.get(0, 4);
        if (Strings.isNullOrEmpty(pgmName)) {
            System.out.println(String.format("??????ID[%s]????????????????????????", inPgmId));
            System.exit(0);
        }

        Workbook workbook = ExcelUtil.getWorkbook();
        Font font = workbook.getFontAt((short) 0);
        //?????????"?????? ??????"?????????????????????"?????? ???????????????"??????????????????????????????
        font.setFontName("?????? ??????");

        createTableList(workbook, table);
        createSqlInfoList(workbook, inPgmId, pgmName);

        ExecDao dao = new ExecDao();

        int rowSize = table.rowKeySet().size();
        for (int rowNo = 0; rowNo < rowSize; rowNo++) {
//            String no = table.get(rowNo, 0);
            String tableName = table.get(rowNo, 1);
            String tableNameStr = table.get(rowNo, 2);
            String kubun = table.get(rowNo, 3);

            List<String> primaryKeyList = dao.getPrimaryKeyList(tableName);

            List<List<String>> columnInfoList = dao.getColumnInfoList(tableName);
            List<String> columnNameList = getColumnNameList(columnInfoList);
            List<String> columnTypeList = getColumnTypeList(columnInfoList);

//            List<String> searchColumnList = getSearchColumnList(table);
//            List<String> searchConditionsList = getSearchConditionsList(table);
//            List<String> searchValueList = getSearchValueList(table);
//            String searchFreeConditions = table.get(Def.SEARCH_FREE_CONDITIONS_ROW_NO, Def.SEARCH_FREE_CONDITIONS_COLUMN_NO);

            String selectSql = String.format("SELECT * FROM %s WHERE ROWNUM <= %s", tableName, GET_TABLE_DATA_MAX_COUNT);
            System.out.println(selectSql);
            List<List<String>> dataList = dao.getDataList(selectSql, columnNameList);

            Sheet sheet = workbook.createSheet(tableName);

            // ???????????????
            ExcelUtil.createRow(sheet, Def.TABLE_NAME_ROW_NO, Arrays.asList("?????????????????????", tableName, "", "?????????????????????", tableNameStr, "", "????????????", kubun, "", "?????????????????????"));
            setCellHyperlink(workbook, sheet.getRow(Def.TABLE_NAME_ROW_NO).getCell(9), "??????????????????");
             // ?????????
            List<String> tmpPrimaryKeyList = Lists.newArrayList("?????????");
            tmpPrimaryKeyList.addAll(primaryKeyList);
            ExcelUtil.createRow(sheet, Def.PRIMARY_KEY_ROW_NO, tmpPrimaryKeyList);
             // ???????????????
            List<String> tmpSearchColumnList = Arrays.asList("???????????????");
//            tmpSearchColumnList.addAll(primaryKeyList);
            ExcelUtil.createRow(sheet, Def.SEARCH_COLUMN_ROW_NO, tmpSearchColumnList);
             // ????????????
            ExcelUtil.createRow(sheet, Def.SEARCH_CONDITIONS_ROW_NO, Arrays.asList("????????????"));
             // ?????????
            ExcelUtil.createRow(sheet, Def.SEARCH_VALUE_ROW_NO, Arrays.asList("?????????"));
             // ?????????????????????
            ExcelUtil.createRow(sheet, Def.SEARCH_FREE_CONDITIONS_ROW_NO, Arrays.asList("?????????????????????"));
            // ?????????????????????
            ExcelUtil.createRow(sheet, Def.DELETE_FREE_CONDITIONS_ROW_NO, Arrays.asList("?????????????????????"));
             // ?????????
            ExcelUtil.createRow(sheet, Def.DELETE_FREE_CONDITIONS_ROW_NO + 1, Arrays.asList("?????????"));
            // ???????????????
            ExcelUtil.createRow(sheet, Def.COLUMN_NAME_ROW_NO, columnNameList);
            // ????????????????????????
            ExcelUtil.createRow(sheet, Def.COLUMN_TYPE_ROW_NO, columnTypeList);
            // ???????????????int startRowNo = Def.DATA_START_ROW_NO;

            //???????????????????????????
            sheet.createFreezePane(0, Def.COLUMN_TYPE_ROW_NO + 1);

            //?????????????????????
            for (Row row : sheet) {
                int lastCellNum = row.getLastCellNum();
                for (int i = 0; i < lastCellNum; i++) {
                    sheet.autoSizeColumn(i, true);
                }
            }

            int startRowNo = Def.DATA_START_ROW_NO;
            for (List<String> list : dataList) {
                ExcelUtil.createRow(sheet, startRowNo++, list);
            }

        }

        setTableListHyperlink(workbook);;

        ExcelUtil.save(DEFALUT_FILENAME, workbook);

        System.out.println("???????????????");
    }

    private static Table<Integer, Integer, String> getPgmTable(String inPgmId) {
        System.out.println("Start read P_???SUB_ACCSESS_DB.xlsx");

        Table<Integer, Integer, String> table = ExcelUtil.getTableBySXSSF(FILE_P_SUB_ACCESS_DB);
        Table<Integer, Integer, String> returnTable = HashBasedTable.create();

        int rowSize = table.rowKeySet().size();
        int startRowNo = 0;
        for (int rowNo = 3; rowNo < rowSize; rowNo++) {
            if (Strings.isNullOrEmpty(table.get(rowNo, 1))) {
                continue;
            }

            String pgmId = table.get(rowNo, 1);

            if (!pgmId.equals(inPgmId)) {
                continue;
            }

            Map<Integer, String> row = table.row(rowNo);
            for (Entry<Integer, String> rowEntry : row.entrySet()) {
                int colNo = rowEntry.getKey();
                String value = rowEntry.getValue().trim();

                if (colNo < 3) {
                    continue;
                }

                if (!Strings.isNullOrEmpty(value)) {
                    String tableName = table.get(1, colNo);
                    String tableNameStr = table.get(2, colNo);

                    returnTable.put(startRowNo, 0, String.valueOf(startRowNo+1));
                    returnTable.put(startRowNo, 1, tableName);
                    returnTable.put(startRowNo, 2, tableNameStr);
                    returnTable.put(startRowNo, 3, value);
                    returnTable.put(startRowNo, 4, table.get(rowNo, 2));

                    startRowNo++;
                }
            }

        }

        System.out.println("End read P_???SUB_ACCSESS_DB.xlsx");
        return returnTable;
    }

    private static void createTableList(Workbook workbook, Table<Integer, Integer, String> table) {
        Sheet sheet = workbook.createSheet("??????????????????");
        ExcelUtil.createRow(sheet, 0, Arrays.asList("No.", "?????????", "?????????", "????????????"));
        //???????????????????????????
        sheet.createFreezePane(0, 1);

        int rowSize = table.rowKeySet().size();
        for (int rowNo = 0; rowNo < rowSize; rowNo++) {
            String no = table.get(rowNo, 0);
            String tableName = table.get(rowNo, 1);
            String tableNameStr = table.get(rowNo, 2);
            String kubun = table.get(rowNo, 3);

            ExcelUtil.createRow(sheet, rowNo + 1, Arrays.asList(no, tableName, tableNameStr, kubun));
        }

        //?????????????????????
        for (Row row : sheet) {
            int lastCellNum = row.getLastCellNum();
            for (int i = 0; i < lastCellNum; i++) {
                sheet.autoSizeColumn(i, true);
            }
        }

    }

    private static void setTableListHyperlink(Workbook workbook) {

        setHyperlink(workbook, "??????????????????", 1);
    }

    private static void setHyperlink(Workbook workbook, String sheetName, int colIndex) {

        Sheet sheet = workbook.getSheet(sheetName);
        int count = 0;
        for (Row row : sheet) {
            if (count == 0) {
                count++;
                continue;
            }

            Cell cell = row.getCell(colIndex);
            String value = ExcelUtil.getStringValue(cell);
            setCellHyperlink(workbook, cell, value);

        }
    }

    private static void setCellHyperlink(Workbook workbook, Cell cell, String linkSheetName) {
        CreationHelper ch = workbook.getCreationHelper();
        Hyperlink link = ch.createHyperlink(HyperlinkType.DOCUMENT);
        link.setAddress(String.format("%s!A1", linkSheetName));

        cell.setHyperlink(link);

        // ?????????????????????
        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLUE.getIndex());
        font.setUnderline(Font.U_SINGLE);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        cell.setCellStyle(style);
    }

    private static void createSqlInfoList(Workbook workbook, String pgmId, String pgmName) throws IOException {
        if (Strings.isNullOrEmpty(pgmId) || Strings.isNullOrEmpty(pgmName)) {
            return ;
        }

        // SQL???????????????
        List<String> sqlList = getSqlList(pgmId, pgmName);
        if (sqlList != null && !sqlList.isEmpty()) {
            Sheet sheet = workbook.createSheet("SQL?????????");
            ExcelUtil.createRow(sheet, 0, Arrays.asList("???", "SQL??????????????????", "??????????????????", "CURD", "???????????????", "????????????"));
            //???????????????????????????
            sheet.createFreezePane(0, 1);

            // "???", "SQL??????????????????", "??????????????????", "CURD", "???????????????", "????????????"

            Map<String, String> sqlMap2 = getSqlMap(pgmId);
            if (sqlMap2 == null || sqlMap2.isEmpty()) {
                // SQL??????????????????????????????
                List<List<String>> rowValueList = new ArrayList<List<String>>();
                for (int i = 0; i < sqlList.size(); i++) {
                    // "???"
                    int no = i + 1;
                    // SQL??????????????????
                    String filename = "";
                    // ???????????????
                    String sql = sqlList.get(i);
                    String[] strArray = sql.split(" ");

                    // CURD
                    String curd = strArray[0];

                    // ??????????????????
                    List<String> tableList = new ArrayList<String>();
                    for (String str : strArray) {
                        // PS, PT, PV
                        str = str.replaceAll("\"", "");
                        if (str.startsWith("PS") || str.startsWith("PT") || str.startsWith("PV")) {
                            tableList.add(str);
                        }
                    }

                    List<String> colValueList = new ArrayList<String>();
                    colValueList.add(String.valueOf(no));
                    colValueList.add(filename);
                    colValueList.add(Joiner.on(", ").join(tableList));
                    colValueList.add(curd);
                    colValueList.add(sql);

                    if (sql.contains("SELECT")) {
                        colValueList.add(sql.substring(sql.lastIndexOf("WHERE") + "WHERE".length()));
                    } else {
                        colValueList.add("");
                    }

                    rowValueList.add(colValueList);
                }

                for (int i = 0; i < rowValueList.size(); i++) {
                    ExcelUtil.createRow(sheet, i + 1, rowValueList.get(i));
                }

                //?????????????????????
                for (Row row : sheet) {
                    int lastCellNum = row.getLastCellNum();
                    for (int i = 0; i < lastCellNum; i++) {
                        if (i > 3) {
                            continue;
                        }

                        sheet.autoSizeColumn(i, true);
                    }
                }

            } else {
                // SQL??????????????????????????????

                int no = 0;
                List<List<String>> rowValueList = new ArrayList<List<String>>();
                for (Entry<String, String> entry : sqlMap2.entrySet()) {
                    // "???"
                    no++;
                    // SQL??????????????????
                    String filename = entry.getKey();
                    // ???????????????
                    String sql = entry.getValue();
                    String[] strArray = sql.split(" ");

                    // CURD
                    String curd = strArray[0];

                    // ??????????????????
                    List<String> tableList = new ArrayList<String>();
                    for (String str : strArray) {
                        // PS, PT, PV
                        str = str.replaceAll("\"", "");
                        if (str.startsWith("PS") || str.startsWith("PT") || str.startsWith("PV")) {
                            tableList.add(str);
                        }
                    }

                    List<String> colValueList = new ArrayList<String>();
                    colValueList.add(String.valueOf(no));
                    colValueList.add(filename);
                    colValueList.add(Joiner.on(", ").join(tableList));
                    colValueList.add(curd);
                    colValueList.add(sql);

                    if (sql.contains("SELECT")) {
                        colValueList.add(sql.substring(sql.lastIndexOf("WHERE") + "WHERE".length()));
                    } else {
                        colValueList.add("");
                    }


                    rowValueList.add(colValueList);
                }

                for (int i = 0; i < rowValueList.size(); i++) {
                    List<String> list = rowValueList.get(i);

                    if (sqlList.contains(list.get(4))) {
                        ExcelUtil.createRow(sheet, i + 1, list);
                    } else {
                        ExcelUtil.createRow(sheet, i + 1, list, workbook, IndexedColors.GREY_50_PERCENT);
                    }

                }
            }

            //?????????????????????
            for (Row row : sheet) {
                int lastCellNum = row.getLastCellNum();
                for (int i = 0; i < lastCellNum; i++) {
                    if (i > 3) {
                        continue;
                    }

                    sheet.autoSizeColumn(i, true);
                }
            }
        }
    }

    private static List<String> getSqlList(String pgmId, String pgmName) {
        String filepath = String.format(COBOL_ANALYSIS_RESULT_BASE_FOLDER_FILEPATH + File.separator + COBOL_ANALYSIS_RESULT_FILENAME_FORMAT, pgmId, pgmName);

        System.out.println("Start read " + filepath);

        Table<Integer, Integer, String> table = ExcelUtil.getTable(filepath, "????????????");
        if (table == null || table.rowKeySet().size() == 0) {
            return null;
        }

        String keyword1 = "??????SQL";
        String keyword2 = "??????SQL";
        String keyword3 = "??????SQL";
        String keyword4 = "??????SQL";
        String keyword5 = "??????????????????";

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

            if (cellValue.toString().contains(keyword1)) {
                // ??????
                bRead = true;
                isSearch = true;
                isCursor = false;

            } else if (cellValue.toString().contains(keyword2)) {
                // ??????
                bRead = true;
                isSearch = false;
                isCursor = false;

            } else if (cellValue.toString().contains(keyword3)) {
                // ??????
                bRead = true;
                isSearch = false;
                isCursor = false;

            } else if (cellValue.toString().contains(keyword4)) {
                // ??????
                bRead = true;
                isSearch = false;
                isCursor = false;

            } else if (cellValue.toString().contains(keyword5)) {
                // ??????????????????
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

                            list.add(Common.changeSql(sb.toString()).replaceAll("  ", " ").replaceAll("  ", " ").replaceAll("  ", " ").replace(" )", ")").replace("( ", "(").replace("WHERE(", "WHERE ("));
                            sb = new StringBuffer();
                        } else {
                            searchOne = true;
                        }
                    } else {
                        if (!(cellValue.toString().contains(keyword1) || cellValue.toString().contains(keyword2) || cellValue.toString().contains(keyword3) || cellValue.toString().contains(keyword4) || cellValue.toString().contains(keyword5))) {
                            sb.append(cellValue);
                        }
                    }

                } else if (isCursor) {

                    if (cellValue.toString().contains("-------")) {
                        if (cursorOne) {
                            bRead = false;
                            cursorOne = false;

                            list.add(Common.changeSql(sb.toString()).replaceAll("  ", " ").replaceAll("  ", " ").replaceAll("  ", " ").replace(" )", ")").replace("( ", "(").replace("WHERE(", "WHERE ("));
                            sb = new StringBuffer();
                        } else {
                            cursorOne = true;
                        }
                    } else {
                        if (!(cellValue.toString().contains(keyword1) || cellValue.toString().contains(keyword2) || cellValue.toString().contains(keyword3) || cellValue.toString().contains(keyword4) || cellValue.toString().contains(keyword5))) {
                            sb.append(cellValue);
                        }
                    }

                } else {
                    if (cellValue.toString().contains("-------")) {
                        bRead = false;

                        list.add(Common.changeSql(sb.toString()).replaceAll("  ", " ").replaceAll("  ", " ").replaceAll("  ", " ").replace(" )", ")").replace("( ", "(").replace("WHERE(", "WHERE ("));
                        sb = new StringBuffer();

                    } else {
                        if (!(cellValue.toString().contains(keyword1) || cellValue.toString().contains(keyword2) || cellValue.toString().contains(keyword3) || cellValue.toString().contains(keyword4) || cellValue.toString().contains(keyword5))) {
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

    private static List<String> getColumnNameList(List<List<String>> columnInfoList) {
        List<String> columnNameList = new ArrayList<String>();
        for (List<String> list : columnInfoList) {
            columnNameList.add(list.get(0));
        }

        return columnNameList;
    }

    private static List<String> getColumnTypeList(List<List<String>> columnInfoList) {
        List<String> columnTypeList = new ArrayList<String>();
        for (List<String> list : columnInfoList) {
            columnTypeList.add(String.format("%s(%s)", list.get(1), list.get(2)));
        }

        return columnTypeList;
    }

}
