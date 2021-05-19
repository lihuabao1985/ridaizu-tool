package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Replace {

    public static void main(String[] args) throws IOException {
        //         File file = new File("C:\\Users\\XCAI\\Desktop\\kirikae\\20200831");
        File file = new File(args[0]);
        File[] fileArray = file.listFiles();
        for (File file2 : fileArray) {
            String filePath = file2.getAbsolutePath();

            String filename = file2.getName();
            //             if (!(filename.contains("PP2PWA") && filename.contains("_accesslog") && filename.contains(".0"))) {
            if (!(filename.contains(args[1]) && filename.contains(args[2]) && filename.contains(args[3]))) {
                continue;
            }

            Path path = Paths.get(filePath);
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            System.out.println(lines.size());
            StringBuffer sb = new StringBuffer();
            for (String string : lines) {
                if (string.contains(".pmp")) {
                    string = string.replaceAll(".pmp", ".jsp");
                    System.out.println(string);
                }

                sb.append(string).append("\n");
            }

            saveDataToFile(filePath, sb.toString());
        }
    }

    private static void saveDataToFile(String fileName, String data) {
        BufferedWriter writer = null;
        File file = new File(fileName);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"));
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("文件写入成功！");
    }
}