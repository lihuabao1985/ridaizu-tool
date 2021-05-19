package test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import common.Common;
import common.Def;
import common.ExcelUtil;
import dao.ExecDao;

public class CreateEvidence_V1 {


    public static final String tmpDENGYU = "=";
    public static final String tmpCOUNT = "COUNT";
    public static final String tmpFETCH = "FETCH";
    public static final String tmpSELECT = "SELECT";
    public static final String tmpUPDATE = "UPDATE";
    public static final String tmpINSERT = "INSERT";
    public static final String tmpDELETE = "DELETE";
    public static final String tmpHOSI = "*";
    public static final String tmpWHERE = "WHERE";
    public static final List<String> tmpList = Arrays.asList("FETCH", "SELECT", "UPDATE", "INSERT", "DELETE", "=");


    public static void main(String[] args) throws Exception {
        Workbook dataWorkbook = ExcelUtil.getWorkbook("data\\00001_testdata.xlsx");
        Workbook templateWorkbook = ExcelUtil.getWorkbook("data\\template.xlsx");
        Sheet templateSheet = templateWorkbook.getSheetAt(0);

        Table<Integer, Integer, String> table = ExcelUtil.getTable(dataWorkbook, 0);
        int rowSize = table.rowKeySet().size();
        int templateStartRowNo = 4;
        for (int rowNo = 0; rowNo < rowSize; rowNo++) {
            ExcelUtil.copyRow(dataWorkbook, "00001", rowNo, templateWorkbook, templateStartRowNo + rowNo);
        }

        Table<Integer, Integer, String> diffToukeiTable = getDiffToukeiTable();

        int logStartRowNo = templateStartRowNo + rowSize + 4;

        Path path = Paths.get("data\\00001_log_old.txt");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            ExcelUtil.setRowValue(templateSheet, logStartRowNo + i, 0, line);

            if (checkData(diffToukeiTable, line)) {
                setCellFontColor(templateWorkbook, ExcelUtil.getCell(templateSheet, logStartRowNo + i, 0), Font.COLOR_RED);
            }
        }

        logStartRowNo = logStartRowNo + lines.size() + 2;
        ExcelUtil.setRowValue(templateSheet, logStartRowNo++, 0, "新側ログ");

        path = Paths.get("data\\00001_log_new.txt");
        lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            ExcelUtil.setRowValue(templateSheet, logStartRowNo + i, 0, line);

            if (checkData(diffToukeiTable, line)) {
                setCellFontColor(templateWorkbook, ExcelUtil.getCell(templateSheet, logStartRowNo + i, 0), Font.COLOR_RED);
            }
        }

        logStartRowNo = logStartRowNo + lines.size() + 2;

        // 実施後出力DB
        ExcelUtil.setRowValue(templateSheet, logStartRowNo++, 0, "実施後出力DB");
        // 現・新比較結果
        ExcelUtil.setRowValue(templateSheet, logStartRowNo++, 0, "現・新比較結果");

//        // 解析新执行日志
//        analysisLog();
//
//        for (int rowNo : toukeiTable.rowKeySet()) {
//            String tableName = toukeiTable.get(rowNo, 0);
//            String optType = toukeiTable.get(rowNo, 1);
//            String count = toukeiTable.get(rowNo, 2);
//
//            System.out.println(String.format("%s\t%s\tCOUNT\t=\t%s", tableName, optType, count));
//
//            ExcelUtil.setRowValue(templateSheet, logStartRowNo + rowNo, 0, tableName);
//            logStartRowNo++;
//
//            // 現行DB情報	新側DB情報	現・新DB比較結果
//            ExcelUtil.setRowValue(templateSheet, logStartRowNo + rowNo, Arrays.asList(0, 1, 2, 3), Arrays.asList("", "現行DB情報", "新側DB情報", "現・新DB比較結果"));
//            logStartRowNo++;
//
//            // 更新情報
//            if ("UPDATE".equals(optType)) {
//                ExcelUtil.setRowValue(templateSheet, logStartRowNo + rowNo, Arrays.asList(0, 1, 2, 3), Arrays.asList("", "更新情報", "更新情報", "更新情報"));
//            } else if ("INSERT".equals(optType)) {
//                ExcelUtil.setRowValue(templateSheet, logStartRowNo + rowNo, Arrays.asList(0, 1, 2, 3), Arrays.asList("", "登録情報", "登録情報", "登録情報"));
//            }
//            logStartRowNo++;
//
//            // 更新情報
//            ExcelUtil.setRowValue(templateSheet, logStartRowNo + rowNo, Arrays.asList(0, 1, 2, 3), Arrays.asList("", "", "", ""));
//            logStartRowNo++;
//
//        }

        List<File> newDataFileList = getNewDataFileList();
        List<File> oldDataFileList = getOldDataFileList();

        if (newDataFileList == null ||
            newDataFileList.size() == 0 ||
            oldDataFileList == null ||
            oldDataFileList.size() == 0) {

            // 現・新比較結果
            ExcelUtil.setRowValue(templateSheet, logStartRowNo++, 0, "DBが更新されていません。");
            ExcelUtil.save("template.xlsx", templateWorkbook);
            templateWorkbook.close();
            System.exit(0);
        }

        ExecDao dao = new ExecDao();

        for (int i = 0; i < newDataFileList.size(); i++) {
            File file = newDataFileList.get(i);

            String filename = file.getName();
            String tableName = filename.replaceAll(".xlsx", "");
            String tableComment = dao.getTableComment(tableName);

            File oldFile = getOldFile(oldDataFileList, filename);
            Table<Integer, Integer, String> tmpTable = ExcelUtil.getTable(file.getAbsolutePath());
            Table<Integer, Integer, String> oldTable = ExcelUtil.getTable(oldFile.getAbsolutePath());

            ExcelUtil.setRowValue(templateSheet, logStartRowNo + i, 0, String.format("%s・%s", tableComment, tableName));
            logStartRowNo++;


            // 更新情報
//            if ("UPDATE".equals(optType)) {
//                ExcelUtil.setRowValue(templateSheet, logStartRowNo + rowNo, Arrays.asList(0, 1, 2, 3), Arrays.asList("", "更新情報", "更新情報", "更新情報"));
//            }
//            else if ("INSERT".equals(optType)) {
//                ExcelUtil.setRowValue(templateSheet, logStartRowNo + rowNo, Arrays.asList(0, 1, 2, 3), Arrays.asList("", "登録情報", "登録情報", "登録情報"));
//            }

            // 現行DB情報	新側DB情報	現・新DB比較結果
            List<Integer> headerColIndexList = new ArrayList<Integer>();
            headerColIndexList.add(0);
            headerColIndexList.add(1);
            headerColIndexList.add(1 + oldTable.rowKeySet().size() - 1);
            headerColIndexList.add(1 + oldTable.rowKeySet().size() + tmpTable.rowKeySet().size() - 1 - 1);
            ExcelUtil.setRowValue(templateSheet, logStartRowNo + i, headerColIndexList, Arrays.asList("", "現行DB情報", "新側DB情報", "現・新DB比較結果"));
            logStartRowNo++;


            List<Integer> headerColIndexList2 = new ArrayList<Integer>();
            List<String> headerColValueList = new ArrayList<String>();
            headerColValueList.add("");

            int headerLength = 1 + oldTable.rowKeySet().size() + tmpTable.rowKeySet().size() - 1 - 1;
            for (int j = 0; j < headerLength; j++) {
                headerColIndexList2.add(j);
                headerColValueList.add("更新情報");
            }

            ExcelUtil.setRowValue(templateSheet, logStartRowNo + i, headerColIndexList2, headerColValueList);
            logStartRowNo++;


            // 更新情報
            List<List<String>> columnValueList = new ArrayList<List<String>>();
            //            for (int rowNo : tmpTable.rowKeySet()) {

            for (int colNo : tmpTable.columnKeySet()) {

                int tmpRowSize = tmpTable.rowKeySet().size();

                List<String> valueList = new ArrayList<String>();
                valueList.add(tmpTable.get(0, colNo));


                for (int j = 1; j < tmpRowSize; j++) {
                    valueList.add(oldTable.get(j, colNo));
                }

                for (int j = 1; j < tmpRowSize; j++) {
                    valueList.add(tmpTable.get(j, colNo));
                }

                columnValueList.add(valueList);
            }

            //            }

            Multimap<Integer, Integer> rowColIndexMultimap = ArrayListMultimap.create();

            for (List<String> list : columnValueList) {

                List<Integer> colNoList = new ArrayList<Integer>();
                for (int j = 0; j < list.size(); j++) {
                    colNoList.add(j);
                }

                ExcelUtil.setRowValue(templateSheet, logStartRowNo + i, colNoList, list);

                int startColNo = colNoList.size();
                int dataCount = (list.size() - 1) / 2;

                List<Integer> compareColIndexList = new ArrayList<Integer>();
                List<String> compareColValueList = new ArrayList<String>();

                for (int j = 0; j < dataCount; j++) {
                    String colIndexName1 = Common.num2alphabet(2 + j);
                    String colIndexName2 = Common.num2alphabet(2 + dataCount + j);

//                    // =IF(B421=C421, "OK", "NG")
//                    ExcelUtil.setCellFormula(ExcelUtil.getCell(templateSheet, logStartRowNo + i, startColNo + j),
//                                                String.format("IF(%s%s=%s%s, \"OK\", \"NG\")", colIndexName1, logStartRowNo + i + 1, colIndexName2, logStartRowNo + i + 1));

                    compareColIndexList.add(startColNo + j);
                    compareColValueList.add(String.format("IF(%s%s=%s%s, \"OK\", \"NG\")", colIndexName1, logStartRowNo + i + 1, colIndexName2, logStartRowNo + i + 1));

                    rowColIndexMultimap.put(logStartRowNo + i, startColNo + j);
                }

                ExcelUtil.setCellFormula(templateSheet, logStartRowNo + i, compareColIndexList, compareColValueList);
                logStartRowNo++;
            }


            Map<Integer, Collection<Integer>> rowColIndexMap = rowColIndexMultimap.asMap();
            for (Entry<Integer, Collection<Integer>> entry : rowColIndexMap.entrySet()) {
                Integer rowIndex = entry.getKey();
                List<Integer> colIndexList = Lists.newArrayList(entry.getValue());

                for (Integer colIndex : colIndexList) {
                    Cell cell = ExcelUtil.getCell(templateSheet, rowIndex, colIndex);
                    String value = ExcelUtil.getStringValue(cell);

                    if (Def.NG.equals(value)) {
                        setCellFontColor(templateWorkbook, cell, Font.COLOR_RED);
                    }
                }
            }

        }


        ExcelUtil.save("template.xlsx", templateWorkbook);
        templateWorkbook.close();
    }

    private static File getOldFile(List<File> fileList, String filename) {
        for (File file : fileList) {
            if (file.getName().equals(filename)) {
                return file;
            }
        }

        return null;
    }

    public static boolean checkData(Table<Integer, Integer, String> diffToukeiTable, String line) {

        if (Strings.isNullOrEmpty(line) || diffToukeiTable.rowKeySet().size() == 0) {
            return false;
        }

        boolean b = false;

        // FETCH
        if (line.contains(tmpFETCH) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
            b = true;
        }

        // SELECT
        if (line.contains(tmpSELECT) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
            if (!line.contains(tmpHOSI) && !line.contains(tmpWHERE))
            b = true;
        }

        // INSERT
        if (line.contains(tmpINSERT) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
            b = true;
        }

        // UPDATE
        if (line.contains(tmpUPDATE) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
            b = true;
        }

        // DELETE
        if (line.contains(tmpDELETE) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
            b = true;
        }

        if (!b) {
            return false;
        }

        String tmpline = line;
        if (line.contains("INFO   - ")) {
            tmpline = line.substring(line.indexOf("INFO   - ") + "INFO   - ".length());
        }

        tmpline = clearSpace(tmpline);
        String[] valueArray = tmpline.split(" ");
        String tmpTableName = valueArray[0];
        String tmpOptType = valueArray[1];

        for (int newRowNo : diffToukeiTable.rowKeySet()) {
            String newTableName = diffToukeiTable.get(newRowNo, 0);
            String newOptType = diffToukeiTable.get(newRowNo, 1);

            if (newTableName.equals(tmpTableName) && newOptType.equals(tmpOptType)) {
                return true;
            }
        }

        return false;
    }

    private static Table<Integer, Integer, String> getDiffToukeiTable() throws IOException {
        Table<Integer, Integer, String> newToukeiTable = getNewToukeiTable();
        Table<Integer, Integer, String> oldToukeiTable = getOldToukeiTable();
        return checkToukeiTable(newToukeiTable, oldToukeiTable);
    }

    private static Table<Integer, Integer, String> getNewToukeiTable() throws IOException {

        Table<Integer, Integer, String> toukeiTable = HashBasedTable.create();

        Path path = Paths.get("data\\00001_log_new.txt");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        int toukeiRowNo = 0;

        for (String line : lines) {

            boolean b = false;

            // FETCH
            if (line.contains(tmpFETCH) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
                b = true;
            }

            // SELECT
            if (line.contains(tmpSELECT) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
                if (!line.contains(tmpHOSI) && !line.contains(tmpWHERE))
                b = true;
            }

            // INSERT
            if (line.contains(tmpINSERT) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
                b = true;
            }

            // UPDATE
            if (line.contains(tmpUPDATE) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
                b = true;
            }

            // DELETE
            if (line.contains(tmpDELETE) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
                b = true;
            }

            if (b) {
                line = line.substring(line.indexOf("INFO   - ") + "INFO   - ".length());
                line = clearSpace(line);
                String[] valueArray = line.split(" ");
                toukeiTable.put(toukeiRowNo, 0, valueArray[0]);
                toukeiTable.put(toukeiRowNo, 1, valueArray[1]);
                toukeiTable.put(toukeiRowNo, 2, valueArray[4]);
                toukeiRowNo++;
            }
        }

        return toukeiTable;
    }

    private static Table<Integer, Integer, String> getOldToukeiTable() throws IOException {
        Table<Integer, Integer, String> toukeiTable = HashBasedTable.create();

        Path path = Paths.get("data\\00001_log_old.txt");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        int toukeiRowNo = 0;

        for (String line : lines) {
            line = clearSpace(line);
            String[] valueArray = line.split(" ");
            toukeiTable.put(toukeiRowNo, 0, valueArray[0]);
            toukeiTable.put(toukeiRowNo, 1, valueArray[1]);
            toukeiTable.put(toukeiRowNo, 2, valueArray[4]);
            toukeiRowNo++;
        }

        return toukeiTable;
    }

    private static Table<Integer, Integer, String> checkToukeiTable(Table<Integer, Integer, String> newToukeiTable, Table<Integer, Integer, String> oldToukeiTable) {
        Table<Integer, Integer, String> table = HashBasedTable.create();

        int startRowNo = 0;

        for (int newRowNo : newToukeiTable.rowKeySet()) {
            String newTableName = newToukeiTable.get(newRowNo, 0);
            String newOptType = newToukeiTable.get(newRowNo, 1);
            String newCount = newToukeiTable.get(newRowNo, 2);

            boolean b = false;

            for (int oldRowNo : oldToukeiTable.rowKeySet()) {
                String oldTableName = oldToukeiTable.get(oldRowNo, 0);
                String oldOptType = oldToukeiTable.get(oldRowNo, 1);
                String oldCount = oldToukeiTable.get(oldRowNo, 2);

                if (newTableName.equals(oldTableName) && newOptType.equals(oldOptType)) {
                    b = newCount.equals(oldCount);
                    break;
                }
            }

            if (!b) {
                table.put(startRowNo, 0, newTableName);
                table.put(startRowNo, 1, newOptType);
                table.put(startRowNo, 2, newCount);
                startRowNo++;
            }
        }

        startRowNo = table.rowKeySet().size();

        for (int oldRowNo : oldToukeiTable.rowKeySet()) {
            String oldTableName = oldToukeiTable.get(oldRowNo, 0);
            String oldOptType = oldToukeiTable.get(oldRowNo, 1);
            String oldCount = oldToukeiTable.get(oldRowNo, 2);

            boolean b = false;

            for (int newRowNo : newToukeiTable.rowKeySet()) {
                String newTableName = newToukeiTable.get(newRowNo, 0);
                String newOptType = newToukeiTable.get(newRowNo, 1);
                String newCount = newToukeiTable.get(newRowNo, 2);

                if (newTableName.equals(oldTableName) && newOptType.equals(oldOptType)) {
                    b = newCount.equals(oldCount);
                    break;
                }
            }

            if (!b) {

                boolean b2 = false;

                for (int rowNo : table.rowKeySet()) {
                    String tableName = table.get(rowNo, 0);
                    String optType = table.get(rowNo, 1);
                    String count = table.get(rowNo, 2);

                    if (tableName.equals(oldTableName) && optType.equals(oldOptType) && count.equals(oldCount)) {
                        b = true;
                        break;
                    }
                }

                if (!b2) {
                    table.put(startRowNo, 0, oldTableName);
                    table.put(startRowNo, 1, oldOptType);
                    table.put(startRowNo, 2, oldCount);
                    startRowNo++;
                }
            }
        }

        return table;
    }

    private static String clearSpace(String value) {
        String keyword = "  ";
        while(value.contains(keyword)) {
            value = value.replaceAll(keyword, " ");
        }

        return value;
    }

    private static void setCellFontColor(Workbook workbook, Cell cell, short color) {

        //フォントを定義する
        Font font = workbook.createFont();
        // 色
        font.setColor(color);

        //セルのフォントを設定する
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        cell.setCellStyle(style);
    }

    private static List<File> getNewDataFileList() {

        File file = new File("data\\00001_testdata_new");
        if (!file.exists()) {
            return null;
        }

        return Arrays.asList(file.listFiles());
    }

    private static List<File> getOldDataFileList() {

        File file = new File("data\\00001_testdata_old");
        if (!file.exists()) {
            return null;
        }

        return Arrays.asList(file.listFiles());
    }

}
