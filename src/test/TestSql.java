package test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.Common;

public class TestSql {

    public static void main(String...strings) {
        List<String> listList = Common.readAllLines("SQL.txt");

        Set<String> set = new HashSet();
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

        System.out.println("-------------------------------------------------------------");

        for (String string : set) {
            System.out.println(string);
        }
    }

    private static boolean isExistTableName(String str) {

        return str.contains("PT") || str.contains("PS");
    }

}
