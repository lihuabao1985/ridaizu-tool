package common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

public class Common {

    public static String leftFilling(int value, int length) {

        return String.format("%0" + length + "d", value);
    }

    public static String changeSql(String value) {

        if (Strings.isNullOrEmpty(value)) {
            return value;
        }

        String format = ":%s";
        int count = 1000;
        for (int i = count; i >= 0; i--) {
            String str = String.format(format, i);
            if (value.contains(str)) {
                value = value.replaceAll(str, "?");
            }
        }

        return value;
    }

    /**
    * 数字をExcelの列名のようにアルファベットへ変換する。
    *
    * @param num 1以上の整数
    * @return 例）A,B,C...Z,AA,AB...AZ,AAA,AAB...
    */
    public static String num2alphabet(int num) {

        int firstIndexAlpha = (int) 'A'; // アルファベットの最初の文字
        int sizeAlpha = 26; // アルファベットの個数

        if (num <= 0) {
            /* 0以下はブランクで返す */
            return "";

        } else if (num <= sizeAlpha) {
            /* 1～26までの処理 */
            return String.valueOf((char) (firstIndexAlpha + num - 1));

        } else {
            /* 27以上の処理 */

            int offset = num - 1; // 0からの連番に補正した値
            int tmp = offset;
            String str = "";
            while (true) {
                int div = tmp / sizeAlpha; // 商
                int mod = tmp % sizeAlpha; // あまり

                str = num2alphabet(mod + 1) + str;

                if (div <= 0) {
                    break;
                }

                tmp = (div - 1);
            }
            ;
            return str;
        }
    }

    /**
     * 通过正则表达式的方式获取字符串中指定字符的个数
     * @param text 指定的字符串
     * @return 指定字符的个数
     */
    public static int getCharCount(String text, String sChar) {
        // 根据指定的字符构建正则
        Pattern pattern = Pattern.compile(sChar);
        // 构建字符串和正则的匹配
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        // 循环依次往下匹配
        while (matcher.find()){ // 如果匹配,则数量+1
            count++;
        }
        return  count;
    }

    public static String clearSpace(String value) {
        String keyword = "  ";
        while(value.contains(keyword)) {
            value = value.replaceAll(keyword, " ");
        }

        return value.replaceAll("\t", " ");
    }

    public static List<String> readAllLines(Path path) {
        List<String> charsetList = Arrays.asList("UTF-8", "SJIS", "MS932");
        for (String string : charsetList) {
            try {
                Charset charset = Charset.forName(string);
                return Files.readAllLines(path, charset);
            } catch (IOException e) {
            }
        }

        return null;
    }

    public static List<String> readAllLines(String filepath) {
        List<String> charsetList = Arrays.asList("UTF-8", "SJIS", "MS932");
        Path path = Paths.get(filepath);

        for (String string : charsetList) {
            try {
                Charset charset = Charset.forName(string);
                return Files.readAllLines(path, charset);
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }

        return null;
    }

    public static void saveDataToFile(String fileName, String data) {
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
    }

    public static void main(String...strings) {
        String text = "sdfsd?s/?wefwef??wefwef?111";
        System.out.println(getCharCount(text, "[?]"));
    }

}
