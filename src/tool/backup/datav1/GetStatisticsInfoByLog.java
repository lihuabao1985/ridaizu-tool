package tool.backup.datav1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.google.common.base.Strings;

import common.Common;

public class GetStatisticsInfoByLog implements TableDataOpt {

    public static final String DATA_FILEPATH = "data";
    public static final String tmpDENGYU = "=";
    public static final String tmpCOUNT = "COUNT";
    public static final String tmpFETCH = "FETCH";
    public static final String tmpSELECT = "SELECT";
    public static final String tmpUPDATE = "UPDATE";
    public static final String tmpINSERT = "INSERT";
    public static final String tmpDELETE = "DELETE";
    public static final String tmpHOSI = "*";
    public static final String tmpWHERE = "WHERE";
    public static final List<String> tmpList = Arrays.asList("FETCH", "SELECT", "UPDATE", "INSERT", "DELETE", "=");

    public void exec(String[] args) throws IOException {
         System.out.println("処理開始。");

         Scanner sc = new Scanner(System.in);
         System.out.println("ログファイルパスを入力してください。");
         String logFilePath = sc.nextLine();
         sc.close();

         if (Strings.isNullOrEmpty(logFilePath)) {
             System.out.println("処理終了。");
             System.exit(0);
         }

         File logFile = new File(logFilePath);
         if (!logFile.exists()) {
             System.out.println("入力されたパスは存在しません。");
             System.out.println("処理終了。");
             System.exit(0);
         }

         System.out.println("ファイル解析開始。");
         String statisticsInfo = getStatisticsInfo(logFilePath);
         System.out.println("ファイル解析終了。");
         Common.saveDataToFile(String.format("output\\statistics_%s", logFile.getName()), statisticsInfo);
         System.out.println("フォルダ「output」に保存しました。");

         System.out.println("処理終了。");
    }

    private String getStatisticsInfo(String filepath) throws IOException {

        Path path = Paths.get(filepath);
        List<String> lines = Common.readAllLines(path);

        StringBuffer sb = new StringBuffer();

        for (String line : lines) {

            boolean b = false;

            // FETCH
            if (line.contains(tmpFETCH) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
                b = true;
            }

            // SELECT
            if (line.contains(tmpSELECT) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
                if (!line.contains(tmpHOSI) && !line.contains(tmpWHERE))
                b = true;
            }

            // INSERT
            if (line.contains(tmpINSERT) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
                b = true;
            }

            // UPDATE
            if (line.contains(tmpUPDATE) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
                b = true;
            }

            // DELETE
            if (line.contains(tmpDELETE) && line.contains(tmpCOUNT) && line.contains(tmpDENGYU)) {
                b = true;
            }

            if (b) {
                line = line.substring(line.indexOf("INFO   - ") + "INFO   - ".length());
                System.out.println(line);
                sb.append(line).append("\r\n");
            }
        }

        return sb.toString();
    }

}
