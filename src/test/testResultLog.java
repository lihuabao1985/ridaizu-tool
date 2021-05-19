package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import common.Common;

public class testResultLog {

    public static void main(String[] args) {
        // TODO 自動生成されたメソッド・スタブ

        String path = "C:\\Users\\li.huabao\\Documents\\PJRD001D\\STEP110\\result.txt";
        List<String> readAllLines = Common.readAllLines(path);
        getMemoryMaxAndMinList(readAllLines);
    }

    public static void getMemoryInfoList(List<String> readAllLines) {
        for (String string : readAllLines) {
            if (string.contains("p21")) {
                System.out.println(string);
            }
        }
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

        System.out.println(String.format("%.2f", list.get(0) / 1000.0) + "\t" + String.format("%.2f", list.get(list.size() - 1) / 1000.0));
    }


}
