package tool.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.base.Strings;

import common.Common;
import common.Def;

public class GetStatisticsInfoByLog implements TableDataOpt {

    private static final String tmpDENGYU = "=";
    private static final String tmpCOUNT = "COUNT";
    private static final String tmpFETCH = "FETCH";
    private static final String tmpSELECT = "SELECT";
    private static final String tmpUPDATE = "UPDATE";
    private static final String tmpINSERT = "INSERT";
    private static final String tmpDELETE = "DELETE";
    private static final String tmpHOSI = "*";
    private static final String tmpWHERE = "WHERE";
    private static final String KEYWORD_INFO_MAIN = "INFO 	[main]	";
    private static final String KEYWORD_INFO_ = "INFO   - ";

    public void exec(String[] args) throws IOException {
         System.out.println("処理開始。");

         String logFilePath = Def.SRC_NEW_LOG_COPY_TO_FILEPATH;
         if (Strings.isNullOrEmpty(logFilePath)) {
             System.out.println("処理終了。");
             System.exit(0);
         }

         File logFile = new File(logFilePath);
         if (!logFile.exists()) {
             System.out.println("指定されたパスは存在しません。");
             System.out.println("処理終了。");
             System.exit(0);
         }

         System.out.println("ファイル解析開始。");
         String statisticsInfo = getStatisticsInfo(logFilePath);
         System.out.println("ファイル解析終了。");
         String saveFilepath = Def.SRC_LOG_STATISTICS_FILEPATH;
         Common.saveDataToFile(saveFilepath, statisticsInfo);
         System.out.println(String.format("ファイル「%s」が保存されました。", saveFilepath));

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

                if (line.contains(KEYWORD_INFO_)) {
                    line = line.substring(line.indexOf(KEYWORD_INFO_)+ KEYWORD_INFO_.length());
                } else if (line.contains(KEYWORD_INFO_MAIN)) {
                    line = line.substring(line.indexOf(KEYWORD_INFO_MAIN)+ KEYWORD_INFO_MAIN.length());
                }

                System.out.println(line);
                sb.append(line).append("\r\n");
            }
        }

        return sb.toString();
    }

}
