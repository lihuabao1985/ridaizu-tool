package tool.pcl;

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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import common.Common;
import common.DateUtil;
import common.Def;
import common.ExcelUtil;
import config.Config;
import dao.ExecDao;

public class CreatePCL {

    public static final String Y = "Y";
    public static final String N = "N";
    public static final String E = "E";
    public static final String L = "L";
    public static final String I = "I";
    public static final String U = "U";
    public static final String MARU = "○";
    public static final String BATU = "×";
    public static final int TABLE_NUM = 4;

    // セルスタイルをコピーするか
    public static final boolean copyCellStyle = Boolean.parseBoolean(Config.getString("IS_COPY_CELL_STYLE"));

    // SQL文置き場所
    public static final String SQL_FILE_BASE_FOLDER_FILEPATH = Config.getString("SQL_FILE_BASE_FOLDER_FILEPATH", "template\\sql");
    // COBOL解析結果置き場所
    public static final String COBOL_ANALYSIS_RESULT_BASE_FOLDER_FILEPATH = Config.getString("COBOL_ANALYSIS_RESULT_BASE_FOLDER_FILEPATH", "template\\COBOL解析結果");
    // COBOL解析結果ファイル名フォーマット
    public static final String COBOL_ANALYSIS_RESULT_FILENAME_FORMAT = Config.getString("COBOL_ANALYSIS_RESULT_FILENAME_FORMAT");
    // ファイル出力フォルダー
    public static final String OUTPUT_FILEPATH = Config.getString("OUTPUT_FILEPATH", "output");
    // ファイル出力フォルダー
    public static final String TEMPLATE_FILEPATH = Config.getString("TEMPLATE_FILEPATH", "template");

    static final String FILE_TEMPLATE = TEMPLATE_FILEPATH + File.separator + "template.xlsm";
    static final String FILE_TEMPLATE_PCL = TEMPLATE_FILEPATH + File.separator + "機能ID_機能名_標準チェックリスト（バッチ）.xlsm";
    static final String FILE_TEMPLATE_PGM_CHANGE = TEMPLATE_FILEPATH + File.separator + "機能ID_機能名_プログラム変更票.xls";
    static final String FILE_P_SUB_ACCESS_DB = TEMPLATE_FILEPATH + File.separator + "P_全SUB_ACCSESS_DB.xlsx";


    public static void main(String[] args) throws Exception {
        System.out.println("Start create PCL.");

        String pgmId = Config.getString("PGM_ID");
        String pgmName = Config.getString("PGM_NAME");
        String author = Config.getString("AUTHOR");
        String createDate = Config.getString("CREATE_DATE", DateUtil.dateToString(DateUtil.getCurrentDateTime(), DateUtil.SHORT_DATE));

        System.out.println("---------------------------------------------------------------------------------------");
        System.out.println(String.format("機能ID「%s」\n機能名「%s」\n作成者「%s」\n作成日「%s」", pgmId, pgmName, author, createDate));
        System.out.println("---------------------------------------------------------------------------------------");

        Table<Integer, Integer, String> table = getPgmTable(pgmId);

        if (table.rowKeySet().size() == 0) {
            System.exit(0);
        }

        File outputFolderFile = new File(OUTPUT_FILEPATH);
        if (!outputFolderFile.exists()) {
            outputFolderFile.mkdirs();
        }

        Workbook templateWorkbook = ExcelUtil.getWorkbook(FILE_TEMPLATE);

        exec(pgmId, pgmName, author, createDate, templateWorkbook, table);

        System.out.println("End create PCL.");
    }

    private static List<Object> getTableInfo(Table<Integer, Integer, String> table, String tableName) {
        int rowSize = table.rowKeySet().size();
        List<Object> list = new ArrayList<Object>();
        for (int rowNo = 0; rowNo < rowSize; rowNo++) {
            if (Strings.isNullOrEmpty(table.get(rowNo, 1))) {
                continue;
            }

            if (table.get(rowNo, 1).equals(tableName)) {
                list.add(rowNo);
                list.add(table.get(rowNo, 1));
                list.add(table.get(rowNo, 2));
                list.add(table.get(rowNo, 3));
            }
        }

        return list;
    }

    private static void exec(String pgmId, String pgmName, String author, String createDate, Workbook templateWorkbook, Table<Integer, Integer, String> table) throws IOException, SQLException {

        int rowSize = table.rowKeySet().size();

        // PCL (入力件数パターン確認)
        // SF List
        List<Integer> sfList = new ArrayList<Integer>();

        // PCL (マスタ確認)
        // マスタ List
        List<Integer> masterList = new ArrayList<Integer>();

        // PCL (更新エラー確認)
        // IUD List
        List<Integer> iudList = new ArrayList<Integer>();

        System.out.println("");
        System.out.println("Start create 標準チェックリスト（バッチ）。");
        System.out.println("--------------関連テーブル一覧--------------");
//        for (int rowNo = 0; rowNo < rowSize; rowNo++) {
//            if (Strings.isNullOrEmpty(table.get(rowNo, 1))) {
//                continue;
//            }
//
//            // 論理名
//            String tableName = table.get(rowNo, 1);
//            // 物理名
//            String tableNameStr = table.get(rowNo, 2);
//            // 操作区分
//            String kubun = table.get(rowNo, 3);
//
//            boolean isS = kubun.contains("S");
//            boolean isF = kubun.contains("F");
//            boolean isI = kubun.contains("I");
//            boolean isU = kubun.contains("U");
//            boolean isD = kubun.contains("D");
//
//            if ((isS || isF) && !tableNameStr.contains("マスタ")) {
//                sfList.add(rowNo);
//            }
//
//            if (isI || isU || isD) {
//                iudList.add(rowNo);
//            }
//
//            if (tableNameStr.contains("マスタ") && !"PV".equals(tableName.substring(0, 2))) {
//                masterList.add(rowNo);
//            }
//
//            System.out.println(String.format("%s\t%s\t%s", tableName, tableNameStr, kubun));
//        }
//
//        // データがない場合、処理終了
//        if (sfList.isEmpty() && masterList.isEmpty() && iudList.isEmpty()) {
//            return ;
//        }
//
//        System.out.println("");
//        System.out.println("--------------SFテーブル一覧--------------");
//        for (int rowNo : sfList) {
//            System.out.println(String.format("%s\t%s\t%s", table.get(rowNo, 1), table.get(rowNo, 2), table.get(rowNo, 3)));
//        }
//
//        System.out.println("");
//        System.out.println("--------------マスタテーブル一覧--------------");
//        for (int rowNo : masterList) {
//            System.out.println(String.format("%s\t%s\t%s", table.get(rowNo, 1), table.get(rowNo, 2), table.get(rowNo, 3)));
//        }
//
//        System.out.println("");
//        System.out.println("--------------IUDテーブル一覧--------------");
//        for (int rowNo : iudList) {
//            System.out.println(String.format("%s\t%s\t%s", table.get(rowNo, 1), table.get(rowNo, 2), table.get(rowNo, 3)));
//        }

        System.out.println("");

        // プログラム変更票を作成
        createChangePgmP(pgmId, pgmName, author, createDate);

        Workbook workbook = ExcelUtil.getWorkbook(FILE_TEMPLATE_PCL);
        Sheet sheet = workbook.getSheet("集計");

        // シート「集計」情報を設定--------------------------------------------------------------------------------------------------
        ExcelUtil.setCellValue(sheet.getRow(11).getCell(21), pgmId);
        ExcelUtil.setCellValue(sheet.getRow(12).getCell(21), pgmName);
        ExcelUtil.setCellValue(sheet.getRow(3).getCell(38), author);
        ExcelUtil.setCellValue(sheet.getRow(3).getCell(41), createDate);


        // SQL文一覧作成
        List<String> sqlList = getSqlList(pgmId, pgmName);
        setSqlListInfo(workbook, templateWorkbook, pgmId, pgmName, sqlList);

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < sqlList.size(); i++) {
            String[] strArray = sqlList.get(i).split("####")[0].split(" ");
            // テーブル対象
            for (String str : strArray) {
                // PS, PT, PV
                str = str.replaceAll("\"", "");
                if (str.startsWith("PS") || str.startsWith("PT") || str.startsWith("PV")) {
                    if (!list.contains(str)) {
                        list.add(str);
                    }
                }
            }
        }

        for (int i = 0; i < list.size(); i++) {
            String tableName = list.get(i);
            List<Object> tableInfo = getTableInfo(table, tableName);
            int rowNo = (int)tableInfo.get(0);
            // 論理名
//            String tableName = (String)tableInfo.get(1);
            // 物理名
            String tableNameStr = (String)tableInfo.get(2);
            // 操作区分
            String kubun = (String)tableInfo.get(3);

            boolean isS = kubun.contains("S");
            boolean isF = kubun.contains("F");
            boolean isI = kubun.contains("I");
            boolean isU = kubun.contains("U");
            boolean isD = kubun.contains("D");

            if ((isS || isF) && !tableNameStr.contains("マスタ")) {
                sfList.add(rowNo);
            }

            if (isI || isU || isD) {
                iudList.add(rowNo);
            }

            if (tableNameStr.contains("マスタ") && !"PV".equals(tableName.substring(0, 2))) {
                masterList.add(rowNo);
            }

            System.out.println(String.format("%s\t%s\t%s", tableName, tableNameStr, kubun));
        }

        System.out.println("");
        System.out.println("--------------SFテーブル一覧--------------");
        for (int rowNo : sfList) {
            System.out.println(String.format("%s\t%s\t%s", table.get(rowNo, 1), table.get(rowNo, 2), table.get(rowNo, 3)));
        }

        System.out.println("");
        System.out.println("--------------マスタテーブル一覧--------------");
        for (int rowNo : masterList) {
            System.out.println(String.format("%s\t%s\t%s", table.get(rowNo, 1), table.get(rowNo, 2), table.get(rowNo, 3)));
        }

        System.out.println("");
        System.out.println("--------------IUDテーブル一覧--------------");
        for (int rowNo : iudList) {
            System.out.println(String.format("%s\t%s\t%s", table.get(rowNo, 1), table.get(rowNo, 2), table.get(rowNo, 3)));
        }



        int addPageCount = 0;

        // シート「PCL (入力件数パターン確認)」情報を設定----------------------------------------------------------------------------

        if (sfList.isEmpty()) {
            workbook.removeSheetAt(3);
            addPageCount--;
        } else {
            if (Boolean.parseBoolean(Config.getString("IS_CREATE_COLUMN", "false"))) {
                setSFInfoByCreateColumn(workbook, templateWorkbook, table, sfList, iudList);
            } else {
                setSFInfo(workbook, templateWorkbook, table, sfList, iudList);
            }
        }

        // シート「PCL (マスタ確認)」情報を設定----------------------------------------------------------------------------

        if (masterList.isEmpty()) {
            workbook.removeSheetAt(workbook.getSheetIndex("PCL (マスタ確認)"));
            addPageCount--;
        } else {
            setMasterInfo(workbook, templateWorkbook, table, masterList, iudList);
        }

        // シート「PCL (更新エラー確認)」情報を設定----------------------------------------------------------------------------

        if (iudList.isEmpty()) {
            workbook.removeSheetAt(workbook.getSheetIndex("PCL (更新エラー確認)"));
            addPageCount--;
        } else {
            setIudInfo(workbook, templateWorkbook, table, iudList);
        }

        Sheet sheetDetail = workbook.getSheet("詳細");
        ExcelUtil.setCellValue(sheetDetail.getRow(11).getCell(14), String.valueOf(3 + addPageCount));

        // エクセル起動する時、公式を実行するように
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            workbook.getSheetAt(i).setForceFormulaRecalculation(true);
        }

//		workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();

        String filePath = String.format(OUTPUT_FILEPATH + File.separator + "%s_%s_標準チェックリスト（バッチ）.xlsm", pgmId, pgmName);
        ExcelUtil.save(filePath, workbook);
        workbook.close();

        System.out.println(String.format("「%s」ファイルが保存されました。", filePath));
        System.out.println("End create 標準チェックリスト（バッチ）。");
    }

    private static void setSFInfo(Workbook workbook, Workbook templateWorkbook, Table<Integer, Integer, String> table, List<Integer> sfList, List<Integer> iudList) throws SQLException {

        // シート「PCL (入力件数パターン確認)」情報を設定----------------------------------------------------------------------------

        // 【入力件数パターン確認】　情報設定
        System.out.println("Start update sheet「PCL (入力件数パターン確認)」 ");

        int tmpStartColNo = 23;
        int startRowNo = 0;
        int endRowNo = 0;
        int startColNo = 0;
        int endColNo = 0;

        int sheetCount = sfList.size() / TABLE_NUM;
        if (sfList.size() % TABLE_NUM != 0) {
            sheetCount++;
        }

        int startSheetNo = 3;
        if (sheetCount > 1) {

            for (int i = 1; i < sheetCount; i++) {
                workbook.cloneSheet(startSheetNo);
                workbook.setSheetOrder(workbook.getSheetAt(workbook.getNumberOfSheets() - 1).getSheetName(), startSheetNo + i);

                Sheet tmpSheet = workbook.getSheetAt(startSheetNo + i);
                Cell cell = ExcelUtil.getCell(tmpSheet, 10, 4);
                String value = ExcelUtil.getStringValue(cell);
                value = value.replaceAll("】", i + 1 + "】");
                ExcelUtil.setCellValue(cell, value);
            }


            Sheet sheetDetail = workbook.getSheet("詳細");
            ExcelUtil.setCellValue(sheetDetail.getRow(11).getCell(14), String.valueOf(3 + sheetCount - 1));

            Sheet tmpSheet = workbook.getSheetAt(startSheetNo);
            Cell cell = ExcelUtil.getCell(tmpSheet, 10, 4);
            String value = ExcelUtil.getStringValue(cell);
            value = value.replaceAll("】", "1】");
            ExcelUtil.setCellValue(cell, value);


        }

        String srcSheet1Name = "PCL (入力件数パターン確認)";
        int tmpCount = 1;

        for (int i = 0; i < sheetCount; i++) {

            List<Integer> value0List = new ArrayList<Integer>();
            Multimap<String, Integer> tableNameMultimap1 = ArrayListMultimap.create();

            int startRowNo1 = 11;
            int addRowCount = 0;
            int Y1Count = 0;
            int rowCount = 1;

            String sheetName = workbook.getSheetAt(startSheetNo + i).getSheetName();
            Sheet sheet = workbook.getSheet(sheetName);

            for (int j = 0; j < sfList.size(); ) {
                int rowNo = sfList.get(j);

                if (rowCount > TABLE_NUM) {
                    rowCount = 1;
                    break;
                }

                // 論理名
                String tableName = table.get(rowNo, 1);
                // 物理名
                String tableNameStr = table.get(rowNo, 2);

                String name = String.format("%s・%s", tableNameStr, tableName);

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 1, workbook, sheetName, startRowNo1, copyCellStyle);
                String stringValue = ExcelUtil.getStringValue(sheet.getRow(startRowNo1).getCell(5)) + Def.zenkakuNumberMap.get(tmpCount);
                ExcelUtil.setCellValue(sheet.getRow(startRowNo1).getCell(5), stringValue);
                startRowNo1++;

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 2, workbook, sheetName, startRowNo1, copyCellStyle);
                ExcelUtil.setCellValue(sheet.getRow(startRowNo1).getCell(6), name);
                startRowNo1++;

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 3, workbook, sheetName, startRowNo1, copyCellStyle);
                ExcelUtil.setCellValue(sheet.getRow(startRowNo1).getCell(tmpStartColNo), Y);
                tableNameMultimap1.put(tableName, tmpStartColNo);
                value0List.add(tmpStartColNo);
                startRowNo1++;
                Y1Count++;
                tmpStartColNo++;

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 4, workbook, sheetName, startRowNo1, copyCellStyle);
                ExcelUtil.setCellValue(sheet.getRow(startRowNo1).getCell(tmpStartColNo), Y);
                tableNameMultimap1.put(tableName, tmpStartColNo);
                startRowNo1++;
                Y1Count++;
                tmpStartColNo++;

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 5, workbook, sheetName, startRowNo1, copyCellStyle);
                ExcelUtil.setCellValue(sheet.getRow(startRowNo1).getCell(tmpStartColNo), Y);
                tableNameMultimap1.put(tableName, tmpStartColNo);
                startRowNo1++;
                Y1Count++;
                tmpStartColNo++;

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 6, workbook, sheetName, startRowNo1, copyCellStyle);
                ExcelUtil.setCellValue(sheet.getRow(startRowNo1).getCell(tmpStartColNo), Y);
                tableNameMultimap1.put(tableName, tmpStartColNo);
                startRowNo1++;
                Y1Count++;
                tmpStartColNo++;

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 7, workbook, sheetName, startRowNo1++, copyCellStyle);

                addRowCount += 7;
                tmpCount++;

                rowCount++;
                sfList.remove(j);
            }

            startRowNo = 9;
            endRowNo = 13;
            startColNo = 2;
            endColNo = 3;

            endRowNo += addRowCount;

            sheet.addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));


            // バッチのリターンコードが現・新で同じであること
            // 正常 = 0 情報設定
            for (int j = 0; j < Y1Count; j++) {
                tmpStartColNo = 23;
                int tmpRowNo = endRowNo + 3;
                ExcelUtil.setCellValue(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
            }

            // 出力ＤＢ確認　情報設定
            int startRowNo2 = 21 + addRowCount;
            int addRowCount2 = 0;
            for (int rowNo : iudList) {

                // 論理名
                String tableName = table.get(rowNo, 1);
                // 物理名
                String tableNameStr = table.get(rowNo, 2);

                String name = String.format("%s・%s", tableNameStr, tableName);

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 8, workbook, sheetName, startRowNo2, copyCellStyle);
                ExcelUtil.setCellValue(sheet.getRow(startRowNo2).getCell(5), name);

                List<Integer> colIndexList = Lists.newArrayList(tableNameMultimap1.get(tableName));
                for (Integer colIndex : colIndexList) {
                    ExcelUtil.setCellValue(sheet.getRow(startRowNo2).getCell(colIndex), MARU);
                }

                startRowNo2++;
                addRowCount2++;
            }


            ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 9, workbook, sheetName, startRowNo2++, copyCellStyle);addRowCount2++;
            ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 10, workbook, sheetName, startRowNo2++, copyCellStyle);addRowCount2++;
            ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 11, workbook, sheetName, startRowNo2++, copyCellStyle);addRowCount2++;

            startRowNo = startRowNo1 + 3;
            endRowNo = startRowNo + 24 + addRowCount2;

            sheet.addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));

            // 出力ＤＢ確認
            for (int j = 0; j < Y1Count; j++) {
                tmpStartColNo = 23;

                if (value0List.contains(tmpStartColNo + j)) {
                    int tmpRowNo = endRowNo - 20;
                    ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
                } else {
                    int tmpRowNo = endRowNo - 19;
                    ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
                    tmpRowNo = endRowNo - 18;
                    ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
                }
            }

            // ＳＹＳＯＵＴログ 情報設定
            //   開始・終了メッセージ出力
            for (int j = 0; j < Y1Count; j++) {
                tmpStartColNo = 23;
                int tmpRowNo = endRowNo - 12;
                ExcelUtil.setCellValue(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
            }

            // ＳＹＳＯＵＴログ 情報設定
            //   入出力件数出力
            for (int j = 0; j < Y1Count; j++) {
                tmpStartColNo = 23;
                int tmpRowNo = 0;

                if (value0List.contains(tmpStartColNo + j)) {
                    tmpRowNo = endRowNo - 9;
                } else {
                    tmpRowNo = endRowNo - 8;
                }

                ExcelUtil.setCellValue(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
            }

            // PCL区分
            for (int j = 0; j < Y1Count; j++) {
                tmpStartColNo = 23;

                int tmpRowNo = endRowNo + 5;
                if (value0List.contains(tmpStartColNo + j)) {
                    ExcelUtil.setCellValue(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), L);
                } else {
                    ExcelUtil.setCellValue(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), N);
                }
            }

        }
        System.out.println("End update sheet「PCL (入力件数パターン確認)」");

    }

    private static void setSFInfoByCreateColumn(Workbook workbook, Workbook templateWorkbook, Table<Integer, Integer, String> table, List<Integer> sfList, List<Integer> iudList) throws SQLException {

        // シート「PCL (入力件数パターン確認)」情報を設定----------------------------------------------------------------------------

        // 【入力件数パターン確認】　情報設定
        System.out.println("Start update sheet「PCL (入力件数パターン確認)」 ");

        int tmpStartColNo = 23;
        int startRowNo = 0;
        int endRowNo = 0;
        int startColNo = 0;
        int endColNo = 0;

        int sheetCount = sfList.size() / TABLE_NUM;
        if (sfList.size() % TABLE_NUM != 0) {
            sheetCount++;
        }

        int startSheetNo = 3;
        if (sheetCount > 1) {

            for (int i = 1; i < sheetCount; i++) {
                workbook.cloneSheet(startSheetNo);
                workbook.setSheetOrder(workbook.getSheetAt(workbook.getNumberOfSheets() - 1).getSheetName(), startSheetNo + i);

                Sheet tmpSheet = workbook.getSheetAt(startSheetNo + i);
                Cell cell = ExcelUtil.getCell(tmpSheet, 10, 4);
                String value = ExcelUtil.getStringValue(cell);
                value = value.replaceAll("】", i + 1 + "】");
                ExcelUtil.setCellValue(cell, value);
            }


            Sheet sheetDetail = workbook.getSheet("詳細");
            ExcelUtil.setCellValue(sheetDetail.getRow(11).getCell(14), String.valueOf(3 + sheetCount - 1));

            Sheet tmpSheet = workbook.getSheetAt(startSheetNo);
            Cell cell = ExcelUtil.getCell(tmpSheet, 10, 4);
            String value = ExcelUtil.getStringValue(cell);
            value = value.replaceAll("】", "1】");
            ExcelUtil.setCellValue(cell, value);


        }

        ExecDao execDao = new ExecDao();

        String srcSheet1Name = "PCL (入力件数パターン確認)";
        int tmpCount = 1;

        for (int i = 0; i < sheetCount; i++) {

            List<Integer> value0List = new ArrayList<Integer>();
            Multimap<String, Integer> tableNameMultimap1 = ArrayListMultimap.create();

            int startRowNo1 = 11;
            int addRowCount = 0;
            int Y1Count = 0;
            int rowCount = 1;

            String sheetName = workbook.getSheetAt(startSheetNo + i).getSheetName();
            Sheet sheet = workbook.getSheet(sheetName);

            for (int j = 0; j < sfList.size(); ) {
                int rowNo = sfList.get(j);

                if (rowCount > TABLE_NUM) {
                    rowCount = 1;
                    break;
                }

                // 論理名
                String tableName = table.get(rowNo, 1);
                // 物理名
                String tableNameStr = table.get(rowNo, 2);

                String name = String.format("%s・%s", tableNameStr, tableName);

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 1, workbook, sheetName, startRowNo1, copyCellStyle);
                String stringValue = ExcelUtil.getStringValue(sheet.getRow(startRowNo1).getCell(5)) + Def.zenkakuNumberMap.get(tmpCount);
                ExcelUtil.setCellValue(sheet.getRow(startRowNo1).getCell(5), stringValue);
                startRowNo1++;

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 2, workbook, sheetName, startRowNo1, copyCellStyle);
                ExcelUtil.setCellValue(sheet.getRow(startRowNo1).getCell(6), name);
                startRowNo1++;

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 3, workbook, sheetName, startRowNo1, copyCellStyle);
                ExcelUtil.setCellValue(sheet.getRow(startRowNo1).getCell(tmpStartColNo), Y);
                tableNameMultimap1.put(tableName, tmpStartColNo);
                value0List.add(tmpStartColNo);
                startRowNo1++;
                Y1Count++;
                tmpStartColNo++;

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 4, workbook, sheetName, startRowNo1, copyCellStyle);
                ExcelUtil.setCellValue(sheet.getRow(startRowNo1).getCell(tmpStartColNo), Y);
                tableNameMultimap1.put(tableName, tmpStartColNo);
                startRowNo1++;
                Y1Count++;
                tmpStartColNo++;

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 5, workbook, sheetName, startRowNo1, copyCellStyle);
                ExcelUtil.setCellValue(sheet.getRow(startRowNo1).getCell(tmpStartColNo), Y);
                tableNameMultimap1.put(tableName, tmpStartColNo);
                startRowNo1++;
                Y1Count++;
                tmpStartColNo++;

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 6, workbook, sheetName, startRowNo1, copyCellStyle);
                ExcelUtil.setCellValue(sheet.getRow(startRowNo1).getCell(tmpStartColNo), Y);
                tableNameMultimap1.put(tableName, tmpStartColNo);
                startRowNo1++;
                Y1Count++;
                tmpStartColNo++;

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 7, workbook, sheetName, startRowNo1++, copyCellStyle);

                addRowCount += 7;
                tmpCount++;

                rowCount++;
                sfList.remove(j);
            }

            startRowNo = 9;
            endRowNo = 13;
            startColNo = 2;
            endColNo = 3;

            endRowNo += addRowCount;

            sheet.addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));


            // バッチのリターンコードが現・新で同じであること
            // 正常 = 0 情報設定
            for (int j = 0; j < Y1Count; j++) {
                tmpStartColNo = 23;
                int tmpRowNo = endRowNo + 3;
                ExcelUtil.setCellValue(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
            }

            // 出力ＤＢ確認　情報設定
            int startRowNo2 = 21 + addRowCount;
            int addRowCount2 = 0;
            for (int rowNo : iudList) {

                // 論理名
                String tableName = table.get(rowNo, 1);
                // 物理名
                String tableNameStr = table.get(rowNo, 2);
                // 操作区分
                String kubun = table.get(rowNo, 3);

                String name = String.format("%s・%s", tableNameStr, tableName);

                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 8, workbook, sheetName, startRowNo2, copyCellStyle);
                ExcelUtil.setCellValue(sheet.getRow(startRowNo2).getCell(5), name);

//				List<Integer> colIndexList = Lists.newArrayList(tableNameMultimap1.get(tableName));
//				for (Integer colIndex : colIndexList) {
//					ExcelUtil.setCell(sheet.getRow(startRowNo2).getCell(colIndex), MARU);
//				}
                startRowNo2++;
                addRowCount2++;


                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 9, workbook, sheetName, startRowNo2++, copyCellStyle);addRowCount2++;
                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 10, workbook, sheetName, startRowNo2++, copyCellStyle);addRowCount2++;
                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 11, workbook, sheetName, startRowNo2++, copyCellStyle);addRowCount2++;

                // 出力ＤＢ確認
                for (int j = 0; j < Y1Count; j++) {
                    tmpStartColNo = 23;

                    if (value0List.contains(tmpStartColNo + j)) {
                        int tmpRowNo = startRowNo2 - 3;
                        ExcelUtil.setCellValue(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
                    } else {
                        int tmpRowNo = startRowNo2 - 2;
                        ExcelUtil.setCellValue(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
                        tmpRowNo = startRowNo2 - 1;
                        ExcelUtil.setCellValue(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
                    }
                }


                // 現・新で一致
                System.out.println("---------------------------------------------------------------------------------------");
                System.out.println(String.format("Start read table columns. TableName[%s]", tableName));
                List<String> columnNameList = execDao.getColumnNameList(tableName);
//				List<String> columnNameList = getColumnNameList(tableName);
                System.out.println(String.format("TableName[%s] columns: %s", tableName, Joiner.on(", ").join(columnNameList)));
                System.out.println(String.format("End read table columns. TableName[%s]", tableName));
                System.out.println("---------------------------------------------------------------------------------------");

                int tmpStartRowNo = startRowNo2;
                int tmpEndRowNo = 0;
                for (int j = 0; j < columnNameList.size(); j++) {
                    String columnName = columnNameList.get(j);
                    int tempRowIndex = 13;
                    if (kubun.contains(I)) {
                        if ("作成時間".equals(columnName)) {
                            tempRowIndex = 14;
                        }
                    }

                    if (kubun.contains(U)) {
                        if ("更新時間".equals(columnName)) {
                            tempRowIndex = 14;
                        }
                    }

                    ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, tempRowIndex, workbook, sheetName, startRowNo2, copyCellStyle);
                    ExcelUtil.setCellValue(sheet.getRow(startRowNo2).getCell(7), columnName);
                    ExcelUtil.setCellValue(sheet.getRow(startRowNo2).getCell(24), MARU);
                    startRowNo2++;
                    addRowCount2++;
                }
                tmpEndRowNo = startRowNo2 - 1;
                sheet.groupRow(tmpStartRowNo, tmpEndRowNo);



                ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 12, workbook, sheetName, startRowNo2++, copyCellStyle);addRowCount2++;
            }

//			ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 9, workbook, sheetName, startRowNo2++);addRowCount2++;
//			ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 10, workbook, sheetName, startRowNo2++);addRowCount2++;
//			ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 11, workbook, sheetName, startRowNo2++);addRowCount2++;

            startRowNo = startRowNo1 + 3;
            endRowNo = startRowNo + 24 + addRowCount2;

            sheet.addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));



//			// 出力ＤＢ確認
//			for (int j = 0; j < Y1Count; j++) {
//				tmpStartColNo = 23;
//
//				if (value0List.contains(tmpStartColNo + j)) {
//					int tmpRowNo = endRowNo - 20;
//					ExcelUtil.setCell(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
//				} else {
//					int tmpRowNo = endRowNo - 19;
//					ExcelUtil.setCell(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
//					tmpRowNo = endRowNo - 18;
//					ExcelUtil.setCell(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
//				}
//			}



            // ＳＹＳＯＵＴログ 情報設定
            //   開始・終了メッセージ出力
            for (int j = 0; j < Y1Count; j++) {
                tmpStartColNo = 23;
                int tmpRowNo = endRowNo - 12;
                ExcelUtil.setCellValue(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
            }

            // ＳＹＳＯＵＴログ 情報設定
            //   入出力件数出力
            for (int j = 0; j < Y1Count; j++) {
                tmpStartColNo = 23;
                int tmpRowNo = 0;

                if (value0List.contains(tmpStartColNo + j)) {
                    tmpRowNo = endRowNo - 9;
                } else {
                    tmpRowNo = endRowNo - 8;
                }

                ExcelUtil.setCellValue(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
            }

            // PCL区分
            for (int j = 0; j < Y1Count; j++) {
                tmpStartColNo = 23;

                int tmpRowNo = endRowNo + 5;
                if (value0List.contains(tmpStartColNo + j)) {
                    ExcelUtil.setCellValue(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), L);
                } else {
                    ExcelUtil.setCellValue(sheet.getRow(tmpRowNo).getCell(tmpStartColNo + j), N);
                }
            }

        }
        System.out.println("End update sheet「PCL (入力件数パターン確認)」");

    }

    private static List<String> getColumnNameList(String tableName) {
        List<String> columnNameList = new ArrayList<String>();
        columnNameList = Arrays.asList("新車受払管理番号", "作成年月日", "作成時間", "更新年月日", "更新時間");

        return columnNameList;
    }

    private static void setMasterInfo(Workbook workbook, Workbook templateWorkbook, Table<Integer, Integer, String> table, List<Integer> masterList, List<Integer> iudList) {

        // シート「PCL (マスタ確認)」情報を設定----------------------------------------------------------------------------

        int startRowNo3 = 11;
        int addRowCount3 = 0;
        int tmpStartColNo = 23;
        int Y2Count = 0;
        List<Integer> notTableList = new ArrayList<Integer>();

        System.out.println("Start update sheet「PCL (マスタ確認)」");
        for (int rowNo : masterList) {

            // 論理名
            String tableName = table.get(rowNo, 1);
            // 物理名
            String tableNameStr = table.get(rowNo, 2);

            ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 1, workbook, "PCL (マスタ確認)", startRowNo3, copyCellStyle);
            ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(startRowNo3).getCell(5), tableNameStr);
            ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(startRowNo3).getCell(15), tableName);
            startRowNo3++;
            addRowCount3++;

            ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 2, workbook, "PCL (マスタ確認)", startRowNo3, copyCellStyle);
            ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(startRowNo3).getCell(tmpStartColNo), Y);
            notTableList.add(tmpStartColNo);
            addRowCount3++;
            startRowNo3++;
            Y2Count++;
            tmpStartColNo++;

            ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 3, workbook, "PCL (マスタ確認)", startRowNo3, copyCellStyle);
            ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(startRowNo3).getCell(tmpStartColNo++), Y);
            addRowCount3++;
            startRowNo3++;
            Y2Count++;

            ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 4, workbook, "PCL (マスタ確認)", startRowNo3, copyCellStyle);
            addRowCount3++;
            startRowNo3++;

        }

        int startRowNo = 9;
        int endRowNo = 14;
        int startColNo = 2;
        int endColNo = 3;

        endRowNo += addRowCount3;
        workbook.getSheet("PCL (マスタ確認)").addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));


        // バッチのリターンコードが現・新で同じであること
        // 正常・異常
        for (int i = 0; i < Y2Count; i++) {
            tmpStartColNo = 23;
            int tmpRowNo = 0;

            if (notTableList.contains(tmpStartColNo + i)) {
                tmpRowNo = endRowNo + 4;
            } else {
                tmpRowNo = endRowNo + 3;
            }

            ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
        }

        // 出力ＤＢ更新確認
        int startRowNo4 = 22 + addRowCount3;
        int addRowCount4 = 0;
        for (int rowNo : iudList) {

            // 論理名
            String tableName = table.get(rowNo, 1);
            // 物理名
            String tableNameStr = table.get(rowNo, 2);

            String name = String.format("%s・%s", tableNameStr, tableName);

            ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 5, workbook, "PCL (マスタ確認)", startRowNo4, copyCellStyle);
            ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(startRowNo4).getCell(5), name);
            startRowNo4++;
            addRowCount4++;
        }

        ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 6, workbook, "PCL (マスタ確認)", startRowNo4++, copyCellStyle);addRowCount4++;
        ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 7, workbook, "PCL (マスタ確認)", startRowNo4++, copyCellStyle);addRowCount4++;
        ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 8, workbook, "PCL (マスタ確認)", startRowNo4++, copyCellStyle);addRowCount4++;

        startRowNo = startRowNo3 + 4;
        endRowNo = startRowNo + 22 + addRowCount4;

        workbook.getSheet("PCL (マスタ確認)").addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));

        // 出力ＤＢ更新確認
        for (int i = 0; i < Y2Count; i++) {
            tmpStartColNo = 23;
            int tmpRowNo = endRowNo - 18;
            ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
        }


        // ＳＹＳＯＵＴログ 情報設定
        //   開始・終了メッセージ出力
        for (int i = 0; i < Y2Count; i++) {
            tmpStartColNo = 23;
            int tmpRowNo = endRowNo - 10;
            ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
        }

        // ＳＹＳＯＵＴログ 情報設定
        //   入出力件数出力
        for (int i = 0; i < Y2Count; i++) {
            tmpStartColNo = 23;
            int tmpRowNo = endRowNo - 7;
            ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
        }

        // PCL区分
        for (int i = 0; i < Y2Count; i++) {
            tmpStartColNo = 23;
            int tmpRowNo = endRowNo + 5;

            if (notTableList.contains(tmpStartColNo + i)) {
                ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), E);
            } else {
                ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), N);
            }
        }

        System.out.println("End update sheet「PCL (マスタ確認)」");

    }

    private static void setIudInfo(Workbook workbook, Workbook templateWorkbook, Table<Integer, Integer, String> table, List<Integer> iudList) {

        // シート「PCL (更新エラー確認)」情報を設定----------------------------------------------------------------------------

        int startRowNo5 = 10;
        int addRowCount5 = 0;
        int tmpCount5 = 1;
        int tmpStartColNo = 23;
        int Y3Count = 0;
        Multimap<String, Integer> tableNameMultimap5 = ArrayListMultimap.create();

        System.out.println("Start update sheet「PCL (更新エラー確認)」");
        for (int rowNo : iudList) {
            // 論理名
            String tableName = table.get(rowNo, 1);
            // 物理名
            String tableNameStr = table.get(rowNo, 2);
            // 操作区分
            String kubun = table.get(rowNo, 3);

            String name = String.format("%s・%s", tableNameStr, tableName);

            ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 1, workbook, "PCL (更新エラー確認)", startRowNo5, copyCellStyle);
            String stringValue = ExcelUtil.getStringValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(4)) + Def.zenkakuNumberMap.get(tmpCount5);
            ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(4), stringValue);
            startRowNo5++;
            tmpCount5++;
            addRowCount5++;

            ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 2, workbook, "PCL (更新エラー確認)", startRowNo5, copyCellStyle);
            ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(5), name);
            startRowNo5++;
            addRowCount5++;


            if (kubun.contains("I")) {
                ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 3, workbook, "PCL (更新エラー確認)", startRowNo5, copyCellStyle);
                ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(tmpStartColNo), Y);
                tableNameMultimap5.put(tableName, tmpStartColNo);
                addRowCount5++;
                startRowNo5++;
                Y3Count++;
                tmpStartColNo++;
            }

            if (kubun.contains("U")) {
                ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 4, workbook, "PCL (更新エラー確認)", startRowNo5, copyCellStyle);
                ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(tmpStartColNo), Y);
                tableNameMultimap5.put(tableName, tmpStartColNo);
                addRowCount5++;
                startRowNo5++;
                Y3Count++;
                tmpStartColNo++;
            }

            if (kubun.contains("D")) {
                ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 5, workbook, "PCL (更新エラー確認)", startRowNo5, copyCellStyle);
                ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(tmpStartColNo), Y);
                tableNameMultimap5.put(tableName, tmpStartColNo);
                addRowCount5++;
                startRowNo5++;
                Y3Count++;
                tmpStartColNo++;
            }

            ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 6, workbook, "PCL (更新エラー確認)", startRowNo5++, copyCellStyle);
            addRowCount5++;
        }

        int startRowNo = 9;
        int endRowNo = 16;
        int startColNo = 2;
        int endColNo = 3;
        endRowNo += addRowCount5;

        workbook.getSheet("PCL (更新エラー確認)").addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));


        // バッチのリターンコードが現・新で同じであること
        // 異常
        for (int i = 0; i < Y3Count; i++) {
            tmpStartColNo = 23;
            int tmpRowNo = endRowNo + 4;

            ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
        }

        // 出力ＤＢロールバック確認
        int startRowNo6 = 24 + addRowCount5;
        int addRowCount6 = 0;
        for (int rowNo : iudList) {

            // 論理名
            String tableName = table.get(rowNo, 1);
            // 物理名
            String tableNameStr = table.get(rowNo, 2);

            String name = String.format("%s・%s", tableNameStr, tableName);

            ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 7, workbook, "PCL (更新エラー確認)", startRowNo6, copyCellStyle);
            ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo6).getCell(5), name);

            List<Integer> colIndexList = Lists.newArrayList(tableNameMultimap5.get(tableName));
            for (Integer colIndex : colIndexList) {
                ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo6).getCell(colIndex), MARU);
            }

            startRowNo6++;
            addRowCount6++;
        }

        ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 8, workbook, "PCL (更新エラー確認)", startRowNo6++, copyCellStyle);addRowCount6++;
        ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 9, workbook, "PCL (更新エラー確認)", startRowNo6++, copyCellStyle);addRowCount6++;
        ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 10, workbook, "PCL (更新エラー確認)", startRowNo6++, copyCellStyle);addRowCount6++;

        startRowNo = endRowNo + 1;
        endRowNo = startRowNo + 27 + addRowCount6;

        workbook.getSheet("PCL (更新エラー確認)").addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));

        // 出力ＤＢ更新確認
        for (int i = 0; i < Y3Count; i++) {
            tmpStartColNo = 23;
            int tmpRowNo = endRowNo - 23;
            ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
        }

        // ＳＹＳＯＵＴログ 情報設定
        //   開始・終了メッセージ出力
        for (int i = 0; i < Y3Count; i++) {
            tmpStartColNo = 23;
            int tmpRowNo = endRowNo - 15;
            ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
        }

        // ＳＹＳＯＵＴログ 情報設定
        //   入出力件数出力
        for (int i = 0; i < Y3Count; i++) {
            tmpStartColNo = 23;
            int tmpRowNo = endRowNo - 12;
            ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
        }

        // PCL区分
        for (int i = 0; i < Y3Count; i++) {
            tmpStartColNo = 23;
            int tmpRowNo = endRowNo + 5;
            ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), E);
        }

        System.out.println("End update PCL sheet「(更新エラー確認)」");

    }

    private static void setSqlListInfo(Workbook workbook, Workbook templateWorkbook, String pgmId, String pgmName, List<String> sqlList) throws IOException {

        // SQL文一覧作成
        if (sqlList != null && !sqlList.isEmpty()) {
            Sheet tmpSheet = workbook.getSheet("SQL文一覧");
//			Sheet tmpSheet = workbook.createSheet("SQL文一覧");
//			ExcelUtil.createRow(tmpSheet, 0, Arrays.asList("№", "SQL文ファイル名", "テーブル対象", "CURD", "スクリプト", "テスト済"));
            // "№", "SQL文ファイル名", "テーブル対象", "CURD", "スクリプト", "テスト済"

            Map<String, String> sqlMap2 = getSqlMap(pgmId);
            if (sqlMap2 == null || sqlMap2.isEmpty()) {
                // SQL文ファイルがない場合
                for (int i = 0; i < sqlList.size(); i++) {
                    ExcelUtil.copyRow(templateWorkbook, "SQL文一覧", 0, workbook, "SQL文一覧", i + 1);

                    // "№"
                    int no = i + 1;
                    // SQL文ファイル名
                    String filename = "";
                    String[] strArray = sqlList.get(i).split("####");
                    // スクリプト
                    String sql = strArray[0];

                    // CURD
                    String curd = strArray[1];

//                    // スクリプト
//                    String sql = sqlList.get(i);
//                    String[] strArray = sql.split(" ");
//
//                    // CURD
//                    String curd = strArray[0];

                    // テーブル対象
                    List<String> tableList = new ArrayList<String>();
                    strArray = sql.split(" ");
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
                    colValueList.add("");

                    // №		テーブル対象	CURD	スクリプト	確認結果
                    List<Integer> colIndexList = Arrays.asList(1, 2, 3, 4, 5, 6);
                    ExcelUtil.setRowValue(tmpSheet.getRow(i + 1), colIndexList, colValueList);

                }

            } else {
                // SQL文ファイルがある場合

                int no = 0;
                List<List<String>> rowValueList = new ArrayList<List<String>>();
                for (Entry<String, String> entry : sqlMap2.entrySet()) {
                    // "№"
                    no++;

                    ExcelUtil.copyRow(templateWorkbook, "SQL文一覧", no, workbook, "SQL文一覧", no);

                    // SQL文ファイル名
                    String filename = entry.getKey();
                    // スクリプト
                    String sql = entry.getValue();
                    String[] strArray = sql.split(" ");

                    // CURD
                    String curd = strArray[0];

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
                    if (Strings.isNullOrEmpty(sql2)) {
                        colValueList.add(BATU);
                        colValueList.add(String.valueOf(no));
                        // D12_FE_CNT
                        // PBB08111.SQL_06_D02_SELECT_PROC.sql
                        colValueList.add(filename);
                        colValueList.add(null);
                        colValueList.add(null);
                        colValueList.add(null);
                        colValueList.add(null);
                    } else {
                        colValueList.add(MARU);
                        colValueList.add(String.valueOf(no));
                        // D12_FE_CNT
                        // PBB08111.SQL_06_D02_SELECT_PROC.sql
                        colValueList.add(filename);
                        colValueList.add(Joiner.on(", ").join(tableList));
                        colValueList.add(sql2.split("####")[1]);
                        colValueList.add(sql);
                        colValueList.add("");
                    }

//                    rowValueList.add(colValueList);

                    // 確認対象 №		テーブル対象	CURD	スクリプト	確認結果
                    List<Integer> colIndexList = Arrays.asList(0, 1, 2, 3, 4, 5, 6);
                    ExcelUtil.setRowValue(tmpSheet.getRow(no), colIndexList, colValueList);
                }

//                for (int i = 0; i < rowValueList.size(); i++) {
//                    List<String> list = rowValueList.get(i);
//
//                    String sql1 = list.get(4);
//
//                    if (sqlList.contains(sql1)) {
//                        ExcelUtil.createRow(tmpSheet, i + 1, list);
//                    } else {
//                        List<String> asList = Arrays.asList(BATU, list.get(1), list.get(2));
//                        ExcelUtil.createRow(tmpSheet, i + 1, asList);
//                    }
//
//
//                    ExcelUtil.createRow(tmpSheet, i + 1, list);
//                }
            }

        }
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


    private static Table<Integer, Integer, String> getPgmTable(String inPgmId) {
        System.out.println("Start read P_全SUB_ACCSESS_DB.xlsx");

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

                    startRowNo++;
                }
            }

        }

        System.out.println("End read P_全SUB_ACCSESS_DB.xlsx");
        return returnTable;
    }

    private static void createChangePgmP(String pgmId, String pgmName, String author, String createDate) throws IOException {
        System.out.println("Start create プログラム変更票。");

        Workbook workbook = ExcelUtil.getWorkbook(FILE_TEMPLATE_PGM_CHANGE);
        workbook.setSheetName(0, String.format("%s_%s", pgmId, pgmName));
        Sheet sheet = workbook.getSheetAt(0);
        // 作成者を設定
        ExcelUtil.setCellValue(sheet.getRow(2).getCell(36), String.format("CIT%s", author));

        // 作成日を設定
        ExcelUtil.setCellValue(sheet.getRow(2).getCell(47), createDate);

        // プログラムIDを設定
        ExcelUtil.setCellValue(sheet.getRow(7).getCell(6), pgmId);

        // プログラム名を設定
        ExcelUtil.setCellValue(sheet.getRow(9).getCell(6), pgmName);

        // BatchLoaderクラス名を設定
        String batchLoaderClassName = ExcelUtil.getStringValue(ExcelUtil.getCell(sheet, 15, 0));
        ExcelUtil.setCellValue(sheet.getRow(15).getCell(0), batchLoaderClassName.replaceAll("機能ID", pgmId));

        // 業務処理クラス名を設定
        String businessProcessClassName = ExcelUtil.getStringValue(ExcelUtil.getCell(sheet, 16, 2));
        ExcelUtil.setCellValue(sheet.getRow(16).getCell(2), businessProcessClassName.replaceAll("機能ID", pgmId));

        String filePath = String.format(OUTPUT_FILEPATH + File.separator + "%s_%s_プログラム変更票.xls", pgmId, pgmName);
        ExcelUtil.save(filePath, workbook);
        workbook.close();

        System.out.println(String.format("「%s」ファイルが保存しました。", filePath));
        System.out.println("End create プログラム変更票。");
    }

    private static List<String> getSqlList(String pgmId, String pgmName) {
        String filepath = String.format(COBOL_ANALYSIS_RESULT_BASE_FOLDER_FILEPATH + File.separator + COBOL_ANALYSIS_RESULT_FILENAME_FORMAT, pgmId, pgmName);
        System.out.println("Start read " + filepath);

        Table<Integer, Integer, String> table = null;
        try {
            table = ExcelUtil.getTable(filepath, "呼出階層");
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
