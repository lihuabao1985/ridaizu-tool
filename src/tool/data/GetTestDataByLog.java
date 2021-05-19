package tool.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Strings;

import common.Common;
import common.Def;
import common.ExcelUtil;

public class GetTestDataByLog implements TableDataOpt {

    public static final String KEYWORD_INFO_MAIN = "INFO 	[main]	";
    public static final String KEYWORD_INFO_ = "INFO   - ";
    public static final String tmpDENGYU = "=";
    public static final String tmpCOUNT = "COUNT";
    public static final String tmpFETCH = "FETCH";
    public static final String tmpSELECT = "SELECT";
    public static final String tmpUPDATE = "UPDATE";
    public static final String tmpINSERT = "INSERT";
    public static final String tmpDELETE = "DELETE";
    public static final String tmpHOSI = "*";
    public static final String tmpWHERE = "WHERE";

    public void exec(String[] args) throws IOException {
        System.out.println("処理開始。");

        String logFilePath = Def.SRC_NEW_LOG_COPY_TO_FILEPATH;
        String tableFilePath = Def.TABLE_DATA_FILEPATH;

        File logFile = new File(logFilePath);
        if (!logFile.exists()) {
            System.out.println("入力されたログパスは存在しません。 \r\n" + logFilePath);
            System.out.println("処理終了。");
            System.exit(0);
        }

        File tableFile = new File(tableFilePath);
        if (!tableFile.exists()) {
            System.out.println("入力されたテーブルデータパスは存在しません。 \r\n" + tableFilePath);
            System.out.println("処理終了。");
            System.exit(0);
        }

        Workbook workbook = ExcelUtil.getWorkbook(tableFilePath);

        Workbook destWorkbook = ExcelUtil.getWorkbook();
        Sheet destSheet = destWorkbook.createSheet();
        int startRowIndex = 0;
        setDestWorkbook(workbook, destWorkbook, destSheet, startRowIndex, logFilePath);

        ExcelUtil.save(Def.SRC_TESTDATA_FILEPATH, destWorkbook);
        System.out.println(String.format("ファイル「%s」が保存されました。", Def.SRC_TESTDATA_FILEPATH));

        System.out.println("処理終了。");
    }

    public int setDestWorkbook(Workbook workbook, Workbook destWorkbook, Sheet destSheet, int startRowIndex, String logFilePath) throws IOException {
        List<String> destTableNameList = getDestTableNameList(workbook, logFilePath);

        for (String destTableName : destTableNameList) {
            System.out.println(String.format("テーブル「%s」書き込み開始。", destTableName));

            Sheet sheet = workbook.getSheet(destTableName);
            String tableNameStr = ExcelUtil.getStringValue(ExcelUtil.getCell(sheet, Def.TABLE_NAME_ROW_NO, 4));

            ExcelUtil.setRowValue(destSheet, startRowIndex, 0, String.format("%s・%s", tableNameStr, destTableName));
            ExcelUtil.setCellFontBold(destWorkbook, ExcelUtil.getCell(destSheet, startRowIndex, 0), (short)11);
            startRowIndex++;

            ExcelUtil.copyRow(workbook, destTableName, Def.COLUMN_NAME_ROW_NO, destWorkbook, startRowIndex, false, false);
            Row destRow = destSheet.getRow(startRowIndex);
            short lastCellNum = destRow.getLastCellNum();
            for (int i = 0; i < lastCellNum; i++) {
                ExcelUtil.setCellFontBold(destWorkbook, ExcelUtil.getCell(destSheet, startRowIndex, i), (short)11);

            }
            startRowIndex++;

            Sheet srcSheet = workbook.getSheet(destTableName);
            int startRowNum = Def.DATA_START_ROW_NO;
            int lastRowNum = srcSheet.getLastRowNum();

            for (int i = startRowNum; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row ==null) {
                    sheet.createRow(i);
                }

                String cellValue = ExcelUtil.getStringValue(ExcelUtil.getCell(sheet, i, 0));

                if (!Strings.isNullOrEmpty(cellValue)) {
                    ExcelUtil.copyRow(workbook, destTableName, i, destWorkbook, startRowIndex++, false, false);
                }
            }

            startRowIndex++;

            System.out.println(String.format("テーブル「%s」書き込み終了。", destTableName));
        }

        return startRowIndex;
    }

    private List<String> getDestTableNameList(Workbook workbook, String logFilePath) throws IOException {
        List<String> selectTableNameList = getSelectTableNameList(logFilePath);

        List<String> destTableNameList = new ArrayList<String>();
        for (String selectTableName : selectTableNameList) {
            Sheet sheet = workbook.getSheet(selectTableName);
            if (sheet != null) {
                int startRowNum = Def.DATA_START_ROW_NO;
                int lastRowNum = sheet.getLastRowNum();

                boolean isExistData = false;

                for (int i = startRowNum; i <= lastRowNum; i++) {
                    if(sheet.getRow(i) == null) {
                        continue;
                    }

                    String cellValue = ExcelUtil.getStringValue(ExcelUtil.getCell(sheet, i, 0));

                    if (!Strings.isNullOrEmpty(cellValue)) {
                        isExistData = true;
                        break;
                    }
                }

                if (isExistData) {
                    destTableNameList.add(selectTableName);
                    System.out.println(selectTableName);
                }
            }
        }

        return destTableNameList;
    }

    private List<String> getSelectTableNameList(String filepath) throws IOException {

        Path path = Paths.get(filepath);
        List<String> lines = Common.readAllLines(path);

        List<String> selectTableNameList = new ArrayList<String>();

        for (String line : lines) {

            // SELECT
            if (line.contains(tmpSELECT) && line.contains(tmpWHERE)) {

                if (line.contains(KEYWORD_INFO_)) {
                    line = line.substring(line.indexOf(KEYWORD_INFO_)+ KEYWORD_INFO_.length());
                } else if (line.contains(KEYWORD_INFO_MAIN)) {
                    line = line.substring(line.indexOf(KEYWORD_INFO_MAIN)+ KEYWORD_INFO_MAIN.length());
                }

                line = Common.clearSpace(line);
                String tableName = null;
                String where = line.substring(line.lastIndexOf(tmpWHERE) + tmpWHERE.length());

                String[] strArray = line.split(" ");
                // テーブル対象
                for (String str : strArray) {
                    // PS, PT, PV
                    str = str.replaceAll("\"", "");
                    if (str.startsWith("PS") || str.startsWith("PT") || str.startsWith("PV")) {
                        tableName = str;
                    }
                }

                System.out.println(String.format("%s\t%s\t%s", tableName, where, line));

                if (!selectTableNameList.contains(tableName)) {
                    selectTableNameList.add(tableName);
                }
            }

        }

        return selectTableNameList;
    }
}
