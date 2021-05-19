package tool.hikinoukennsyou;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import common.Common;
import common.DateUtil;
import common.ExcelUtil;

public class Main {

    public static final String BASE_PATH = "C:\\Users\\li.huabao\\Desktop\\PJRD001D_管理帳票日次集計処理";

    public static final String DB1_FILENAME = "DB1.xlsx";

    public static final String DB2_FILENAME = "DB2.xlsx";

    public static final String RESULT_FILENAME = "result.txt";

    public static final String TIBIKKO_LOG_FILENAME = "SYSOUT.TXT";

    public static final String TIBIKKO_DATETIME_LOG_FILENAME = "log.txt";

    public static final String TIBIKKO_MEMORY_INFO_FILENAME = "AP_CPU_Memory_Disk_old.xlsx";

    private static int sgiExecPlanIndex = 1;

    private static int tibikkoExecPlanIndex = 1;

    private static final int HANDLE_TIME_MS = 1000;

    public static void main(String[] args) throws Exception {
//        setTibikkoInfo("PJRD001D_ちびっ子.xlsx");
//        setSGIInfo("PJRD001D_SGI.xlsx");
        summarize("PJRD001D.xlsx");
    }

    public static void summarize(String saveFilename) throws IOException {

        Workbook templateWorkbook = ExcelUtil.getWorkbook("template_hikinou.xlsx");
        Sheet sheet1 = templateWorkbook.getSheetAt(0);

        File sgiBaseFile = new File(BASE_PATH + File.separator + "SGI");
        File tibikkoBaseFile = new File(BASE_PATH + File.separator + "ちびっ子");

        File[] tibikkoListFiles = tibikkoBaseFile.listFiles();
        List<String> tibikkoPathList = new ArrayList<String>();
        for (File file : tibikkoListFiles) {
            if (file.isDirectory()) {
                tibikkoPathList.add(file.getAbsolutePath());
            }
        }

        File[] sgiListFiles = sgiBaseFile.listFiles();
        List<String> sgiPathList = new ArrayList<String>();
        for (File file : sgiListFiles) {
            if (file.isDirectory()) {
                sgiPathList.add(file.getAbsolutePath());
            }
        }


        // まとめ
        // ■実施時間
        List<String> rmiInitDatetimeList = new ArrayList<String>();
        int startRowIndex1 = 3;
        List<Integer> cellIndexList = Arrays.asList(0, 1, 2, 3, 4 ,5 ,6);
        for (int i = 0; i < tibikkoPathList.size(); i++) {
            List<String> cellValueList = new ArrayList<String>();
            cellValueList.add(String.format("Step%d", i + 1));

            String tibikkoBasePath = tibikkoPathList.get(i);

            List<String> tibikkoLogLineList = Common.readAllLines(tibikkoBasePath + File.separator + TIBIKKO_DATETIME_LOG_FILENAME);
            // 2021/02/22 22:59:38.63
            // ちびっ子 開始時間
            String tibikkoStartDatetime = tibikkoLogLineList.get(0).trim();
            // ちびっ子 終了時間
            String tibikkoEndDatetime = tibikkoLogLineList.get(4).trim();

            // ちびっ子 実施時間
            long tibikkoRunTime = DateUtil.stringToDate(DateUtil.LONG_DATE_SS, tibikkoEndDatetime).getTime() -
                                    DateUtil.stringToDate(DateUtil.LONG_DATE_SS, tibikkoStartDatetime).getTime();
            String tibikkoRunTimeStr = String.format("%.2f秒", tibikkoRunTime / 1000.0);

            String sgiBasePath = sgiPathList.get(i);
            File logFile = getLogFile(new File(sgiBasePath));
            List<String> sgiLogLineList = Common.readAllLines(logFile.getAbsolutePath());

            // SGI 開始時間
            String sgiStartDatetime = null;
            // SGI 終了時間
            String sgiEndDatetime = null;

            // RMIサーバ初期化 開始時間
            String rmiInitStartDatetime = null;
            // RMIサーバ初期化 終了時間
            String rmiInitEndDatetime = null;

            for (int j = 0; j < sgiLogLineList.size(); j++) {
                String line = sgiLogLineList.get(j);

                // SGI 開始時間
                if (line.contains("INFO") && sgiStartDatetime == null) {
                    String[] splitArray = Common.clearSpace(line).split(" ");
                    sgiStartDatetime = String.format("%s %s", splitArray[0], splitArray[1]);
                }

                // SGI 終了時間
                if (line.contains("処理時間")) {
                    String[] splitArray = Common.clearSpace(line).split(" ");
                    sgiEndDatetime = String.format("%s %s", splitArray[0], splitArray[1]);
                }

                // SGI RMIサーバ初期化時間
                if (line.contains("ロールバック対象")) {

                    String[] splitArray = Common.clearSpace(sgiLogLineList.get(j - 1)).split(" ");
                    rmiInitStartDatetime = String.format("%s %s", splitArray[0], splitArray[1]);

                    splitArray = Common.clearSpace(line).split(" ");
                    rmiInitEndDatetime = String.format("%s %s", splitArray[0], splitArray[1]);
                }
            }

            // SGI 実施時間
            long sgiRunTime = DateUtil.stringToDate(DateUtil.LONG_DATE_SS, sgiEndDatetime).getTime() -
                                DateUtil.stringToDate(DateUtil.LONG_DATE_SS, sgiStartDatetime).getTime();
            String sgiRunTimeStr = String.format("%.2f秒", sgiRunTime / 1000.0);

            cellValueList.add(tibikkoStartDatetime);
            cellValueList.add(tibikkoEndDatetime);
            cellValueList.add(tibikkoRunTimeStr);
            cellValueList.add(sgiStartDatetime);
            cellValueList.add(sgiEndDatetime);
            cellValueList.add(sgiRunTimeStr);

            ExcelUtil.copyRow(templateWorkbook, startRowIndex1, templateWorkbook, startRowIndex1 + 1);
            ExcelUtil.setRowValue(sheet1, startRowIndex1, cellIndexList, cellValueList);

            // SGI RMIサーバ初期化時間
            long rmiInitRunTime = DateUtil.stringToDate(DateUtil.LONG_DATE_SS, rmiInitEndDatetime).getTime() -
                    DateUtil.stringToDate(DateUtil.LONG_DATE_SS, rmiInitStartDatetime).getTime();
            String rmiInitRunTimeStr = String.format("%.2f秒", rmiInitRunTime / 1000.0);
            rmiInitDatetimeList.add(rmiInitRunTimeStr);

            startRowIndex1++;
        }

        startRowIndex1 = startRowIndex1 + 3;

        for (int i = 0; i < rmiInitDatetimeList.size(); i++) {
            String rmiInitDatetime = rmiInitDatetimeList.get(i);

            ExcelUtil.copyRow(templateWorkbook, startRowIndex1, templateWorkbook, startRowIndex1 + 1);
            ExcelUtil.setCellValue(ExcelUtil.getCell(sheet1, startRowIndex1, 2), String.format("Step%d：%s", i + 1, rmiInitDatetime));

            startRowIndex1++;
        }

        ExcelUtil.setCellValue(ExcelUtil.getCell(sheet1, startRowIndex1, 2), "");


        // ■メモリ
        startRowIndex1 = startRowIndex1 + 4;
        List<Integer> memoryColIndexList = Arrays.asList(0, 1, 2, 4, 5);
        for (int i = 0; i < tibikkoPathList.size(); i++) {
            List<String> cellValueList = new ArrayList<String>();

            String tibikkoBasePath = tibikkoPathList.get(i);

            Table<Integer, Integer, String> tibikkoMemoryTable = ExcelUtil.getTable(tibikkoBasePath + File.separator + TIBIKKO_MEMORY_INFO_FILENAME);
            int tibikkoMemorySize = tibikkoMemoryTable.rowMap().size();
            List<Long> tibikkoMemoryList = new ArrayList<Long>();
            for (int j = 1; j < tibikkoMemorySize; j++) {
                String tibikkoMemory = tibikkoMemoryTable.get(j, 4);
                if (!Strings.isNullOrEmpty(tibikkoMemory)) {
                    tibikkoMemoryList.add(Long.parseLong(tibikkoMemory));
                }
            }

            Collections.sort(tibikkoMemoryList);
            long tibikkoMaxMemory = tibikkoMemoryList.get(tibikkoMemoryList.size() - 1);
            long tibikkoMinMemory = tibikkoMemoryList.get(0);

            String sgiBasePath = sgiPathList.get(i);
            List<String> sgiMemoryLineList = Common.readAllLines(sgiBasePath + File.separator + RESULT_FILENAME);
            List<Integer> memoryList = new ArrayList<Integer>();
            for (String string : sgiMemoryLineList) {
                if (string.contains("p21") && string.contains("classpath")) {
                    String tmp = Common.clearSpace(string);
                    String[] valueArray = tmp.split(" ");
                    memoryList.add(Integer.parseInt(valueArray[5]));
                }
            }

            Collections.sort(memoryList);
            int sgiMaxMemory = memoryList.get(memoryList.size() - 1);
            int sgiMinMemory = memoryList.get(0);

            cellValueList.add(String.format("Step%d", i + 1));
            cellValueList.add(String.format("%.2f", tibikkoMaxMemory / 1000.0 / 1000.0));
            cellValueList.add(String.format("%.2f", tibikkoMinMemory / 1000.0 / 1000.0));
            cellValueList.add(String.format("%.2f", sgiMinMemory / 1000.0));
            cellValueList.add(String.format("%.2f", sgiMaxMemory / 1000.0));

            ExcelUtil.copyRow(templateWorkbook, startRowIndex1, templateWorkbook, startRowIndex1 + 1);
            ExcelUtil.setRowValue(sheet1, startRowIndex1, memoryColIndexList, cellValueList);

            ExcelUtil.setCellFormula(ExcelUtil.getCell(sheet1, startRowIndex1, 3),
                                        String.format("B%d-C%d", startRowIndex1 + 1, startRowIndex1 + 1));

            startRowIndex1++;
        }

        // ■SQL実施時間
        startRowIndex1 = startRowIndex1 + 7;

        List<Integer> sqlRunTimeColIndexList = Arrays.asList(0, 1, 2, 3, 4, 5);
        int idIndex = 1;
        for (int i = 0; i < tibikkoPathList.size(); i++) {

          String tibikkoBasePath = tibikkoPathList.get(i);
          Table<Integer, Integer, String> tibikkoDB1Table = ExcelUtil.getTable(tibikkoBasePath + File.separator + DB1_FILENAME);

          String sgiBasePath = sgiPathList.get(i);
          Table<Integer, Integer, String> sgiDB1Table = ExcelUtil.getTable(sgiBasePath + File.separator + DB1_FILENAME);

          int tibikkoDB1RowSize = tibikkoDB1Table.rowMap().size();

          for (int j = 0; j < tibikkoDB1RowSize; j++) {
              String tibikkoCellValue = tibikkoDB1Table.get(j, 1);

              if (!Strings.isNullOrEmpty(tibikkoCellValue) && StringUtils.isNumeric(tibikkoDB1Table.get(j, 9))) {
                  // DISK_READS
                  String tibikkoDiskReads = tibikkoDB1Table.get(j, 8);
                  // BUFFER_GETS(平均)
                  double tibikkoBufferTetsAverage = Double.parseDouble(tibikkoDB1Table.get(j, 9)) / Double.parseDouble(tibikkoDB1Table.get(j, 7));

                  // DISK_READS
                  String sgiDiskReads = sgiDB1Table.get(j, 8);
                  // BUFFER_GETS(平均)
                  double sgiBufferTetsAverage = Double.parseDouble(sgiDB1Table.get(j, 9)) / Double.parseDouble(sgiDB1Table.get(j, 7));


                  List<String> colValueList = Arrays.asList(
                                                              String.format("Step%d", i + 1),
                                                              String.valueOf(idIndex++),
                                                              tibikkoDiskReads,
                                                              String.valueOf(Math.round(tibikkoBufferTetsAverage)),
                                                              sgiDiskReads,
                                                              String.valueOf(Math.round(sgiBufferTetsAverage)));

                  ExcelUtil.copyRow(templateWorkbook, startRowIndex1, templateWorkbook, startRowIndex1 + 1);
                  ExcelUtil.setRowValue(sheet1, startRowIndex1, sqlRunTimeColIndexList, colValueList);

                  ExcelUtil.setCellFormula(ExcelUtil.getCell(sheet1, startRowIndex1, 6), String.format("E%d-C%d", startRowIndex1 + 1, startRowIndex1 + 1));
                  ExcelUtil.setCellFormula(ExcelUtil.getCell(sheet1, startRowIndex1, 7), String.format("F%d-D%d", startRowIndex1 + 1, startRowIndex1 + 1));

                  startRowIndex1++;
              }
          }


//            String tibikkoBasePath = tibikkoPathList.get(i);
//            Table<Integer, Integer, String> tibikkoDB1Table = ExcelUtil.getTable(tibikkoBasePath + File.separator + DB1_FILENAME);
//            int tibikkoDB1RowSize = tibikkoDB1Table.rowMap().size();
//
//            List<Long> tibikkoElapsedTimeAverageList = new ArrayList<Long>();
//            for (int j = 0; j < tibikkoDB1RowSize; j++) {
//                String cellValue = tibikkoDB1Table.get(j, 1);
//                if (!Strings.isNullOrEmpty(cellValue) && StringUtils.isNumeric(tibikkoDB1Table.get(j, 9))) {
//                    // DISK_READS
//                    String diskReads = tibikkoDB1Table.get(j, 8);
//                    // BUFFER_GETS(平均)
//                    double bufferTetsAverage = Double.parseDouble(tibikkoDB1Table.get(j, 9)) / Double.parseDouble(tibikkoDB1Table.get(j, 7));
////                    double elapsedTimeAverage = Double.parseDouble(tibikkoDB1Table.get(j, 10)) / Double.parseDouble(tibikkoDB1Table.get(j, 7));
////                    tibikkoElapsedTimeAverageList.add(Math.round(elapsedTimeAverage));
//                }
//            }
//
//
//            List<Long> sgiElapsedTimeAverageList = new ArrayList<Long>();
//            String sgiBasePath = sgiPathList.get(i);
//            Table<Integer, Integer, String> sgiDB1Table = ExcelUtil.getTable(sgiBasePath + File.separator + DB1_FILENAME);
//            int sgiDB1RowSize = sgiDB1Table.rowMap().size();
//
//            for (int j = 0; j < sgiDB1RowSize; j++) {
//                String cellValue = sgiDB1Table.get(j, 1);
//                if (!Strings.isNullOrEmpty(cellValue) && StringUtils.isNumeric(sgiDB1Table.get(j, 9))) {
//                    // DISK_READS
//                    String diskReads = tibikkoDB1Table.get(j, 8);
//                    // BUFFER_GETS(平均)
//                    double bufferTetsAverage = Double.parseDouble(sgiDB1Table.get(j, 9)) / Double.parseDouble(sgiDB1Table.get(j, 7));
//
//
//	                  sgiElapsedTimeAverageList.add(Math.round(elapsedTimeAverage));
//	                  sgiElapsedTimeAverageList.add(Math.round(elapsedTimeAverage));
//
////                    double elapsedTimeAverage = Double.parseDouble(sgiDB1Table.get(j, 10)) / Double.parseDouble(sgiDB1Table.get(j, 7));
////                    sgiElapsedTimeAverageList.add(Math.round(elapsedTimeAverage));
//                }
//            }
//
//            for (int j = 0; j < tibikkoElapsedTimeAverageList.size(); j++) {
//                Long tibikkoElapsedTimeAverage = tibikkoElapsedTimeAverageList.get(j);
//                Long sgiElapsedTimeAverage = sgiElapsedTimeAverageList.get(j);
//
//                List<String> colValueList = Arrays.asList(String.format("Step%d", i + 1), String.valueOf(idIndex++), String.valueOf(tibikkoElapsedTimeAverage), String.valueOf(sgiElapsedTimeAverage));
//
//                ExcelUtil.copyRow(templateWorkbook, startRowIndex1, templateWorkbook, startRowIndex1 + 1);
//                ExcelUtil.setRowValue(sheet1, startRowIndex1, sqlRunTimeColIndexList, colValueList);
//
//                startRowIndex1++;
//            }

        }


        // SQL性能
        Sheet sheetSQL = templateWorkbook.getSheet("SQL性能");
        for (int i = 1; i < tibikkoPathList.size(); i++) {

            ExcelUtil.copyRow(templateWorkbook, "SQL性能", 0, templateWorkbook, "SQL性能", i * 7 + 0, true, true);
            ExcelUtil.copyRow(templateWorkbook, "SQL性能", 1, templateWorkbook, "SQL性能", i * 7 + 1, true, true);
            ExcelUtil.copyRow(templateWorkbook, "SQL性能", 2, templateWorkbook, "SQL性能", i * 7 + 2, true, true);
            ExcelUtil.copyRow(templateWorkbook, "SQL性能", 3, templateWorkbook, "SQL性能", i * 7 + 3, true, true);
            ExcelUtil.copyRow(templateWorkbook, "SQL性能", 4, templateWorkbook, "SQL性能", i * 7 + 4, true, true);
            ExcelUtil.copyRow(templateWorkbook, "SQL性能", 5, templateWorkbook, "SQL性能", i * 7 + 5, true, true);
            ExcelUtil.copyRow(templateWorkbook, "SQL性能", 6, templateWorkbook, "SQL性能", i * 7 + 6, true, true);

            ExcelUtil.setCellValue(ExcelUtil.getCell(sheetSQL, i * 7, 0), String.format("Step%d", i + 1));
        }

        int startRowIndex = 2;
        int startSgiIdIndex = 1;
        int startTibikkoIdIndex = 1;

        for (int i = 0; i < tibikkoPathList.size(); i++) {
            String tibikkoBasePath = tibikkoPathList.get(i);
            boolean isSgiHeader = true;
            boolean isTibikkoHeader = true;

            Workbook tibikkoWorkbookDB1 = ExcelUtil.getWorkbook(tibikkoBasePath + File.separator + DB1_FILENAME);

            Table<Integer, Integer, String> tibikkoDB1Table = ExcelUtil.getTable(tibikkoBasePath + File.separator + DB1_FILENAME);
            int tibikkoDB1RowSize = tibikkoDB1Table.rowMap().size();

            for (int j = 0; j < tibikkoDB1RowSize; j++) {
                String cellValue = tibikkoDB1Table.get(j, 1);
                if (!Strings.isNullOrEmpty(cellValue)) {
                    ExcelUtil.copyRow(tibikkoWorkbookDB1, j, templateWorkbook, "SQL性能", startRowIndex++);

                    ExcelUtil.copyCellStyle(templateWorkbook,
                            ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 1), ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 0));
                    ExcelUtil.copyCellStyle(templateWorkbook,
                            ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 10), ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 11));
                    ExcelUtil.copyCellStyle(templateWorkbook,
                            ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 10), ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 12));

                    if (isTibikkoHeader) {
                        ExcelUtil.setCellValue(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 0), "ID");
                        ExcelUtil.setCellValue(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 11),
                                                ExcelUtil.getStringValue(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 9)) + "(平均)");
                        ExcelUtil.setCellValue(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 12),
                                                ExcelUtil.getStringValue(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 10)) + "(平均)");

                        isTibikkoHeader = false;
                    } else {
                        ExcelUtil.setCellValue(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 0), startTibikkoIdIndex++);
//                        ExcelUtil.setFillForegroundColor(templateWorkbook, ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 0), IndexedColors.LIGHT_YELLOW);
                        ExcelUtil.setCellFormula(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 11), String.format("J%d/H%d", startRowIndex, startRowIndex));
                        ExcelUtil.setCellFormula(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 12), String.format("K%d/H%d", startRowIndex, startRowIndex));
                    }
                }
            }

            startRowIndex = startRowIndex + 1;

            String sgiBasePath = sgiPathList.get(i);

            Workbook sgiWorkbookDB1 = ExcelUtil.getWorkbook(sgiBasePath + File.separator + DB1_FILENAME);
            Table<Integer, Integer, String> sgiDB1Table = ExcelUtil.getTable(sgiBasePath + File.separator + DB1_FILENAME);
            int sgiDB1RowSize = sgiDB1Table.rowMap().size();
            for (int j = 0; j < sgiDB1RowSize; j++) {
                String cellValue = sgiDB1Table.get(j, 1);
                if (!Strings.isNullOrEmpty(cellValue)) {
                    ExcelUtil.copyRow(sgiWorkbookDB1, j, templateWorkbook, "SQL性能", startRowIndex++);

                    ExcelUtil.copyCellStyle(templateWorkbook,
                            ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 1), ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 0));
                    ExcelUtil.copyCellStyle(templateWorkbook,
                            ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 10), ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 11));
                    ExcelUtil.copyCellStyle(templateWorkbook,
                            ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 10), ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 12));

                    if (isSgiHeader) {
                        ExcelUtil.setCellValue(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 0), "ID");
                        ExcelUtil.setCellValue(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 11),
                                ExcelUtil.getStringValue(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 9)) + "(平均)");
        ExcelUtil.setCellValue(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 12),
                                ExcelUtil.getStringValue(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 10)) + "(平均)");
                        isSgiHeader = false;
                    } else {
                        ExcelUtil.setCellValue(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 0), startSgiIdIndex++);
//                        ExcelUtil.setFillForegroundColor(templateWorkbook, ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 0), IndexedColors.LIGHT_YELLOW);
                        ExcelUtil.setCellFormula(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 11), String.format("J%d/H%d", startRowIndex, startRowIndex));
                        ExcelUtil.setCellFormula(ExcelUtil.getCell(sheetSQL, startRowIndex - 1, 12), String.format("K%d/H%d", startRowIndex, startRowIndex));
                    }
                }
            }

            startRowIndex = startRowIndex + 2;
        }


        ExcelUtil.save(saveFilename, templateWorkbook);
        System.out.println(String.format("ファイルが保存しました。「%s」", saveFilename));
    }

    public static void setTibikkoInfo(String saveFilename) throws IOException {

        Workbook workbook = ExcelUtil.getWorkbook();

        File baseFile = new File(BASE_PATH + File.separator + "ちびっ子");
        File[] listFiles = baseFile.listFiles();
        int stepIndex = 1;
        for (File file : listFiles) {
            if (file.isDirectory()) {

                String absolutePath = file.getAbsolutePath();
                System.out.println(String.format("base path: %s", absolutePath));

                // SQLメモリ利用状況
                System.out.println(String.format("Step%s SQLメモリ利用状況 書き込み開始", stepIndex));
                setSQLMemoryInfo(workbook, absolutePath, stepIndex);
                System.out.println(String.format("Step%s SQLメモリ利用状況 書き込み終了", stepIndex));

                // ちびっ子ログ
                System.out.println(String.format("Step%s ちびっ子ログ 書き込み開始", stepIndex));
                setTibokkoLog(workbook, absolutePath, stepIndex);
                System.out.println(String.format("Step%s ちびっ子ログ 書き込み終了", stepIndex));

                // ちびっ子メモリ利用状況
                System.out.println(String.format("Step%s ちびっ子メモリ利用状況 書き込み開始", stepIndex));
                setTibikkoMemoryInfo(workbook, absolutePath, stepIndex);
                System.out.println(String.format("Step%s ちびっ子メモリ利用状況 書き込み終了", stepIndex));

                // SQL実行計画
                System.out.println(String.format("Step%s SQL実行計画 書き込み開始", stepIndex));
                setTibikkoSQLExecPlan(workbook, absolutePath);
                System.out.println(String.format("Step%s SQL実行計画 書き込み終了", stepIndex));

                stepIndex++;
            }
        }

        ExcelUtil.save(saveFilename, workbook);
        System.out.println(String.format("ファイルが保存しました。「%s」", saveFilename));

    }

    public static void setSGIInfo(String saveFilename) throws IOException, ParseException {

        Workbook workbook = ExcelUtil.getWorkbook();

        File baseFile = new File(BASE_PATH + File.separator + "SGI");
        File[] listFiles = baseFile.listFiles();
        int stepIndex = 1;
        for (File file : listFiles) {
            if (file.isDirectory()) {

                String absolutePath = file.getAbsolutePath();
                System.out.println(String.format("base path: %s", absolutePath));

                // SQLメモリ利用状況
                System.out.println(String.format("Step%s SQLメモリ利用状況 書き込み開始", stepIndex));
                setSQLMemoryInfo(workbook, absolutePath, stepIndex);
                System.out.println(String.format("Step%s SQLメモリ利用状況 書き込み終了", stepIndex));

                // SIGログ
                System.out.println(String.format("Step%s SIGログ 書き込み開始", stepIndex));
                setSGILog(workbook, absolutePath, stepIndex);
                System.out.println(String.format("Step%s SIGログ 書き込み終了", stepIndex));

                // SGIメモリ利用状況
                System.out.println(String.format("Step%s SGIメモリ利用状況 書き込み開始", stepIndex));
                setSGIMemoryInfo(workbook, absolutePath, stepIndex);
                System.out.println(String.format("Step%s SGIメモリ利用状況 書き込み終了", stepIndex));

                // SQL実行計画
                System.out.println(String.format("Step%s SQL実行計画 書き込み開始", stepIndex));
                setSGISQLExecPlan(workbook, absolutePath);
                System.out.println(String.format("Step%s SQL実行計画 書き込み終了", stepIndex));

                stepIndex++;
            }
        }

        ExcelUtil.save(saveFilename, workbook);
        System.out.println(String.format("ファイルが保存しました。「%s」", saveFilename));

    }

    // SQLメモリ利用状況
    private static void setSQLMemoryInfo(Workbook workbook, String basePath, int stepIndex) throws IOException {
        Sheet sheetStep1 = workbook.createSheet("Step" + stepIndex);

        Workbook db1Workbook = ExcelUtil.getWorkbook(basePath + File.separator + DB1_FILENAME);
        Table<Integer, Integer, String> db1Table = ExcelUtil.getTable(basePath + File.separator + DB1_FILENAME);
        int db1RowSize = db1Table.rowMap().size();
        int destStartRowIndex = 0;
        for (int i = 0; i < db1RowSize; i++) {
            String cellValue = db1Table.get(i, 1);
            if (!Strings.isNullOrEmpty(cellValue)) {
                ExcelUtil.copyRow(db1Workbook, i, workbook, sheetStep1.getSheetName(), destStartRowIndex++);
            }
        }
    }

    // SIGログ
    private static void setSGILog(Workbook workbook, String basePath, int stepIndex) throws ParseException {
        Sheet sheetLog1 = workbook.createSheet("ログ" + stepIndex);

        File baseFile = new File(basePath);
        File logFile = getLogFile(baseFile);
        List<String> readAllLines = Common.readAllLines(logFile.getAbsolutePath());
        List<String> datetimeList = new ArrayList<String>();
        for (int i = 0; i < readAllLines.size(); i++) {
            ExcelUtil.setRowValue(sheetLog1, i, 0, readAllLines.get(i));

            String[] valueArray = Common.clearSpace(readAllLines.get(i)).split(" ");
            if (valueArray.length > 1) {
                datetimeList.add(String.format("%d#%s %s", i, valueArray[0], valueArray[1]));
            }
        }

        for (int i = 1; i < datetimeList.size(); i++) {
            String startDatetime = datetimeList.get(i - 1).replaceAll("-", "/");
            String endDatetime = datetimeList.get(i).replaceAll("-", "/");

            int startRowIndex = Integer.parseInt(startDatetime.split("#")[0]);
            startDatetime = startDatetime.split("#")[1];

            int endRowIndex = Integer.parseInt(endDatetime.split("#")[0]);
            endDatetime = endDatetime.split("#")[1];

            if (startDatetime.length() == 19 || endDatetime.length() == 19) {
                continue;
            }


            Date dateStart = DateUtil.stringToDate(DateUtil.LONG_DATE_SSS, startDatetime);
            Date dateEnd = DateUtil.stringToDate(DateUtil.LONG_DATE_SSS, endDatetime);

            long duration = dateEnd.getTime() - dateStart.getTime();
            if (duration >= HANDLE_TIME_MS) {
                ExcelUtil.setFillForegroundColor(workbook, ExcelUtil.getCell(sheetLog1, startRowIndex, 0), IndexedColors.YELLOW);
                ExcelUtil.setFillForegroundColor(workbook, ExcelUtil.getCell(sheetLog1, endRowIndex, 0), IndexedColors.YELLOW);
            }
        }
    }

    // SIGログ
    private static void setTibokkoLog(Workbook workbook, String basePath, int stepIndex) {
        Sheet sheetLog1 = workbook.createSheet("ログ" + stepIndex);

        List<String> readAllLines = Common.readAllLines(basePath + File.separator + TIBIKKO_LOG_FILENAME);
        for (int i = 0; i < readAllLines.size(); i++) {
            ExcelUtil.setRowValue(sheetLog1, i, 0, readAllLines.get(i));
        }
    }

    private static void setSGIMemoryInfo(Workbook workbook, String basePath, int stepIndex) {
        Sheet sheetMemoryLog1 = workbook.createSheet("メモリ監視" + stepIndex);

        List<String> resultAllLines = Common.readAllLines(basePath + File.separator + RESULT_FILENAME);
        List<String> memoryInfoList = getMemoryInfoList(resultAllLines);
        for (int i = 0; i < memoryInfoList.size(); i++) {
            ExcelUtil.setRowValue(sheetMemoryLog1, i, 0, memoryInfoList.get(i));
        }
    }

    private static void setTibikkoMemoryInfo(Workbook workbook, String basePath, int stepIndex) throws IOException {
        Sheet sheetMemoryLog1 = workbook.createSheet("メモリ監視" + stepIndex);

        Workbook db1Workbook = ExcelUtil.getWorkbook(basePath + File.separator + TIBIKKO_MEMORY_INFO_FILENAME);
        Table<Integer, Integer, String> db1Table = ExcelUtil.getTable(basePath + File.separator + TIBIKKO_MEMORY_INFO_FILENAME);
        int db1RowSize = db1Table.rowMap().size();
        int destStartRowIndex = 0;
        for (int i = 0; i < db1RowSize; i++) {
            String cellValue = db1Table.get(i, 0);
            if (!Strings.isNullOrEmpty(cellValue)) {
                ExcelUtil.copyRow(db1Workbook, i, workbook, sheetMemoryLog1.getSheetName(), destStartRowIndex++, false, true);
            }
        }
    }

    private static void setSGISQLExecPlan(Workbook workbook, String basePath) throws IOException {
        Workbook db2Workbook = ExcelUtil.getWorkbook(basePath + File.separator + DB2_FILENAME);
        Table<Integer, Integer, String> db2Table = ExcelUtil.getTable(basePath + File.separator + DB2_FILENAME);
        int db2RowSize = db2Table.rowMap().size();

        Multimap<Integer, Integer> db2Multmap = ArrayListMultimap.create();
        int db2Index = 1;

        for (int i = 0; i < db2RowSize; i++) {
            String cellValue = db2Table.get(i, 1);
            if (cellValue == null) {
                db2Index++;
            }
            else {
                db2Multmap.put(db2Index, i);
            }
        }

        Map<Integer, Collection<Integer>> asMap = db2Multmap.asMap();
        for (Entry<Integer, Collection<Integer>> entry : asMap.entrySet()) {
            Sheet db2Sheet = workbook.createSheet(String.valueOf(sgiExecPlanIndex));
            ArrayList<Integer> rowIndexList = Lists.newArrayList(entry.getValue());
            int destStartRowIndex = 0;
            for (Integer rowIndex : rowIndexList) {
                ExcelUtil.copyRow(db2Workbook, rowIndex, workbook, db2Sheet.getSheetName(), destStartRowIndex++);
//                db2Sheet.autoSizeColumn(1, true);
            }

            sgiExecPlanIndex++;
        }
    }

    private static void setTibikkoSQLExecPlan(Workbook workbook, String basePath) throws IOException {
        Workbook db2Workbook = ExcelUtil.getWorkbook(basePath + File.separator + DB2_FILENAME);
        Table<Integer, Integer, String> db2Table = ExcelUtil.getTable(basePath + File.separator + DB2_FILENAME);
        int db2RowSize = db2Table.rowMap().size();

        Multimap<Integer, Integer> db2Multmap = ArrayListMultimap.create();
        int db2Index = 1;

        for (int i = 0; i < db2RowSize; i++) {
            String cellValue = db2Table.get(i, 1);
            if (cellValue == null) {
                db2Index++;
            }
            else {
                db2Multmap.put(db2Index, i);
            }
        }

        Map<Integer, Collection<Integer>> asMap = db2Multmap.asMap();
        for (Entry<Integer, Collection<Integer>> entry : asMap.entrySet()) {
            Sheet db2Sheet = workbook.createSheet(String.valueOf(tibikkoExecPlanIndex));
            ArrayList<Integer> rowIndexList = Lists.newArrayList(entry.getValue());
            int destStartRowIndex = 0;
            for (Integer rowIndex : rowIndexList) {
                ExcelUtil.copyRow(db2Workbook, rowIndex, workbook, db2Sheet.getSheetName(), destStartRowIndex++);
//                db2Sheet.autoSizeColumn(1, true);
            }

            tibikkoExecPlanIndex++;
        }
    }

    public static File getLogFile(File baseFile) {
        File[] listFiles = baseFile.listFiles();
        int maxLen = 0;
        int maxLenIndex = 0;
        for (int i = 0; i < listFiles.length; i++) {
            File file = listFiles[i];

            int length = file.getName().length();
            if (length > maxLen) {
                maxLen = length;
                maxLenIndex = i;
            }
        }

        return listFiles[maxLenIndex];
    }

    public static List<String> getMemoryInfoList(List<String> readAllLines) {
        List<String> list = new ArrayList<String>();

        for (String string : readAllLines) {
            if (string.contains("p21")) {
                list.add(string);
            }
        }

        return list;
    }

    public static void getMemoryMaxAndMinList(List<String> readAllLines) {
        List<Integer> list = new ArrayList<Integer>();

        for (String string : readAllLines) {
            if (string.contains("p21") && string.contains("classpath")) {
                String tmp = Common.clearSpace(string);
                String[] valueArray = tmp.split(" ");
                System.out.println(valueArray[5]);
                list.add(Integer.parseInt(valueArray[5]));
            }
        }

        Collections.sort(list);
        System.out.println(list);

        System.out.println(String.format("%.2f", list.get(0) / 1000.0) + "\t"
                + String.format("%.2f", list.get(list.size() - 1) / 1000.0));
    }

}
