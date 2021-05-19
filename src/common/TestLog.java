package common;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class TestLog {

    public static void main(String[] args) throws IOException {
        String basePath = "C:\\次期国内スバル基幹\\sptpj\\SIC_IDE_PH15_C\\p21-app";
        List<File> fileList = FileUtil.traverseFolder1(basePath);

        System.out.println("対象ファイル一覧：");


        String keyword1 = "len=";
        String keyword2 = "@Pic";
        int maxLength = 100000;

        List<String> list = new ArrayList<String>();
        Multimap<String, String> multimap = ArrayListMultimap.create();

        for (File file : fileList) {
            if (file.isDirectory() || !file.getName().contains(".java")) {
                continue;
            }

            Path path = Paths.get(file.getAbsolutePath());
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

            boolean printDir = true;
            for (String line : lines) {
                if (line.contains(keyword1) && line.contains(keyword2)) {
                    String tmpLime = line.substring(line.indexOf(keyword1));
                    String length = tmpLime.split(",")[0].replaceAll(keyword1, "");

                    if (Strings.isNullOrEmpty(length)) {
                        continue;
                    }

                    if (!Strings.isNullOrEmpty(length) && Integer.parseInt(length) >= maxLength) {
                        if (printDir) {
                            System.out.println(file.getAbsolutePath());
                            list.add(file.getName().replaceAll(".java", ""));
                            printDir = false;
                        }
                        System.out.println(line);

                        multimap.put(file.getAbsolutePath(), line);
                    }

                }
            }
        }

        System.out.println();
        System.out.println();
        System.out.println("----------------------------------------------------------");
        System.out.println("対象サブルーチンを呼び出すクラスファイル一覧：");

//        String basePath2 = "C:\\次期国内スバル基幹\\sptpj\\SIC_IDE_PH15_C\\p21-app";
//        List<File> fileList2 = FileUtil.traverseFolder1(basePath2);

        List<String> set = new ArrayList<String>();
        for (File file : fileList) {
            if (file.isDirectory() || !file.getName().contains(".java")) {
                continue;
            }

            Path path = Paths.get(file.getAbsolutePath());
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

            for (String line : lines) {
                String name = file.getName().replaceAll(".java", "");
                String cList = check(line, name, list);
                if (!Strings.isNullOrEmpty(cList)) {
                    if (!set.contains(String.format("%s\t%s\t%s", file.getName(), file.getAbsolutePath(), cList))) {
                        set.add(String.format("%s\t%s\t%s", file.getName(), file.getAbsolutePath(), cList));
                    }
                }
            }
        }

        for (String string : set) {
            System.out.println(string);
        }

        System.out.println();
        System.out.println();
        System.out.println("----------------------------------------------------------");

        Map<String, Collection<String>> asMap = multimap.asMap();
        for (Entry<String, Collection<String>> entry : asMap.entrySet()) {
            String key = entry.getKey();
            String filename = key.substring(key.lastIndexOf("\\") + 1);
            List<String> valueList = Lists.newArrayList(entry.getValue());

            for (String line : valueList) {

                String tmpLime = line.substring(line.indexOf(keyword1));
                String length = tmpLime.split(",")[0].replaceAll(keyword1, "");

                if (Integer.parseInt(length) >= 100000 && Integer.parseInt(length) < 500000) {
                    System.out.println(String.format("10万～50万\t%s\t%s\t%s", filename, line.trim(), key));
                } else if (Integer.parseInt(length) >= 500000 && Integer.parseInt(length) < 1000000) {
                    System.out.println(String.format("50万～100万\t%s\t%s\t%s", filename, line.trim(), key));
                } else if (Integer.parseInt(length) >= 1000000 && Integer.parseInt(length) < 2000000) {
                    System.out.println(String.format("100万～200万\t%s\t%s\t%s", filename, line.trim(), key));
                } else {
                    System.out.println(String.format("200万～\t%s\t%s\t%s", filename, line.trim(), key));
                }
            }
        }
    }

    public static String check(String line, String fileName, List<String> list) {
        String keyword3 = "CobolSubProgram";
        for (String string : list) {

            if (!fileName.equals(string) && line.contains(string) && line.contains(keyword3)) {
                return string;
            }
        }

        return null;
    }

}
