package common;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Strings;
import com.google.common.collect.Table;

public class TestLog3 {

    public static void main(String[] args) throws IOException {
        String basePath = "C:\\次期国内スバル基幹\\sptpj\\SIC_IDE_PH15_C\\p21-app";
        List<File> fileList = FileUtil.traverseFolder1(basePath);

        Map<String, String> fileMap = new HashMap<String, String>();

        for (File file : fileList) {
            if (file.isDirectory() || !file.getName().contains(".java")) {
                continue;
            }

            fileMap.put(file.getName(), file.getAbsolutePath());
        }

        String keyword1 = "package ";
        String keyword2 = "クラス名     : ";
        Table<Integer, Integer, String> table = ExcelUtil.getTable("クラス変数長さ統計.xlsx", "機能単位");
        Workbook workbook = ExcelUtil.getWorkbook("クラス変数長さ統計.xlsx");
        Sheet sheet = workbook.getSheet("機能単位");

        int size = table.rowMap().size();
        for (int i = 1; i < size; i++) {
            if (Strings.isNullOrEmpty(table.get(i, 2))) {
                continue;
            }

            String fileName = table.get(i, 2);
            String filePath = fileMap.get(fileName);
            List<String> readAllLines = Common.readAllLines(filePath);

            String pgmId = fileName.replaceAll(".java", "");
            String packageName = null;
            String className = null;

            for (String line : readAllLines) {
                if (line.contains(keyword1)) {
                    packageName = line.substring(line.indexOf(keyword1) + keyword1.length()).replaceAll(";", "");
                    break;
                }
            }

            for (String line : readAllLines) {
                if (line.contains(keyword2)) {
                    className = line.substring(line.indexOf(keyword2) + keyword2.length());
                    break;
                }
            }

            System.out.println(String.format("%s\t%s\t%s\t%s", i, pgmId, className, packageName));


            ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, i, 0), pgmId);
            ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, i, 1), className);
            ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, i, 3), packageName);
        }

        ExcelUtil.save("クラス変数長さ統計.xlsx", workbook);
    }

}
