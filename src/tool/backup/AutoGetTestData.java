package tool.backup;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.google.common.io.Files;

public class AutoGetTestData {

    public static final String SRC_BASE_DIR = "C:\\Users\\li.huabao\\Desktop\\TableDataOptTool\\output";

    public static final String DEST_BASE_DIR = "C:\\Users\\li.huabao\\Desktop\\TableDataOptTool\\data";

    public static final String NEW_LOG_FILEPATH = "C:\\Users\\li.huabao\\Desktop\\証拠\\output.log";

    public static final String LOG_COPY_TO_FILEPATH_FORMAT = "%s_log_new.txt";

    public static final String TESTCASE_NO = "00001";

    public static final String TABLE_DATA_FORMAT = "table_%s.xlsx";

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {

        // 各フォルダーを作成
        String srcBaseDir = String.format(SRC_BASE_DIR, TESTCASE_NO) + File.separator + TESTCASE_NO;
        File srcBaseDirFile = new File(srcBaseDir);
        if (!srcBaseDirFile.exists()) {
            srcBaseDirFile.mkdirs();
        }

        String destBaseDir = DEST_BASE_DIR;
        File destBaseDirFile = new File(destBaseDir);
        if (!destBaseDirFile.exists()) {
            destBaseDirFile.mkdirs();
        }

        String logFilepath = srcBaseDir + File.separator + String.format(LOG_COPY_TO_FILEPATH_FORMAT, TESTCASE_NO);
        String tableDataFilepath = String.format(TABLE_DATA_FORMAT, TESTCASE_NO);

        // 新ログファイルの名前を変更し、指定の場所に移動
        Files.copy(new File(NEW_LOG_FILEPATH), new File(logFilepath));

//        // 新ログファイルを解析し、更新または登録されたデータを作成
//        GetUpdatedDataByLog getUpdatedDataByLog = new GetUpdatedDataByLog();
//        getUpdatedDataByLog.exec(logFilepath, tableDataFilepath, TESTCASE_NO, Def.NEW);
//
//        // 新ログファイルの統計情報を作成
//        GetStatisticsInfoByLog getStatisticsInfoByLog = new GetStatisticsInfoByLog();
//        getStatisticsInfoByLog.exec(logFilepath, TESTCASE_NO);

//        // テストデータを作成
//        GetTestDataByLog getTestDataByLog = new GetTestDataByLog();
//        getTestDataByLog.exec(logFilepath, tableDataFilepath, TESTCASE_NO);
//
//        // テストデータを戻る
//        UpdateTableData updateTableData = new UpdateTableData();
//        updateTableData.exec(tableDataFilepath);
//
//        // 現システムを実行し、ログファイルを指定の場所にコピー
//        new Thread(new PowerShellTest(srcBaseDir, TESTCASE_NO)).start();
//        Thread.sleep(10000);

//        // 新ログファイルを解析し、更新または登録されたデータを作成
//        getUpdatedDataByLog = new GetUpdatedDataByLog();
//        getUpdatedDataByLog.exec(logFilepath, tableDataFilepath, TESTCASE_NO, Def.OLD);
//
//        // 上記で作成されたデータをすべて、テスト証拠のフォルダーに移動
//        //コピー元のパスを持つFileオブジェクトの生成
//        File srcDir = new File(SRC_BASE_DIR);
//
//        //ディレクトリの複製
//        FileUtils.copyDirectory(srcDir, destBaseDirFile);
    }

}
