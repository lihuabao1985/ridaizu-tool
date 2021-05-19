package tool.backup.data;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Strings;
import com.google.common.collect.Table;

import common.ExcelUtil;

public class GetTargetTableCURD {

    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);
        System.out.println("機能IDを入力してください。");
        String inPgmId = sc.nextLine();
        sc.close();

        if (Strings.isNullOrEmpty(inPgmId)) {
            System.out.println("処理終了。");
            System.exit(0);
        }

        Table<Integer, Integer, String> table = ExcelUtil.getTableBySXSSF("P_全SUB_ACCSESS_DB.xlsx");

        int startRowNo = 0;
        Workbook workbook = ExcelUtil.getWorkbook();
        Sheet sheet = workbook.createSheet();
        ExcelUtil.createRow(sheet, startRowNo++, getColumnList());
        String pgmName = null;
        int rowSize = table.rowKeySet().size();
        for (int rowNo = 3; rowNo < rowSize; rowNo++) {
            if (Strings.isNullOrEmpty(table.get(rowNo, 1))) {
                continue;
            }

            String pgmId = table.get(rowNo, 1);

            if (!pgmId.equals(inPgmId)) {
                continue;
            }

            pgmName = table.get(rowNo, 2);

            int no = 1;
            Map<Integer, String> row = table.row(rowNo);
            System.out.println("---------------------------------------------------------");

            for (Entry<Integer, String> rowEntry : row.entrySet()) {
                int colNo = rowEntry.getKey();
                String value = rowEntry.getValue().trim();

                if (colNo < 3) {
                    continue;
                }

                if (!Strings.isNullOrEmpty(value)) {
                    String tableName = table.get(1, colNo);
                    String tableNameStr = table.get(2, colNo);

                    System.out.println(String.format("%s\t%s\t%s\t%s\t%s",
                            pgmId, pgmName, tableName, tableNameStr, value));

                    ExcelUtil.createRow(sheet, startRowNo++, Arrays.asList(String.valueOf(no++), tableName, tableNameStr, value));
                }

            }

            System.out.println("---------------------------------------------------------");

            break;
        }

        if (Strings.isNullOrEmpty(pgmName)) {
            System.out.println(String.format("機能ID「%s」に該当する情報を見つけませんでした。", inPgmId));
        } else {

            //ウィンドウ枠の固定
            sheet.createFreezePane(0, 1);

            //列幅の自動調整
            for (Row row : sheet) {
                int lastCellNum = row.getLastCellNum();
                for (int i = 0; i < lastCellNum; i++) {
                    sheet.autoSizeColumn(i, true);
                }
            }


            ExcelUtil.createRow(sheet, startRowNo++, Arrays.asList(""));
            ExcelUtil.createRow(sheet, startRowNo++, Arrays.asList("※S:SELECT　F:FETCH　U:UPDATE　I:INSERT　D:DELETE"));

            String filePath = String.format("%s_%s_CURD.xlsx", inPgmId, pgmName);
            ExcelUtil.save(filePath, workbook);

            System.out.println(String.format("ファイル「%s」が出力されました。", filePath));
        }
    }

    private static List<String> getColumnList() {
        return Arrays.asList("No.", "論理名", "物理名", "操作区分");
    }

}
