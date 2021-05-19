package test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.Common;

public class TestSql2 {

    public static void main(String...strings) {
        String path = "C:\\次期国内スバル基幹\\sptpj\\PH1.5_NF\\p21-app\\p21-lxpa\\p21-lxpa-business\\src\\main\\java\\spt\\lxp\\lxpa\\pab63101";
        File folder = new File(path);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("フォルダーではありません。");
            System.exit(0);
        }

        Set<String> set = new HashSet<String>();

        File[] fileList = folder.listFiles();
        int count = 1;
        for (File file : fileList) {
            String filename = file.getName();

            System.out.println(String.format("%d: %s", count++, file.getAbsolutePath()));

            if (filename.contains(".sql")) {

                List<String> listList = Common.readAllLines(file.getAbsolutePath());

                for (String string : listList) {

                    while(isExistTableName(string)) {

                        System.out.println(string);

                        if (string.contains("PT")) {
                            int index = string.indexOf("PT");
                            String tableName = string.substring(index, index + 8);
                            System.out.println(tableName);

                            string = string.substring(index + 8);
                            System.out.println(string);

                            set.add(tableName);
                        }

                        if (string.contains("PS") ) {
                            int index = string.indexOf("PS");
                            String tableName = string.substring(index, index + 8);
                            System.out.println(tableName);

                            string = string.substring(index + 8);
                            System.out.println(string);

                            set.add(tableName);
                        }

                    }

                }



            }
        }

        System.out.println("-------------------------------------------------------------");

        for (String string : set) {
            System.out.println(string);
        }
    }

    private static boolean isExistTableName(String str) {

        return str.contains("PT") || str.contains("PS");
    }

}
