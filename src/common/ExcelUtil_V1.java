package common;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;


/**
 * @author kaho-ri
 *
 */
public class ExcelUtil_V1 {

    public static final int DEFAULT_SHEET = 0;

    public static Map<String, Table<Integer, Integer, String>> getTableMap(String filePath) throws IOException {
        Map<String, Table<Integer, Integer, String>> map = Maps.newHashMap();
        Workbook wb = getWorkbook(filePath);
        Iterator<Sheet> sheetIterator = wb.sheetIterator();
        while(sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            String sheetName = sheet.getSheetName();
            map.put(sheetName, getTable(wb, sheetName));
        }
        return map;
    }

    public static Table<Integer, Integer, String> getTable(String filePath) {
        return getTable(filePath, DEFAULT_SHEET);
    }

    public static Table<Integer, Integer, String> getTable(InputStream fileInputStream, String filePath) {
        return getTable(fileInputStream, filePath, DEFAULT_SHEET);
    }

    public static Table<Integer, Integer, String> getTable(InputStream fileInputStream, String filename, int sheetIndex) {
        Table<Integer, Integer, String> table = HashBasedTable.create();
        try {
            Workbook wb = getWorkbook(fileInputStream, filename);
            //シートを読み込みます。
            Sheet sheet = wb.getSheetAt(sheetIndex);

            Iterator<Row> rowIterator = sheet.rowIterator();
            while(rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                while(cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    table.put(row.getRowNum(), cell.getColumnIndex(), getStringValue(cell));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("処理が失敗しました");
        }

        return table;
    }

    public static Table<Integer, Integer, String> getTable(String filePath, int sheetIndex) {
        Table<Integer, Integer, String> table = HashBasedTable.create();
        try {
            Workbook wb = getWorkbook(filePath);
            //シートを読み込みます。
            Sheet sheet = wb.getSheetAt(sheetIndex);

            Iterator<Row> rowIterator = sheet.rowIterator();
            while(rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                while(cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    table.put(row.getRowNum(), cell.getColumnIndex(), getStringValue(cell));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("処理が失敗しました");
        }

        return table;
    }

    public static Table<Integer, Integer, String> getTable(String filePath, String sheetName) {
        Table<Integer, Integer, String> table = HashBasedTable.create();
        try {
            Workbook wb = getWorkbook(filePath);
            //シートを読み込みます。
            Sheet sheet = wb.getSheet(sheetName);

            Iterator<Row> rowIterator = sheet.rowIterator();
            while(rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                while(cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    table.put(row.getRowNum(), cell.getColumnIndex(), getStringValue(cell));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("処理が失敗しました");
        }

        return table;
    }

    public static Table<Integer, Integer, String> getTable(Workbook wb, String sheetName) {
        Table<Integer, Integer, String> table = HashBasedTable.create();
        try {
            //シートを読み込みます。
            Sheet sheet = wb.getSheet(sheetName);

            Iterator<Row> rowIterator = sheet.rowIterator();
            while(rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                while(cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    table.put(row.getRowNum(), cell.getColumnIndex(), getStringValue(cell));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("処理が失敗しました");
        }

        return table;
    }

    public static Workbook getWorkbook(String filePath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        String fileExtension = filePath.substring(filePath.lastIndexOf("."));
        Workbook workbook = null;
        if(fileExtension.equals(".xls")){
            workbook  = new HSSFWorkbook(new POIFSFileSystem(fileInputStream));
        } else if(fileExtension.equals(".xlsx") || fileExtension.equals(".xlsm")){
            workbook  = new XSSFWorkbook(fileInputStream);
        }
        return workbook;
    }

    public static Workbook getWorkbook(InputStream fileInputStream, String filename) throws IOException {
        String fileExtension = filename.substring(filename.indexOf("."));
        Workbook workbook = null;
        if(fileExtension.equals(".xls")){
            workbook  = new HSSFWorkbook(new POIFSFileSystem(fileInputStream));
        } else if(fileExtension.equals(".xlsx")){
            workbook  = new XSSFWorkbook(fileInputStream);
        }
        return workbook;
    }

    public static String getStringValue(Cell cell) {
        String value = null;
        switch (cell.getCellType()) {
        // 数値
        // 日付も数値として判定される
        case Cell.CELL_TYPE_NUMERIC:

            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());
                value = sdf.format(date);
            } else {
                DecimalFormat df = new DecimalFormat("0");
                value = df.format(cell.getNumericCellValue());
            }
            break;
        // 関数（SUMとかIFとか）
        case Cell.CELL_TYPE_FORMULA:
//            value = String.valueOf(cell.getCellFormula());
            value = getStringFormulaValue(cell);
            break;
        // 真偽
        case Cell.CELL_TYPE_BOOLEAN:
            value = Boolean.toString(cell.getBooleanCellValue());
            break;
        // 文字列
        case Cell.CELL_TYPE_STRING:
            value = cell.getStringCellValue();
            break;
        // 空
        case Cell.CELL_TYPE_BLANK:
            value = ""; //getStringRangeValue(cell);
            break;
        default:
            System.out.println(cell.getCellType());
            return null;
        }
        return value;
    }

    // セルの数式を計算し、Stringとして取得する例
    public static String getStringFormulaValue(Cell cell) {
        assert cell.getCellType() == Cell.CELL_TYPE_FORMULA;

        Workbook book = cell.getSheet().getWorkbook();
        CreationHelper helper = book.getCreationHelper();
        FormulaEvaluator evaluator = helper.createFormulaEvaluator();
        CellValue value = evaluator.evaluate(cell);
        switch (value.getCellType()) {
        case Cell.CELL_TYPE_STRING:
            return value.getStringValue();
        case Cell.CELL_TYPE_NUMERIC:
            DecimalFormat df = new DecimalFormat("0");
            return df.format(value.getNumberValue());
//            return Double.toString(value.getNumberValue());
        case Cell.CELL_TYPE_BOOLEAN:
            return Boolean.toString(value.getBooleanValue());
        default:
            System.out.println(value.getCellType());
            return null;
        }
    }

    // 結合セルの値をStringとして取得する例
     public static String getStringRangeValue(Cell cell) {
         int rowIndex = cell.getRowIndex();
         int columnIndex = cell.getColumnIndex();

         Sheet sheet = cell.getSheet();
         int size = sheet.getNumMergedRegions();
         for (int i = 0; i < size; i++) {
             CellRangeAddress range = sheet.getMergedRegion(i);
             if (range.isInRange(rowIndex, columnIndex)) {
                 Cell firstCell = getCell(sheet, range.getFirstRow(), range.getFirstColumn()); // 左上のセルを取得
                 return getStringValue(firstCell);
             }
         }
         return null;
     }
    public static Cell getCell(Sheet sheet, int rowIndex, int columnIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            Cell cell = row.getCell(columnIndex);
            return cell;
        }
        return null;
    }

    public static void createRow(Sheet sheet, int rowNum, List<String> cellValueList) {
        Row row = createRow(sheet, rowNum);
        for (int i = 0; i < cellValueList.size(); i++) {
            setCell(createCell(row, i), cellValueList.get(i));
        }
    }

    public static void createRow(Sheet sheet, int rowNum, List<String> cellValueList, Workbook workbook, IndexedColors color) {
        Row row = createRow(sheet, rowNum);
        for (int i = 0; i < cellValueList.size(); i++) {

        	Cell cell = createCell(row, i);

			//スタイルの生成
		    CellStyle cellstyle = workbook.createCellStyle();
		    cellstyle.setFillForegroundColor(color.index);  //色の指定
		    cellstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);    //塗り潰し
		    cell.setCellStyle(cellstyle);

            setCell(cell, cellValueList.get(i));
        }
    }

    public static Workbook getWorkbook() {
        return getXSSFWorkbook();
    }

    public static Workbook getXSSFWorkbook() {
        return new XSSFWorkbook();
    }

    public static Workbook getHSSFWorkbook() {
        return new HSSFWorkbook();
    }

    public static Sheet getSheet() {
        Workbook wb = getWorkbook();
        return wb.createSheet();
    }

    public static Row createRow(Sheet sheet, int rowNum) {
        return sheet.createRow(rowNum);
    }

    public static Cell createCell(Row row, int columnIndex) {
        return row.createCell(columnIndex);
    }

    public static void setCell(Cell cell, String value) {
        cell.setCellValue(value);
    }

    public static void save(String filePath, Workbook wb) {
        FileOutputStream out = null;
        try{
          out = new FileOutputStream(filePath);
          wb.write(out);
        }catch(IOException e){
          System.out.println(e.toString());
        }finally{
          try {
            out.close();
          }catch(IOException e){
            System.out.println(e.toString());
          }
        }
    }




    /**
     * POIで行をコピーする処理
     * @param srcWorkbook ワークブック
     * @param worksheet ワークシート
     * @param srcRowNum コピー元の行インデックス
     * @param destRowNum コピー先の行インデックス
     */
    @SuppressWarnings("deprecation")
    public static void copyRow(Workbook srcWorkbook, String srcSheetName, int srcRowNum, Workbook destWorkbook, int destRowNum) {

//		Sheet srcSheet = srcWorkbook.getSheetAt(0);
        Sheet srcSheet = srcWorkbook.getSheet(srcSheetName);
        Sheet destSheet = destWorkbook.getSheetAt(0);

        Row descRow = destSheet.getRow(destRowNum);
        Row srcRow = srcSheet.getRow(srcRowNum);

        if (descRow != null) {
            // コピー先に行が既に存在する場合、１行下にずらす
        	destSheet.shiftRows(destRowNum, destSheet.getLastRowNum(), 1);
            descRow = destSheet.createRow(destRowNum);
        } else {
            // 存在しない場合は作成
            descRow = destSheet.createRow(destRowNum);
        }

        // セルの型、スタイル、値などをすべてコピーする
        for (int i = 0; i < srcRow.getLastCellNum(); i++) {
            Cell srcCell = srcRow.getCell(i);
            Cell destCell = descRow.createCell(i);

            // コピー元の行が存在しない場合、処理を中断
            if (srcCell == null) {
                destCell = null;
                continue;
            }

            // スタイルのコピー
            CellStyle srcCellStyle = srcWorkbook.createCellStyle();
            srcCellStyle.cloneStyleFrom(srcCell.getCellStyle());

            CellStyle destCellStyle = destWorkbook.createCellStyle();
            destCellStyle.cloneStyleFrom(srcCellStyle);
            destCell.setCellStyle(destCellStyle);

            // コメントのコピー
            if (srcCell.getCellComment() != null) {
                destCell.setCellComment(srcCell.getCellComment());
            }

            // ハイパーリンクのコピー
            if (srcCell.getHyperlink() != null) {
                destCell.setHyperlink(srcCell.getHyperlink());
            }

            // セル型のコピー
            destCell.setCellType(srcCell.getCellType());

            // セルの値をコピー
            switch (srcCell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                destCell.setCellValue(srcCell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                destCell.setCellValue(srcCell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                destCell.setCellErrorValue(srcCell.getErrorCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                destCell.setCellFormula(srcCell.getCellFormula());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                destCell.setCellValue(srcCell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                destCell.setCellValue(srcCell.getRichStringCellValue());
                break;
            }
        }

        // セル結合のコピー
        for (int i = 0; i < srcSheet.getNumMergedRegions(); i++) {
            CellRangeAddress cellRangeAddress = srcSheet.getMergedRegion(i);
            if (cellRangeAddress.getFirstRow() == srcRow.getRowNum()) {
                CellRangeAddress destCellRangeAddress = new CellRangeAddress(descRow.getRowNum(),
                        (descRow.getRowNum() + (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow())),
                        cellRangeAddress.getFirstColumn(), cellRangeAddress.getLastColumn());
                destSheet.addMergedRegion(destCellRangeAddress);
            }
        }
    }

    /**
     * POIで行をコピーする処理
     * @param srcWorkbook ワークブック
     * @param worksheet ワークシート
     * @param srcRowNum コピー元の行インデックス
     * @param destRowNum コピー先の行インデックス
     */
    @SuppressWarnings("deprecation")
    public static void copyRow(Workbook srcWorkbook, String srcSheetName, int srcRowNum, Workbook destWorkbook, String destSheetName, int destRowNum) {

//		Sheet srcSheet = srcWorkbook.getSheetAt(0);
        Sheet srcSheet = srcWorkbook.getSheet(srcSheetName);
        Sheet destSheet = destWorkbook.getSheet(destSheetName);

        Row descRow = destSheet.getRow(destRowNum);
        Row srcRow = srcSheet.getRow(srcRowNum);

        if (descRow != null) {
            // コピー先に行が既に存在する場合、１行下にずらす
        	destSheet.shiftRows(destRowNum, destSheet.getLastRowNum(), 1);
            descRow = destSheet.createRow(destRowNum);
        } else {
            // 存在しない場合は作成
            descRow = destSheet.createRow(destRowNum);
        }

        // セルの型、スタイル、値などをすべてコピーする
        for (int i = 0; i < srcRow.getLastCellNum(); i++) {
            Cell srcCell = srcRow.getCell(i);
            Cell destCell = descRow.createCell(i);

            // コピー元の行が存在しない場合、処理を中断
            if (srcCell == null) {
                destCell = null;
                continue;
            }

            // スタイルのコピー
            CellStyle srcCellStyle = srcWorkbook.createCellStyle();
            srcCellStyle.cloneStyleFrom(srcCell.getCellStyle());

            CellStyle destCellStyle = destWorkbook.createCellStyle();
            destCellStyle.cloneStyleFrom(srcCellStyle);
            destCell.setCellStyle(destCellStyle);

            // コメントのコピー
            if (srcCell.getCellComment() != null) {
                destCell.setCellComment(srcCell.getCellComment());
            }

            // ハイパーリンクのコピー
            if (srcCell.getHyperlink() != null) {
                destCell.setHyperlink(srcCell.getHyperlink());
            }

            // セル型のコピー
            destCell.setCellType(srcCell.getCellType());

            // セルの値をコピー
            switch (srcCell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
            	if (!Strings.isNullOrEmpty(srcCell.getStringCellValue())) {
            		destCell.setCellValue(srcCell.getStringCellValue());
            	}
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                destCell.setCellValue(srcCell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                destCell.setCellErrorValue(srcCell.getErrorCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                destCell.setCellFormula(srcCell.getCellFormula());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                destCell.setCellValue(srcCell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
        		destCell.setCellValue(srcCell.getRichStringCellValue());
                break;
            }
        }

        // セル結合のコピー
        for (int i = 0; i < srcSheet.getNumMergedRegions(); i++) {
            CellRangeAddress cellRangeAddress = srcSheet.getMergedRegion(i);
            if (cellRangeAddress.getFirstRow() == srcRow.getRowNum()) {
                CellRangeAddress destCellRangeAddress = new CellRangeAddress(descRow.getRowNum(),
                        (descRow.getRowNum() + (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow())),
                        cellRangeAddress.getFirstColumn(), cellRangeAddress.getLastColumn());
                destSheet.addMergedRegion(destCellRangeAddress);
            }
        }
    }

}
