package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import common.Common;
import common.ExcelUtil;

public class LogTest2 {
    public static final String keyword = "処理時間";
    public static final List<String> sqlKeywordList = Arrays.asList("SELECT", "INSERT", "UPDATE", "DELETE");

    private static boolean checkSql(String line) {
        for (String string : sqlKeywordList) {
            if (line.contains(string)) {
                return true;
            }
        }

        return false;
    }

    private static Map<String, String> getSqlStatisticsMap(List<String> sqlList) {
        Multimap<String, String> multimap = LinkedHashMultimap.create();
        int count = 1;
        for (String string : sqlList) {
            List<String> rowValueList = getRowValueList(string);
            multimap.put(String.format("%s####%s", rowValueList.get(2), rowValueList.get(3)),
                    String.format("%s####%s", rowValueList.get(1), count++));
        }

        Map<String, String> map = new LinkedHashMap<String, String>();
        Map<String, Collection<String>> asMap = multimap.asMap();
        for (Entry<String, Collection<String>> entry : asMap.entrySet()) {
            ArrayList<String> list = Lists.newArrayList(entry.getValue());

            int sTime = 0;
            for (String string : list) {
                String[] split = string.split("####");
                sTime += Integer.parseInt(split[0]);
            }

            map.put(entry.getKey(), String.format("%s####%s", entry.getValue().size(), sTime));
        }

        return map;
    }

    private static List<String> getRowValueList(String line) {
        String sql = line.substring(line.indexOf(keyword));
        String[] split = sql.split(":");
        String str1 = split[0].trim();
        String str2 = split[1].trim();
        String str3 = split[2].trim();

        String str21 = str2.split(" ")[0];
        String str22 = str2.split(" ")[1];

        if (str3.contains("[")) {
            str3 = str3.substring(0, str3.indexOf("["));
        } else if (str3.contains("jp.co")) {
            str3 = str3.substring(0, str3.indexOf("jp.co") - "jp.co".length());
        }

        List<String> rowValueList = new ArrayList<String>();
        rowValueList.add(str1);
        rowValueList.add(str21.replaceAll("ms", ""));
        rowValueList.add(str22);
        rowValueList.add(str3);

        return rowValueList;
    }

    public static void main(String[] args) throws IOException {
        List<String> allLines = Common.readAllLines("p21-rmiserver_一回目.txt");
        List<String> webappAllLines = Common.readAllLines("p21-webapp_一回目.txt");

        Workbook workbook = ExcelUtil.getWorkbook();

        Sheet webappSheet = workbook.createSheet("webappログ");
        for (int i = 0; i < webappAllLines.size(); i++) {
            String line = webappAllLines.get(i);
            if (line.length() > 32767) {
                line = Common.clearSpace(line);

                if (line.length() > 32767) {
                    line = line.substring(0, 32766);
                }
            }

            ExcelUtil.setRowValue(webappSheet, i, 0, line);
        }

        Sheet sheet = workbook.createSheet("rmiログ");

        int startRowNo = 0;
        List<String> sqlList = new ArrayList<String>();

        for (int i = 0; i < allLines.size(); i++) {
            String line = allLines.get(i);
            if (line.length() > 32767) {
                line = Common.clearSpace(line);

                if (line.length() > 32767) {
                    line = line.substring(0, 32766);
                }
            }
            ExcelUtil.setRowValue(sheet, i, 0, line);

            if (line.contains(keyword)) {
                sheet.groupRow(startRowNo, i - 1);
                sheet.setRowGroupCollapsed(startRowNo, true);
                startRowNo = i + 1;

                if (checkSql(line)) {

                    sqlList.add(line);
                }

            }

        }

        sheet.groupRow(startRowNo, allLines.size() - 1);

        if (sqlList.size() > 0) {
            Sheet sqlSheet = workbook.createSheet("SQL文一覧");
            ExcelUtil.setRowValue(sqlSheet, 0, Arrays.asList(0, 1, 2, 3), Arrays.asList("", "SQL文実行時間(ms)", "SQL文ID", "SQL文"));

            for (int i = 0; i < sqlList.size(); i++) {
                String line = sqlList.get(i);
                List<String> rowValueList = getRowValueList(line);
    //            ExcelUtil.setRowValue(sqlSheet, i + 1, Arrays.asList(0, 1, 2, 3), rowValueList);
                Row row = sqlSheet.createRow(i + 1);
                ExcelUtil.setCellValue(row.createCell(0), rowValueList.get(0));
                ExcelUtil.setCellValue(row.createCell(1), Integer.parseInt(rowValueList.get(1)));
                ExcelUtil.setCellValue(row.createCell(2), rowValueList.get(2));
                ExcelUtil.setCellValue(row.createCell(3), rowValueList.get(3));
            }

            ExcelUtil.setCellFormula(sqlSheet.createRow(sqlList.size() + 1).createCell(1), String.format("SUM(B%s:B%s)", 1, sqlList.size() + 1));

            Sheet sqlStatisticsSheet = workbook.createSheet("SQL文一覧実行統計");
            ExcelUtil.setRowValue(sqlStatisticsSheet, 0, Arrays.asList(0, 1, 2, 3), Arrays.asList("SQL文ID", "SQL文", "実行回数", "実行時間(ms)"));

            Map<String, String> sqlStatisticsMap = getSqlStatisticsMap(sqlList);

            List<String> statisticsInfoList = new ArrayList<String>();
            StringBuffer sb = new StringBuffer();
            int rowIndex = 1;
            for (Entry<String, String> entry : sqlStatisticsMap.entrySet()) {
                String key = entry.getKey();
                String string1 = key.split("####")[0];
                String string2 = key.split("####")[1];
                String value = entry.getValue();
                String string3 = value.split("####")[0];
                String string4 = value.split("####")[1];

//                ExcelUtil.setRowValue(sqlStatisticsSheet, rowIndex++, Arrays.asList(0, 1, 2),
//                                                Arrays.asList(string1, string2, value));

                Row row = sqlStatisticsSheet.createRow(rowIndex++);
                ExcelUtil.setCellValue(row.createCell(0), string1);
                ExcelUtil.setCellValue(row.createCell(1), string2);

                ExcelUtil.setCellValue(row.createCell(2), Integer.parseInt(string3));
                ExcelUtil.setCellValue(row.createCell(3), Integer.parseInt(string4));

                statisticsInfoList.add(String.format("%sの実行回数：%s回(%sミリ秒)", string1, string3, string4));
            }

            Row statisticsRow = sqlStatisticsSheet.createRow(sqlStatisticsMap.size() + 1);
            ExcelUtil.setCellFormula(statisticsRow.createCell(2), String.format("SUM(C%s:C%s)", 1, sqlStatisticsMap.size() + 1));
            ExcelUtil.setCellFormula(statisticsRow.createCell(3), String.format("SUM(D%s:D%s)", 1, sqlStatisticsMap.size() + 1));

            ExcelUtil.setCellValue(sqlStatisticsSheet.createRow(sqlStatisticsMap.size() + 3).createCell(0),
                    String.format("全部のSQL文の実行回数：%s回。所要時間：%s秒",
                    ExcelUtil.getStringValue(ExcelUtil.getCell(sqlStatisticsSheet, sqlStatisticsMap.size() + 1, 2)),
                    ExcelUtil.getStringValue(ExcelUtil.getCell(sqlStatisticsSheet, sqlStatisticsMap.size() + 1, 3))));

            for (int j = 0; j < statisticsInfoList.size(); j++) {
                ExcelUtil.setCellValue(sqlStatisticsSheet.createRow(sqlStatisticsMap.size() + 4 + j).createCell(0), statisticsInfoList.get(j));

            }
        }


        // エクセル起動する時、公式を実行するように
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            workbook.getSheetAt(i).setForceFormulaRecalculation(true);
        }

        ExcelUtil.save("tmp.xlsx", workbook);

        System.out.println(allLines.size());

    }

}
