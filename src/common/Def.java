package common;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import config.Config;

public class Def {

    public static final String OK = "OK";
    public static final String NG = "NG";
    public static final String NEW = "new";
    public static final String OLD = "old";

    // テーブル物理名　行No
    public static final int TABLE_NAME_ROW_NO = 0;
    // テーブル物理名　列No
    public static final int TABLE_NAME_COLUMN_NO = 1;
    // 主キー　行No
    public static final int PRIMARY_KEY_ROW_NO = 1;
    // 主キー　列No
    public static final int PRIMARY_KEY_COLUMN_NO = 1;
    // 検索カラム　行No
    public static final int SEARCH_COLUMN_ROW_NO = 2;
    // 検索カラム　列No
    public static final int SEARCH_COLUMN_COLUMN_NO = 1;
    // 検索条件　行No
    public static final int SEARCH_CONDITIONS_ROW_NO = 3;
    // 検索条件　列No
    public static final int SEARCH_CONDITIONS_COLUMN_NO = 1;
    // 検索値　行No
    public static final int SEARCH_VALUE_ROW_NO = 4;
    // 検索値　列No
    public static final int SEARCH_VALUE_COLUMN_NO = 1;
    // 検索フリー条件　行No
    public static final int SEARCH_FREE_CONDITIONS_ROW_NO = 5;
    // 検索フリー条件　列No
    public static final int SEARCH_FREE_CONDITIONS_COLUMN_NO = 1;
    // 削除フリー条件　行No
    public static final int DELETE_FREE_CONDITIONS_ROW_NO = 6;
    // 削除フリー条件　列No
    public static final int DELETE_FREE_CONDITIONS_COLUMN_NO = 1;
    // カラム名　行No
    public static final int COLUMN_NAME_ROW_NO = 8;
    // カラムタイプ　行No
    public static final int COLUMN_TYPE_ROW_NO = 9;
    // データ　行No
    public static final int DATA_START_ROW_NO = 10;


    public static final String FORMAT_SELECT_SQL = "SELECT * FROM %s WHERE %s";
    public static final String FORMAT_INSERT_SQL = "INSERT INTO %s (%s) VALUES(%s)";
    public static final String FORMAT_UPDATE_SQL = "UPDATE %s SET %s WHERE %s";
    public static final String FORMAT_DELETE_SQL = "DELETE FROM %s WHERE %s";
    public static final String FORMAT_FILENAME_SELECT = "%s_SELECT.sql";
    public static final String FORMAT_FILENAME_INSERT = "%s_INSERT.sql";
    public static final String FORMAT_FILENAME_UPDATE = "%s_UPDATE.sql";
    public static final String FORMAT_FILENAME_DELETE = "%s_DELETE.sql";

    public static final List<String> TEMPLATE_SHEET_EXCLUSION_LIST = Arrays.asList(Config.getString("TEMPLATE_SHEET_EXCLUSION_LIST", "").split(","));

    public static final String NULL_STRING = "null";

    public static Map<Integer, String> zenkakuNumberMap = new HashedMap<Integer, String>();
    static {
        zenkakuNumberMap.put(0, "０");
        zenkakuNumberMap.put(1, "１");
        zenkakuNumberMap.put(2, "２");
        zenkakuNumberMap.put(3, "３");
        zenkakuNumberMap.put(4, "４");
        zenkakuNumberMap.put(5, "５");
        zenkakuNumberMap.put(6, "６");
        zenkakuNumberMap.put(7, "７");
        zenkakuNumberMap.put(8, "８");
        zenkakuNumberMap.put(9, "９");
        zenkakuNumberMap.put(10, "１０");
        zenkakuNumberMap.put(11, "１１");
        zenkakuNumberMap.put(12, "１２");
        zenkakuNumberMap.put(13, "１３");
        zenkakuNumberMap.put(14, "１４");
        zenkakuNumberMap.put(15, "１５");
        zenkakuNumberMap.put(16, "１６");
        zenkakuNumberMap.put(17, "１７");
        zenkakuNumberMap.put(18, "１８");
        zenkakuNumberMap.put(19, "１９");
        zenkakuNumberMap.put(20, "２０");
        zenkakuNumberMap.put(21, "２１");
        zenkakuNumberMap.put(22, "２２");
        zenkakuNumberMap.put(23, "２３");
        zenkakuNumberMap.put(24, "２４");
        zenkakuNumberMap.put(25, "２５");
        zenkakuNumberMap.put(26, "２６");
        zenkakuNumberMap.put(27, "２７");
        zenkakuNumberMap.put(28, "２８");
        zenkakuNumberMap.put(29, "２９");
        zenkakuNumberMap.put(30, "３０");
        zenkakuNumberMap.put(31, "３１");
        zenkakuNumberMap.put(32, "３２");
        zenkakuNumberMap.put(33, "３３");
        zenkakuNumberMap.put(34, "３４");
        zenkakuNumberMap.put(35, "３５");
        zenkakuNumberMap.put(36, "３６");
        zenkakuNumberMap.put(37, "３７");
        zenkakuNumberMap.put(38, "３８");
        zenkakuNumberMap.put(39, "３９");
        zenkakuNumberMap.put(40, "４０");
    }

    // ベースパス
    public static final String SRC_BASE_DIR = Config.getString("SRC_BASE_DIR");
    public static final String DEST_BASE_DIR = Config.getString("DEST_BASE_DIR");

    // テストケースNo
    public static final String TESTCASE_NO = Config.getString("TESTCASE_NO");

    // テストデータパス
    public static final String FORMAT_TABLE_DATA_FILENAME = Config.getString("FORMAT_TABLE_DATA_FILENAME");
    public static final String TABLE_DATA_FILENAME = String.format(FORMAT_TABLE_DATA_FILENAME, TESTCASE_NO);
    public static final String TABLE_DATA_FILEPATH = Config.getString("FORMAT_TABLE_DATA_BASE_FILEPATH") + File.separator + TABLE_DATA_FILENAME;

    // 新規ログファイルパス
    public static final String FORMAT_LOG_COPY_TO_FILENAME = Config.getString("FORMAT_LOG_COPY_TO_FILENAME");
    public static final String LOG_COPY_TO_FILENAME = String.format(FORMAT_LOG_COPY_TO_FILENAME, TESTCASE_NO);
    public static final String SRC_NEW_LOG_COPY_TO_FILEPATH = SRC_BASE_DIR + File.separator + TESTCASE_NO + File.separator + LOG_COPY_TO_FILENAME;
    public static final String DEST_NEW_LOG_COPY_TO_FILEPATH = DEST_BASE_DIR + File.separator + TESTCASE_NO + File.separator + LOG_COPY_TO_FILENAME;

    // 新規ログ統計ファイルパス
    public static final String FORMAT_LOG_STATISTICS_FILENAME = Config.getString("FORMAT_LOG_STATISTICS_FILENAME");
    public static final String LOG_STATISTICS_FILENAME = String.format(FORMAT_LOG_STATISTICS_FILENAME, LOG_COPY_TO_FILENAME);
    public static final String SRC_LOG_STATISTICS_FILEPATH = SRC_BASE_DIR + File.separator + TESTCASE_NO + File.separator + LOG_STATISTICS_FILENAME;
    public static final String DEST_LOG_STATISTICS_FILEPATH = DEST_BASE_DIR + File.separator + TESTCASE_NO + File.separator + LOG_STATISTICS_FILENAME;

    // 現行ログファイルパス
    public static final String POWER_SHELL_FORMAT_COPY_TO_LOCAL_FILENAME = Config.getString("POWER_SHELL_FORMAT_COPY_TO_LOCAL_FILENAME");
    public static final String SRC_POWER_SHELL_COPY_TO_LOCAL_FILEPATH = SRC_BASE_DIR + File.separator + TESTCASE_NO + File.separator +
                                                                                String.format(POWER_SHELL_FORMAT_COPY_TO_LOCAL_FILENAME, TESTCASE_NO);
    public static final String DEST_POWER_SHELL_COPY_TO_LOCAL_FILEPATH = DEST_BASE_DIR + File.separator + TESTCASE_NO + File.separator +
                                                                                String.format(POWER_SHELL_FORMAT_COPY_TO_LOCAL_FILENAME, TESTCASE_NO);

    // テストデータバックアップパス
    public static final String FORMAT_TESTDATA_FILENAME = Config.getString("FORMAT_TESTDATA_FILENAME");
    public static final String SRC_TESTDATA_FILEPATH = SRC_BASE_DIR + File.separator + TESTCASE_NO + File.separator + String.format(FORMAT_TESTDATA_FILENAME, TESTCASE_NO);
    public static final String DEST_TESTDATA_FILEPATH = DEST_BASE_DIR + File.separator + TESTCASE_NO + File.separator + String.format(FORMAT_TESTDATA_FILENAME, TESTCASE_NO);

    // エビデンス名
    public static final String FORMAT_EVIDENCE_FILENAME = Config.getString("FORMAT_EVIDENCE_FILENAME");
    public static final String EVIDENCE_FILENAME = String.format(FORMAT_EVIDENCE_FILENAME, TESTCASE_NO);
    public static final String DEST_EVIDENCE_FILENAME = DEST_BASE_DIR + File.separator + TESTCASE_NO + File.separator + EVIDENCE_FILENAME;

    // 新規ログ出力パス
    public static final String NEW_LOG_FILEPATH = Config.getString("NEW_LOG_FILEPATH");

    // 各ファイルフォーマット
    public static final String FORMAT_TABLE_DIR_FILEPATH = Config.getString("FORMAT_TABLE_DIR_FILEPATH");
    public static final String FORMAT_TABLE_FILENAME = Config.getString("FORMAT_TABLE_FILENAME");
    public static final String FORMAT_UPDATED_TABLE_FILENAME = Config.getString("FORMAT_UPDATED_TABLE_FILENAME");

    // SQLベースパス
    public static final String SQL_PATH = Config.getString("SQL_PATH");

}
