package tool.backup.datav1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

        Scanner sc = new Scanner(System.in);
        System.out.println("ログファイルパスとテーブルデータファイルをスペース区切りで入力してください。\r\n例：log.txt table.xlsx");
        String inLine = sc.nextLine();
        sc.close();

        if (Strings.isNullOrEmpty(inLine)) {
            System.out.println("処理終了。");
            System.exit(0);
        }

        String[] split = inLine.split(" ");
        String logFilePath = split[0];
        String tableFilePath = split[1];

        File logFile = new File(logFilePath);
        if (!logFile.exists()) {
            System.out.println("入力されたログパスは存在しません。");
            System.out.println("処理終了。");
            System.exit(0);
        }

        File tableFile = new File(tableFilePath);
        if (!tableFile.exists()) {
            System.out.println("入力されたテーブルデータパスは存在しません。");
            System.out.println("処理終了。");
            System.exit(0);
        }

        Workbook workbook = ExcelUtil.getWorkbook(tableFilePath);

        Workbook destWorkbook = ExcelUtil.getWorkbook();
        Sheet destSheet = destWorkbook.createSheet();

        // 00001_log_new.txt table.xlsx
        // 00001_log_new.txt table_kubun1.xlsx
        List<String> selectTableNameList = getSelectTableNameList(logFilePath);

        List<String> destTableNameList = new ArrayList<String>();
        for (String selectTableName : selectTableNameList) {
            Sheet sheet = workbook.getSheet(selectTableName);
            if (sheet != null) {
                int startRowNum = Def.DATA_START_ROW_NO;
                int lastRowNum = sheet.getLastRowNum();

                boolean isExistData = false;

                for (int i = startRowNum; i <= lastRowNum; i++) {
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

        int startRowIndex = 0;
        for (String destTableName : destTableNameList) {
            Sheet sheet = workbook.getSheet(destTableName);
            String tableNameStr = ExcelUtil.getStringValue(ExcelUtil.getCell(sheet, Def.TABLE_NAME_ROW_NO, 4));

            ExcelUtil.setRowValue(destSheet, startRowIndex++, 0, String.format("%s・%s", tableNameStr, destTableName));

            ExcelUtil.copyRow(workbook, destTableName, Def.COLUMN_NAME_ROW_NO, destWorkbook, startRowIndex++);

            Sheet srcSheet = workbook.getSheet(destTableName);
            int startRowNum = Def.DATA_START_ROW_NO;
            int lastRowNum = srcSheet.getLastRowNum();

            for (int i = startRowNum; i <= lastRowNum; i++) {
                String cellValue = ExcelUtil.getStringValue(ExcelUtil.getCell(sheet, i, 0));

                if (!Strings.isNullOrEmpty(cellValue)) {
                    ExcelUtil.copyRow(workbook, destTableName, i, destWorkbook, startRowIndex++, true, false);
                }
            }

            startRowIndex++;

        }

        ExcelUtil.save("output\\testdata.xlsx", destWorkbook);
        System.out.println("フォルダ「output」に保存しました。");

        System.out.println("処理終了。");
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
