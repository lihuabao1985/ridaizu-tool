package tool.backup.datav1;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import common.Common;
import common.Def;
import common.ExcelUtil;
import dao.ExecDao;
import test.Diff;

public class CreateEvidence {

    private static final String OUTPUT_FILEPATH = Def.DEST_EVIDENCE_FILENAME;
    private static final String tmpDENGYU = "=";
    private static final String tmpCOUNT = "COUNT";
    private static final String tmpFETCH = "FETCH";
    private static final String tmpSELECT = "SELECT";
    private static final String tmpUPDATE = "UPDATE";
    private static final String tmpINSERT = "INSERT";
    private static final String tmpDELETE = "DELETE";
    private static final String tmpHOSI = "*";
    private static final String tmpWHERE = "WHERE";
    private static final String KEYWORD_INFO_MAIN = "INFO 	[main]	";
    private static final String KEYWORD_INFO_ = "INFO   - ";
    private static final String COLUMN_NAME_CREATE_TIME = "作成時間";
    private static final String COLUMN_NAME_UPDATE_TIME = "更新時間";

    private static List<Diff> logDiffList = new ArrayList<Diff>();
    private static List<Diff> tableDiffList = new ArrayList<Diff>();


    public static void main(String[] args) throws Exception {
        System.out.println("エビデンス作成開始。");

        File dataFile = new File(Def.DEST_BASE_DIR);
        if (!dataFile.exists()) {
            System.out.println("指定されたフォルダは存在しません。" + Def.DEST_BASE_DIR);
            System.out.println("処理終了。");
            System.exit(0);
        }

        File[] listFiles = dataFile.listFiles();
        if (listFiles.length == 0) {
            System.out.println("指定されたフォルダにはデータが存在しません。" + Def.DEST_BASE_DIR);
            System.out.println("処理終了。");
            System.exit(0);
        }

        Workbook templateWorkbook = ExcelUtil.getWorkbook();
        ExecDao dao = new ExecDao();

        for (File file : listFiles) {
            System.out.println(String.format("エビデンス「%s」処理開始。", file.getName()));
            exce(templateWorkbook, file.getAbsolutePath(), dao);
            System.out.println(String.format("エビデンス「%s」処理終了。", file.getName()));
        }

        ExcelUtil.save(OUTPUT_FILEPATH, templateWorkbook);
        templateWorkbook.close();
        System.out.println(String.format("ファイル「%s」が保存されました。", OUTPUT_FILEPATH));

        System.out.println("エビデンス作成終了。");
    }

    public static void exce(Workbook templateWorkbook, String filepath, ExecDao dao) throws IOException, SQLException {
        String testcaseId = Def.TESTCASE_NO;

        Workbook dataWorkbook = ExcelUtil.getWorkbook(Def.DEST_TESTDATA_FILEPATH);
        Sheet templateSheet = templateWorkbook.createSheet(testcaseId);

        int logStartRowNo = 1;

        ExcelUtil.setRowValue(templateSheet, logStartRowNo, 0, "事前準備データ");
        ExcelUtil.setCellFontBold(templateWorkbook, ExcelUtil.getCell(templateSheet, logStartRowNo, 0));
        logStartRowNo += 2;

        ExcelUtil.setRowValue(templateSheet, logStartRowNo, 0, "入力DB");
        ExcelUtil.setCellFontBold(templateWorkbook, ExcelUtil.getCell(templateSheet, logStartRowNo, 0));

        System.out.println("テストデータ書き込み開始。");
        Table<Integer, Integer, String> table = ExcelUtil.getTable(dataWorkbook, 0);
        int rowSize = table.rowKeySet().size();
        int templateStartRowNo = 4;
        for (int rowNo = 0; rowNo < rowSize; rowNo++) {
            ExcelUtil.copyRow(dataWorkbook, rowNo, templateWorkbook, testcaseId, templateStartRowNo + rowNo);
        }
        System.out.println("テストデータ書き込み終了。");

        logStartRowNo += rowSize + 2;
        ExcelUtil.setRowValue(templateSheet, logStartRowNo, 0, "実施後出力");
        ExcelUtil.setCellFontBold(templateWorkbook, ExcelUtil.getCell(templateSheet, logStartRowNo, 0));
        logStartRowNo++;

        ExcelUtil.setRowValue(templateSheet, logStartRowNo, 0, "現行ログ SYSOUT.TXT");
        ExcelUtil.setCellFontBold(templateWorkbook, ExcelUtil.getCell(templateSheet, logStartRowNo, 0));

        logStartRowNo = templateStartRowNo + rowSize + 4;

        System.out.println("現行ログ書き込み開始。");
        List<String> oldStatisticsInfoList = getOldStatisticsInfoList();
        List<Integer> oldLogDiffIndexList = getOldLogDiffIndexList();

        for (int i = 0; i < oldStatisticsInfoList.size(); i++) {
            String line = oldStatisticsInfoList.get(i);
            ExcelUtil.setRowValue(templateSheet, logStartRowNo + i, 0, line);
        }

        List<Diff> oldLogDiffList = new ArrayList<Diff>();

        for (Integer oldLogDiffIndex : oldLogDiffIndexList) {
            ExcelUtil.setCellFontColor(templateWorkbook, ExcelUtil.getCell(templateSheet, logStartRowNo + oldLogDiffIndex, 0), Font.COLOR_RED);

            Diff diff = new Diff();
            diff.setOldValue(ExcelUtil.getStringValue(ExcelUtil.getCell(templateSheet, logStartRowNo + oldLogDiffIndex, 0)));
            diff.setOldDiffRowIndex(logStartRowNo + oldLogDiffIndex);
            oldLogDiffList.add(diff);
        }

        System.out.println("現行ログ書き込み終了。");

        logStartRowNo = logStartRowNo + oldStatisticsInfoList.size() + 2;
        ExcelUtil.setRowValue(templateSheet, logStartRowNo, 0, "新側ログ");
        ExcelUtil.setCellFontBold(templateWorkbook, ExcelUtil.getCell(templateSheet, logStartRowNo, 0));
        logStartRowNo++;

        System.out.println("新規ログ書き込み開始。");
        List<String> newStatisticsInfoList = getNewLogInfoList();
        List<Integer> newLogDiffIndexList = getNewLogDiffIndexList();

        for (int i = 0; i < newStatisticsInfoList.size(); i++) {
            String line = newStatisticsInfoList.get(i);
            ExcelUtil.setRowValue(templateSheet, logStartRowNo + i, 0, line);
        }

        List<Diff> newLogDiffList = new ArrayList<Diff>();

        for (Integer newLogDiffIndex : newLogDiffIndexList) {
            ExcelUtil.setCellFontColor(templateWorkbook, ExcelUtil.getCell(templateSheet, logStartRowNo + newLogDiffIndex, 0), Font.COLOR_RED);

            Diff diff = new Diff();
            diff.setNewValue(ExcelUtil.getStringValue(ExcelUtil.getCell(templateSheet, logStartRowNo + newLogDiffIndex, 0)));
            diff.setNewDiffRowIndex(logStartRowNo + newLogDiffIndex);
            newLogDiffList.add(diff);
        }
        System.out.println("新規ログ書き込み終了。");

        for (int i = 0; i < oldLogDiffList.size(); i++) {
            Diff oldDiff = oldLogDiffList.get(i);
            Diff newDiff = newLogDiffList.get(i);

            Diff diff = new Diff();
            diff.setOldValue(oldDiff.getOldValue());
            diff.setOldDiffRowIndex(oldDiff.getOldDiffRowIndex());
            diff.setNewValue(newDiff.getNewValue());
            diff.setNewDiffRowIndex(newDiff.getNewDiffRowIndex());

            logDiffList.add(diff);
        }

        logStartRowNo = logStartRowNo + newStatisticsInfoList.size() + 2;

        // 実施後出力DB
        ExcelUtil.setRowValue(templateSheet, logStartRowNo, 0, "実施後出力DB");
        ExcelUtil.setCellFontBold(templateWorkbook, ExcelUtil.getCell(templateSheet, logStartRowNo, 0));
        logStartRowNo++;

        // 現・新比較結果
        ExcelUtil.setRowValue(templateSheet, logStartRowNo, 0, "現・新比較結果");
        ExcelUtil.setCellFontBold(templateWorkbook, ExcelUtil.getCell(templateSheet, logStartRowNo, 0));
        logStartRowNo++;

        System.out.println("テーブルデータ書き込み開始。");
        List<File> newDataFileList = getNewDataFileList(filepath, testcaseId);
        List<File> oldDataFileList = getOldDataFileList(filepath, testcaseId);

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

        for (int i = 0; i < newDataFileList.size(); i++) {
            File file = newDataFileList.get(i);

            String filename = file.getName();
            String tableName = filename.replaceAll(".xlsx", "");
            String tableComment = dao.getTableComment(tableName);

            File oldFile = getOldFile(oldDataFileList, filename);
            Table<Integer, Integer, String> tmpTable = ExcelUtil.getTable(file.getAbsolutePath());
            Table<Integer, Integer, String> oldTable = ExcelUtil.getTable(oldFile.getAbsolutePath());

            ExcelUtil.setRowValue(templateSheet, logStartRowNo + i, 0, String.format("%s・%s", tableComment, tableName));
            ExcelUtil.setCellFontBold(templateWorkbook, ExcelUtil.getCell(templateSheet, logStartRowNo + i, 0), (short)11);
            logStartRowNo++;

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
                        ExcelUtil.setCellFontColor(templateWorkbook, cell, Font.COLOR_RED);

                        String columnName = ExcelUtil.getStringValue(ExcelUtil.getCell(templateSheet, rowIndex, 0));
                        if (COLUMN_NAME_CREATE_TIME.equals(columnName) || COLUMN_NAME_UPDATE_TIME.equals(columnName)) {

                            Row row = templateSheet.getRow(rowIndex);
                            for (int tmpColIndex = 0; tmpColIndex < row.getLastCellNum(); tmpColIndex++) {
                                ExcelUtil.setFillForegroundColor(templateWorkbook, ExcelUtil.getCell(templateSheet, rowIndex, tmpColIndex), IndexedColors.GREY_50_PERCENT);
                            }

                            ExcelUtil.setCellValue(ExcelUtil.getCell(templateSheet, rowIndex, row.getLastCellNum()), "確認対象外");
                        }

                        Diff diff = new Diff();
                        diff.setTableName(String.format("%s・%s", tableComment, tableName));
                        diff.setColumnName(columnName);
                        diff.setOldValue(ExcelUtil.getStringValue(ExcelUtil.getCell(templateSheet, rowIndex, colIndex - 2)));
                        diff.setNewValue(ExcelUtil.getStringValue(ExcelUtil.getCell(templateSheet, rowIndex, colIndex - 1)));
                        diff.setOldDiffRowIndex(rowIndex);

                        tableDiffList.add(diff);
                    }
                }

            }
        }

        System.out.println("テーブルデータ書き込み終了。");
        System.out.println("差分データ書き込み開始。");

        Sheet newSheet = templateWorkbook.createSheet("不一致統計");

        int diffStartRowNo = 1;
        ExcelUtil.setRowValue(newSheet, diffStartRowNo, 0, "ログ差分");
        ExcelUtil.setCellFontBold(templateWorkbook, ExcelUtil.getCell(newSheet, diffStartRowNo, 0));
        diffStartRowNo++;

        ExcelUtil.setRowValue(newSheet, diffStartRowNo++, Arrays.asList(0, 1), Arrays.asList("現行ログ", "新規ログ"));
        for (Diff diff : logDiffList) {
            String newValue = diff.getNewValue();
            if (newValue.contains(KEYWORD_INFO_)) {
                newValue = newValue.substring(newValue.indexOf(KEYWORD_INFO_)+ KEYWORD_INFO_.length());
            } else if (newValue.contains(KEYWORD_INFO_MAIN)) {
                newValue = newValue.substring(newValue.indexOf(KEYWORD_INFO_MAIN)+ KEYWORD_INFO_MAIN.length());
            }

            ExcelUtil.setRowValue(newSheet, diffStartRowNo++, Arrays.asList(0, 1), Arrays.asList(diff.getOldValue(), newValue));
        }

        diffStartRowNo++;

        ExcelUtil.setRowValue(newSheet, diffStartRowNo, 0, "テーブルデータ差分");
        ExcelUtil.setCellFontBold(templateWorkbook, ExcelUtil.getCell(newSheet, diffStartRowNo, 0));
        diffStartRowNo++;

        ExcelUtil.setRowValue(newSheet, diffStartRowNo++, Arrays.asList(0, 1, 2, 3), Arrays.asList("テーブル名", "カラム名", "現行値", "新規値"));
        for (Diff diff : tableDiffList) {
            ExcelUtil.setRowValue(newSheet, diffStartRowNo++, Arrays.asList(0, 1, 2, 3), Arrays.asList(diff.getTableName(), diff.getColumnName(), diff.getOldValue(), diff.getNewValue()));
        }

        System.out.println("差分データ書き込み終了。");
    }

    private static File getOldFile(List<File> fileList, String filename) {
        for (File file : fileList) {
            if (file.getName().equals(filename)) {
                return file;
            }
        }

        return null;
    }

    private static List<File> getNewDataFileList(String basePath, String testcaseId) {

        File file = new File(basePath + File.separator + testcaseId + "_updated_data_new");
        if (!file.exists()) {
            return null;
        }

        return Arrays.asList(file.listFiles());
    }

    private static List<File> getOldDataFileList(String basePath, String testcaseId) {

        File file = new File(basePath + File.separator + testcaseId + "_updated_data_old");
        if (!file.exists()) {
            return null;
        }

        return Arrays.asList(file.listFiles());
    }

    private static List<Integer> getOldLogDiffIndexList() {

        return Lists.newArrayList(getLogDiffMap().values());
    }

    private static List<Integer> getNewLogDiffIndexList() {

        List<Integer> logDiffIndexList = Lists.newArrayList(getLogDiffMap().values());
        Map<Integer, Integer> newStatisticsIndexMap = getNewStatisticsIndexMap();

        List<Integer> indexList = new ArrayList<Integer>();

        for (int logDiffIndex : logDiffIndexList) {
            indexList.add(newStatisticsIndexMap.get(logDiffIndex));
        }

        return indexList;
    }

    private static Map<Integer, Integer> getNewStatisticsIndexMap() {

        Map<Integer, Integer> indexMap = new LinkedMap<Integer, Integer>();

        List<String> lines = getNewLogInfoList();

        int keyIndex = 1;
        int index = 1;
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

                if (line.contains(KEYWORD_INFO_)) {
                    line = line.substring(line.indexOf(KEYWORD_INFO_)+ KEYWORD_INFO_.length());
                } else if (line.contains(KEYWORD_INFO_MAIN)) {
                    line = line.substring(line.indexOf(KEYWORD_INFO_MAIN)+ KEYWORD_INFO_MAIN.length());
                }

                indexMap.put(keyIndex++, index);
            }

            index++;
        }

        return indexMap;
    }


    private static Map<String, Integer> getLogDiffMap() {
        List<String> newStatisticsInfoList = getNewStatisticsInfoList();
        List<String> oldStatisticsInfoList = getOldStatisticsInfoList();

        Map<String, Integer> diffMap = new HashedMap<String, Integer>();

        if (newStatisticsInfoList.size() != oldStatisticsInfoList.size()) {
            return diffMap;
        }

        for (int i = 0; i < oldStatisticsInfoList.size(); i++) {
            String newStatisticsInfo = newStatisticsInfoList.get(i);
            String oldStatisticsInfo = oldStatisticsInfoList.get(i);

            if (!newStatisticsInfo.trim().equals(oldStatisticsInfo.trim())) {
                diffMap.put(oldStatisticsInfo, i);
            }
        }

        return diffMap;
    }

    private static List<String> getNewStatisticsInfoList() {

        return getLogInfoList(Def.DEST_LOG_STATISTICS_FILEPATH);
    }

    private static List<String> getOldStatisticsInfoList() {

        return getLogInfoList(Def.DEST_POWER_SHELL_COPY_TO_LOCAL_FILEPATH);
    }

    private static List<String> getLogInfoList(String filepath) {

        File file = new File(filepath);
        if (!file.exists()) {
            return null;
        }

        List<String> readAllLines = Common.readAllLines(filepath);
        Iterables.removeIf(readAllLines, Predicates.isNull());
        return readAllLines;
    }

    private static List<String> getNewLogInfoList() {

        File file = new File(Def.DEST_NEW_LOG_COPY_TO_FILEPATH);
        if (!file.exists()) {
            return null;
        }

        return Common.readAllLines(Def.DEST_NEW_LOG_COPY_TO_FILEPATH);
    }


}
