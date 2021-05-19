package test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class FindJob {

    public static void main(String[] args) {
        // TODO 自動生成されたメソッド・スタブ
        String jobPath = "\\\\10.0.1.75\\c$\\PT21\\spt";
        File file = new File(jobPath);
        File[] listFiles = file.listFiles();

        Multimap<String, String> multimap = ArrayListMultimap.create();
        for (File file2 : listFiles) {
            String[] split = file2.getName().split("[.]");
            System.out.println(String.format("%s: %s", split[0], split[1]));
            multimap.put(split[0], split[1].toLowerCase());
        }

        System.out.println("----------------------------------------------------");

        Map<String, Collection<String>> asMap = multimap.asMap();
        for (Entry<String, Collection<String>> entry : asMap.entrySet()) {
            String key = entry.getKey();
            ArrayList<String> valueList = Lists.newArrayList(entry.getValue());
//            if ((isExist(valueList, "spt") && !isExist(valueList, "spv")) ||
//                    (!isExist(valueList, "spt") && isExist(valueList, "spv"))) {

            if (isExist(valueList, "spt") && !isExist(valueList, "spv")) {
                System.out.println(key);
            }
        }
    }

    private static  boolean isExist(List<String> list, String key) {
        return list.contains(key);
    }

}
