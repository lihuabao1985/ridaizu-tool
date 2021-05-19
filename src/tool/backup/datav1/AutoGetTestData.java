package tool.backup.datav1;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

public class AutoGetTestData {

    public static final String NEW_LOG_FILEPATH = "C:\\Users\\li.huabao\\Desktop\\証拠\\output.log";

    public static final String LOG_COPY_TO_FILEPATH_FORMAT = "C:\\Users\\li.huabao\\Desktop\\TableDataOptTool\\%s_log_new.txt";

    public static final String TESTCASE_NO = "00001";

    public static void main(String[] args) throws IOException {

        // 新ログファイルの名前を変更し、指定の場所に移動
        Files.copy(new File(NEW_LOG_FILEPATH), new File(String.format(LOG_COPY_TO_FILEPATH_FORMAT, TESTCASE_NO)));

        // 新ログファイルを解析し、更新または登録されたデータを作成

        // 新ログファイルの統計情報を作成

        // テストデータを作成

        // 上記で作成されたデータをすべて、テスト証拠kのフォルダーに移動

        // テストデータを戻る

        // 現システムを実行し、ログファイルを指定の場所にコピー

        // 新ログファイルを解析し、更新または登録されたデータを作成

        // 作成されたデータをすべて、テスト証拠kのフォルダーに移動

        // 証拠データを作成
    }

}
