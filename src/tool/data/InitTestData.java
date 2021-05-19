package tool.data;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;

import common.Def;

public class InitTestData {
    // 1: initDirAndLogFile, 2: moveEvidenceFile
    private static final List<String> paramValueList = Arrays.asList("1", "2");

    public void exec(String[] args) throws IOException, SQLException, InterruptedException {
        if (args.length == 0) {
            System.out.println("実行タイプを入力してください。");
            System.exit(0);
        }

        String div = args[0];

        if (!paramValueList.contains(div)) {
            System.out.println("正しい実行タイプを入力してください。");
            System.exit(0);
        }

        if ("1".equals(div)) {
            initDirAndLogFile();
        } else {
            moveEvidenceFile();
        }
    }

    public void initDirAndLogFile() throws IOException {
        System.out.println("各パスとログファイル初期化開始。");

        // 各フォルダーを作成
        String srcBaseDir = String.format(Def.SRC_BASE_DIR, Def.TESTCASE_NO) + File.separator + Def.TESTCASE_NO;
        File srcBaseDirFile = new File(srcBaseDir);
        if (!srcBaseDirFile.exists()) {
            srcBaseDirFile.mkdirs();
        }

        File destBaseDirFile = new File(Def.DEST_BASE_DIR);
        if (!destBaseDirFile.exists()) {
            destBaseDirFile.mkdirs();
        }

        String logFilepath = srcBaseDir + File.separator + String.format(Def.FORMAT_LOG_COPY_TO_FILENAME, Def.TESTCASE_NO);

        // 新ログファイルの名前を変更し、指定の場所に移動
        Files.copy(new File(Def.NEW_LOG_FILEPATH), new File(logFilepath));
        System.out.println(String.format("ファイル「%s」が保存されました。", logFilepath));

        System.out.println("各パスとログファイル初期化終了。");
    }

    public void moveEvidenceFile() throws IOException {
        System.out.println("証拠ファイル移動開始。");

        // 上記で作成されたデータをすべて、テスト証拠のフォルダーに移動
        //コピー元のパスを持つFileオブジェクトの生成
        File srcDir = new File(Def.SRC_BASE_DIR);
        File destBaseDirFile = new File(Def.DEST_BASE_DIR);

        //ディレクトリの複製
        FileUtils.copyDirectory(srcDir, destBaseDirFile);
        FileUtils.deleteDirectory(srcDir);

        System.out.println(String.format("証拠ファイルは「%s」に移動しました。", destBaseDirFile));
        System.out.println("証拠ファイル移動終了。");
    }

}
