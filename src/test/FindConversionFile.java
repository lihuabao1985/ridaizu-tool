package test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import common.FileUtil;

public class FindConversionFile {

    public static void main(String[] args) {
        // TODO 自動生成されたメソッド・スタブ
        String basePath = "C:\\次期国内スバル基幹\\doc\\40_現行コンバージョン\\提供資産";
        List<File> fileList = FileUtil.traverseFolder1(basePath);

        String keyword = "PJS00101"; //"PJM010" + "Controller.java";

        List<String> list = new ArrayList<String>();
        int count = 1;
        for (File file : fileList) {
            if (file.isDirectory() || !file.getName().contains(".java")) {
                continue;
            }

            list.add(file.getName());
            if (file.getName().contains(keyword)) {
                System.out.println(file.getName());
            }
//            System.out.println(String.format("%d\t%s", count++, file.getName()));
        }

        if (list.contains(keyword)) {
            System.out.println("OKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
        }
    }

}
