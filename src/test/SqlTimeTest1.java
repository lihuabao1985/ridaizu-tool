package test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Table;

import common.ExcelUtil;

public class SqlTimeTest1 {

    public static void main(String... string) {
        pom640();
    }

    private static void pom650() {

        Table<Integer, Integer, String> table = ExcelUtil.getTableBySXSSF("POM650_SGI単体性能調査.xlsx", "sqlログ_normal");

        int rowSize = table.rowMap().size();

        Map<String, List<Integer>> map = new LinkedHashMap<String, List<Integer>>();

        for (int i = 1; i < rowSize; i++) {
            String sqlId = table.get(i, 3);
            String time = table.get(i, 4);

            if (!map.containsKey(sqlId)) {
                map.put(sqlId, Arrays.asList(1, Integer.parseInt(time)));
            } else {
                List<Integer> list = map.get(sqlId);
                int runCount = list.get(0);
                int runTime = list.get(1);

                runCount++;
                runTime += Integer.parseInt(time);

                map.put(sqlId, Arrays.asList(runCount, runTime));
            }

            System.out.println(String.format("%s\t%s\t%s", i, sqlId, time));
        }

        System.out.println("------------------------------------------");

        int sum = 0;
        for (Entry<String, List<Integer>> entry : map.entrySet()) {
            List<Integer> valueList = entry.getValue();
            sum += valueList.get(1);

            double d = (double)valueList.get(1) / (double)valueList.get(0);

            System.out.println(String.format("%s\t：実行件数：%s件、実行時間：%sms、平均実行時間：%sms", entry.getKey(), valueList.get(0), valueList.get(1), d));
        }

        System.out.println(String.format("Total: %s", sum));

    }

    private static void pom640() {

        Table<Integer, Integer, String> table = ExcelUtil.getTableBySXSSF("POM640_SGI単体性能調査.xlsx", "sqlログ_通常モード");

        int rowSize = table.rowMap().size();

        Map<String, List<Integer>> map = new LinkedHashMap<String, List<Integer>>();

        for (int i = 1; i < rowSize; i++) {
            String sqlId = table.get(i, 3);
            String time = table.get(i, 4);

            if (!map.containsKey(sqlId)) {
                map.put(sqlId, Arrays.asList(1, Integer.parseInt(time)));
            } else {
                List<Integer> list = map.get(sqlId);
                int runCount = list.get(0);
                int runTime = list.get(1);

                runCount++;
                runTime += Integer.parseInt(time);

                map.put(sqlId, Arrays.asList(runCount, runTime));
            }

            System.out.println(String.format("%s\t%s\t%s", i, sqlId, time));
        }

        System.out.println("------------------------------------------");

        int sum = 0;
        for (Entry<String, List<Integer>> entry : map.entrySet()) {
            List<Integer> valueList = entry.getValue();
            sum += valueList.get(1);

            double d = (double)valueList.get(1) / (double)valueList.get(0);

            System.out.println(String.format("%s\t：実行件数：%s件、実行時間：%sms、平均実行時間：%sms", entry.getKey(), valueList.get(0), valueList.get(1), d));
        }

        System.out.println(String.format("Total: %s", sum));

    }

}
