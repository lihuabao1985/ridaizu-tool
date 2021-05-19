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

import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.monitorjbl.xlsx.StreamingReader;


/**
 * @author kaho-ri
 *
 */
public class ExcelUtil {

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
        return getTable(filePath, DEFAULT_SHEET, false);
    }

    public static Table<Integer, Integer, String> getTableBySXSSF(String filePath) {
        return getTable(filePath, DEFAULT_SHEET, true);
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

    public static Table<Integer, Integer, String> getTable(String filePath, int sheetIndex, boolean isSXSSF) {
        Table<Integer, Integer, String> table = HashBasedTable.create();
        try {
            Workbook wb = null;

            if (isSXSSF) {
                wb = getWorkbookBySXSSF(filePath);
            } else {
                wb = getWorkbook(filePath);
            }

            //シートを読み込みます。
            Sheet sheet = wb.getSheetAt(sheetIndex);

            Iterator<Row> rowIterator = sheet.rowIterator();
            while(rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                while(cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (isSXSSF) {
                        table.put(row.getRowNum(), cell.getColumnIndex(), getStringValueBySXSSF(cell));
                    } else {
                        table.put(row.getRowNum(), cell.getColumnIndex(), getStringValue(cell));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("処理が失敗しました");
        }

        return table;
    }

    public static Table<Integer, Integer, String> getTable(String filePath, String sheetName) {

        return getTable(filePath, sheetName, false);
    }

    public static Table<Integer, Integer, String> getTableBySXSSF(String filePath, String sheetName) {

        return getTable(filePath, sheetName, true);
    }

    public static Table<Integer, Integer, String> getTable(String filePath, String sheetName, boolean isSXSSF) {
        Table<Integer, Integer, String> table = HashBasedTable.create();
        try {
            Workbook wb = null;
            if (isSXSSF) {
                wb = getWorkbookBySXSSF(filePath);
            } else {
                wb = getWorkbook(filePath);
            }
            //シートを読み込みます。
            Sheet sheet = wb.getSheet(sheetName);

            Iterator<Row> rowIterator = sheet.rowIterator();
            while(rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                while(cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (isSXSSF) {
                        table.put(row.getRowNum(), cell.getColumnIndex(), getStringValueBySXSSF(cell));
                    } else {
                        table.put(row.getRowNum(), cell.getColumnIndex(), getStringValue(cell));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("処理が失敗しました");
        }

        return table;
    }

//    public static Table<Integer, Integer, String> getTable(Workbook wb, int sheetIndex) {
//        Table<Integer, Integer, String> table = HashBasedTable.create();
//        try {
//            //シートを読み込みます。
//            Sheet sheet = wb.getSheetAt(sheetIndex);
//
//            Iterator<Row> rowIterator = sheet.rowIterator();
//            while(rowIterator.hasNext()) {
//                Row row = rowIterator.next();
//                Iterator<Cell> cellIterator = row.cellIterator();
//                while(cellIterator.hasNext()) {
//                    Cell cell = cellIterator.next();
//                    table.put(row.getRowNum(), cell.getColumnIndex(), getStringValue(cell));
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("処理が失敗しました");
//        }
//
//        return table;
//    }


    public static Table<Integer, Integer, String> getTable(Workbook wb, int sheetIndex) {
        Table<Integer, Integer, String> table = HashBasedTable.create();
        try {
            //シートを読み込みます。
            Sheet sheet = wb.getSheetAt(sheetIndex);

            int rowIndex = 0;
            for (Row row : sheet) {

                int cellIndex = 0;
                for (Cell cell : row) {
                    table.put(rowIndex, cellIndex++, getStringValue(cell));
                }

                rowIndex++;
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

    public static Workbook getWorkbookBySXSSF(String filePath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        Workbook workbook = StreamingReader.builder()
                            .rowCacheSize(100)
                            .bufferSize(4096)
                            .open(fileInputStream);
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
        return getStringValue(cell, false);
    }

    public static String getStringValueBySXSSF(Cell cell) {
        return getStringValue(cell, true);
    }

    public static String getStringValue(Cell cell, boolean isSXSSF) {
        String value = null;
        if (isSXSSF) {
            value = cell.getStringCellValue();
        } else {
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
        }
        return value;
    }

    // セルの数式を計算し、Stringとして取得する例
    public static String getStringFormulaValue(Cell cell) {
        assert cell.getCellType() == Cell.CELL_TYPE_FORMULA;

        Workbook book = cell.getSheet().getWorkbook();
        CreationHelper helper = book.getCreationHelper();
        FormulaEvaluator evaluator = helper.createFormulaEvaluator();

        try {
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
        } catch (Exception e) {
            return "";
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
            if (cell == null) {
                cell = createCell(row, columnIndex);
            }
            return cell;
        }
        return null;
    }

    public static void createRow(Sheet sheet, int rowNum, List<String> cellValueList) {
        Row row = createRow(sheet, rowNum);
        for (int i = 0; i < cellValueList.size(); i++) {
            setCellValue(createCell(row, i), cellValueList.get(i));
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

            setCellValue(cell, cellValueList.get(i));
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

    public static void setCellValue(Cell cell, String value) {
        cell.setCellValue(value);
    }

    public static void setCellValue(Cell cell, double value) {
        cell.setCellValue(value);
    }

    public static void setCellFormula(Cell cell, String value) {
        cell.setCellFormula(value);
    }

    public static void setCellFormula(Sheet sheet, int rowIndex, List<Integer> colIndexList, List<String> colValueList) {

        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        for (int i = 0; i < colIndexList.size(); i++) {

            Cell cell = row.getCell(colIndexList.get(i));
            if (cell == null) {
                cell = createCell(row, colIndexList.get(i));
            }

            setCellFormula(cell, colValueList.get(i));
        }
    }

    public static void setRowValue(Row row, List<Integer> colIndexList, List<String> colValueList) {

        if (row == null) {
            return ;
        }

        for (int i = 0; i < colIndexList.size(); i++) {
            setRowValue(row, colIndexList.get(i), colValueList.get(i));
        }

    }

    public static void setRowValue(Sheet sheet, int rowIndex, List<Integer> colIndexList, List<String> colValueList) {

        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        for (int i = 0; i < colIndexList.size(); i++) {
            setRowValue(row, colIndexList.get(i), colValueList.get(i));
        }

    }

    public static void setRowValue(Row row, int colIndex, String colValue) {

        if (row == null) {
            return ;
        }

        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = createCell(row, colIndex);
        }

        setCellValue(cell, colValue);
    }

    public static void setRowValue(Sheet sheet, int rowIndex, int colIndex, String colValue) {

        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = createCell(row, colIndex);
        }

        setCellValue(cell, colValue);
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
    public static void copyRow(Workbook srcWorkbook, int srcRowNum, Workbook destWorkbook, int destRowNum) {

        Sheet srcSheet = srcWorkbook.getSheetAt(0);
        Sheet destSheet = destWorkbook.getSheetAt(0);

        copyRow(srcWorkbook, srcSheet, srcRowNum, destWorkbook, destSheet, destRowNum, true, true);
    }


    /**
     * POIで行をコピーする処理
     * @param srcWorkbook ワークブック
     * @param worksheet ワークシート
     * @param srcRowNum コピー元の行インデックス
     * @param destRowNum コピー先の行インデックス
     */
    public static void copyRow(Workbook srcWorkbook, int srcRowNum, Workbook destWorkbook, String destSheetName, int destRowNum) {

        Sheet srcSheet = srcWorkbook.getSheetAt(0);
        Sheet destSheet = destWorkbook.getSheet(destSheetName);

        copyRow(srcWorkbook, srcSheet, srcRowNum, destWorkbook, destSheet, destRowNum, true, true);
    }

    public static void copyRow(Workbook srcWorkbook, int srcRowNum, Workbook destWorkbook, String destSheetName, int destRowNum, boolean isCopyCellStyle, boolean isCopyCellType) {

        Sheet srcSheet = srcWorkbook.getSheetAt(0);
        Sheet destSheet = destWorkbook.getSheet(destSheetName);

        copyRow(srcWorkbook, srcSheet, srcRowNum, destWorkbook, destSheet, destRowNum, isCopyCellStyle, isCopyCellType);
    }

    /**
     * POIで行をコピーする処理
     * @param srcWorkbook ワークブック
     * @param worksheet ワークシート
     * @param srcRowNum コピー元の行インデックス
     * @param destRowNum コピー先の行インデックス
     */
    public static void copyRow(Workbook srcWorkbook, String srcSheetName, int srcRowNum, Workbook destWorkbook, int destRowNum) {

        Sheet srcSheet = srcWorkbook.getSheet(srcSheetName);
        Sheet destSheet = destWorkbook.getSheetAt(0);

        copyRow(srcWorkbook, srcSheet, srcRowNum, destWorkbook, destSheet, destRowNum, true, true);
    }

   public static void copyRow(Workbook srcWorkbook, String srcSheetName, int srcRowNum, Workbook destWorkbook, int destRowNum, boolean isCopyCellStyle, boolean isCopyCellType) {

       Sheet srcSheet = srcWorkbook.getSheet(srcSheetName);
       Sheet destSheet = destWorkbook.getSheetAt(0);

       copyRow(srcWorkbook, srcSheet, srcRowNum, destWorkbook, destSheet, destRowNum, isCopyCellStyle, isCopyCellType);
   }

    /**
     * POIで行をコピーする処理
     * @param srcWorkbook ワークブック
     * @param worksheet ワークシート
     * @param srcRowNum コピー元の行インデックス
     * @param destRowNum コピー先の行インデックス
     */
    @SuppressWarnings("deprecation")
    public static void copyRow(Workbook srcWorkbook, Sheet srcSheet, int srcRowNum, Workbook destWorkbook, Sheet destSheet, int destRowNum, boolean isCopyCellStyle, boolean isCopyCellType) {

        Row descRow = destSheet.getRow(destRowNum);
        Row srcRow = srcSheet.getRow(srcRowNum);

        if (srcRow == null) {
            return ;
        }

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

            if (isCopyCellStyle) {
                // スタイルのコピー
                CellStyle srcCellStyle = srcWorkbook.createCellStyle();
                srcCellStyle.cloneStyleFrom(srcCell.getCellStyle());

                CellStyle destCellStyle = destWorkbook.createCellStyle();
                destCellStyle.cloneStyleFrom(srcCellStyle);
                destCell.setCellStyle(destCellStyle);
            }

            // コメントのコピー
            if (srcCell.getCellComment() != null) {
                destCell.setCellComment(srcCell.getCellComment());
            }

            // ハイパーリンクのコピー
            if (srcCell.getHyperlink() != null) {
                destCell.setHyperlink(srcCell.getHyperlink());
            }

            if (isCopyCellType) {
                // セル型のコピー
                destCell.setCellType(srcCell.getCellType());
            }

            // セルの値をコピー
//            destCell.setCellValue(getStringValue(srcCell));

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

    public static void copyRow(Workbook srcWorkbook, String srcSheetName, int srcRowNum, Workbook destWorkbook, String destSheetName, int destRowNum) {
        copyRow(srcWorkbook, srcSheetName, srcRowNum, destWorkbook, destSheetName, destRowNum, false, true);
    }


    public static void copyRow(Workbook srcWorkbook, String srcSheetName, int srcRowNum, Workbook destWorkbook, String destSheetName, int destRowNum, boolean isCopyCellStyle) {
        copyRow(srcWorkbook, srcSheetName, srcRowNum, destWorkbook, destSheetName, destRowNum, isCopyCellStyle, true);
    }

    /**
     * POIで行をコピーする処理
     * @param srcWorkbook ワークブック
     * @param worksheet ワークシート
     * @param srcRowNum コピー元の行インデックス
     * @param destRowNum コピー先の行インデックス
     */
    @SuppressWarnings("deprecation")
    public static void copyRow(Workbook srcWorkbook, String srcSheetName, int srcRowNum, Workbook destWorkbook, String destSheetName, int destRowNum, boolean isCopyCellStyle, boolean isCopyCellType) {

//		Sheet srcSheet = srcWorkbook.getSheetAt(0);
        Sheet srcSheet = srcWorkbook.getSheet(srcSheetName);
        Sheet destSheet = destWorkbook.getSheet(destSheetName);

        Row descRow = destSheet.getRow(destRowNum);
        Row srcRow = srcSheet.getRow(srcRowNum);

        if (srcRow == null) {
            return ;
        }

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

            if (isCopyCellStyle) {
                // スタイルのコピー
                CellStyle srcCellStyle = srcWorkbook.createCellStyle();
                srcCellStyle.cloneStyleFrom(srcCell.getCellStyle());

                CellStyle destCellStyle = destWorkbook.createCellStyle();
                destCellStyle.cloneStyleFrom(srcCellStyle);
                destCell.setCellStyle(destCellStyle);
            }

            // コメントのコピー
            if (srcCell.getCellComment() != null) {
                destCell.setCellComment(srcCell.getCellComment());
            }

            // ハイパーリンクのコピー
            if (srcCell.getHyperlink() != null) {
                destCell.setHyperlink(srcCell.getHyperlink());
            }

            if (isCopyCellType) {
                // セル型のコピー
                destCell.setCellType(srcCell.getCellType());
            }

            // セルの値をコピー
            destCell.setCellValue(getStringValue(srcCell));

//            switch (srcCell.getCellType()) {
//            case Cell.CELL_TYPE_BLANK:
//                if (!Strings.isNullOrEmpty(srcCell.getStringCellValue())) {
//                    destCell.setCellValue(srcCell.getStringCellValue());
//                }
//                break;
//            case Cell.CELL_TYPE_BOOLEAN:
//                destCell.setCellValue(srcCell.getBooleanCellValue());
//                break;
//            case Cell.CELL_TYPE_ERROR:
//                destCell.setCellErrorValue(srcCell.getErrorCellValue());
//                break;
//            case Cell.CELL_TYPE_FORMULA:
//                destCell.setCellFormula(srcCell.getCellFormula());
//                break;
//            case Cell.CELL_TYPE_NUMERIC:
//                destCell.setCellValue(srcCell.getNumericCellValue());
//                break;
//            case Cell.CELL_TYPE_STRING:
//                destCell.setCellValue(srcCell.getRichStringCellValue());
//                break;
//            }
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
     * 指定した列をコピーして開始列の前に挿入する
     *
     * @param sheet シート
     * @param target コピーする列
     * @param startRowNo 開始行
     * @param endRowNo 終了行
     * @param columnIndex 開始列
     *
     */
    public static void copyColumn(Sheet srcSheet, int target, int startRowNo, int endRowNo, Sheet destSheet, int columnIndex) {

        Row srcRow;
        Cell srcCell;
        Cell descCell;

        for (int i = startRowNo; i < endRowNo + 1; i++) {
            srcRow = srcSheet.getRow(i);
            Row descRow = destSheet.getRow(i);

            srcCell = srcRow.getCell(target);
            if (srcCell == null) {
                continue;
            }

            descCell = descRow.getCell(columnIndex);
            if (descCell == null) {
                descCell = createCell(descRow, columnIndex);
            }

            // セルの書式をコピー
            descCell.setCellType(srcCell.getCellType());
            descCell.setCellStyle(srcCell.getCellStyle());

            switch (srcCell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                if (!Strings.isNullOrEmpty(srcCell.getStringCellValue())) {
                    descCell.setCellValue(srcCell.getStringCellValue());
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                descCell.setCellValue(srcCell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                descCell.setCellErrorValue(srcCell.getErrorCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                descCell.setCellFormula(srcCell.getCellFormula());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                descCell.setCellValue(srcCell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                descCell.setCellValue(srcCell.getRichStringCellValue());
                break;
            }
        }
    }

    /**
     * 指定した列をコピーして開始列の前に挿入する
     *
     * @param sheet シート
     * @param target コピーする列
     * @param startRowNo 開始行
     * @param endRowNo 終了行
     * @param columnIndex 開始列
     *
     */
    public static void copyColumn(Sheet sheet, int target, int startRowNo, int endRowNo, int columnIndex) {

        Row row;
        Cell fromCell;
        Cell toCell;

        for (int i = startRowNo; i < endRowNo + 1; i++) {
            row = sheet.getRow(i);

            if (row == null) {
                continue;
            }

            fromCell = row.getCell(target);
            if (fromCell == null) {
                continue;
            }

            toCell = row.getCell(columnIndex);
            if (toCell == null) {
                toCell = createCell(row, columnIndex);
            }

            // セルの書式をコピー
            toCell.setCellType(fromCell.getCellType());
            toCell.setCellStyle(fromCell.getCellStyle());

            switch (fromCell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                if (!Strings.isNullOrEmpty(fromCell.getStringCellValue())) {
                    toCell.setCellValue(fromCell.getStringCellValue());
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                toCell.setCellValue(fromCell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                toCell.setCellErrorValue(fromCell.getErrorCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                toCell.setCellFormula(fromCell.getCellFormula());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                toCell.setCellValue(fromCell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                toCell.setCellValue(fromCell.getRichStringCellValue());
                break;
            }
        }
    }

//    /**
//     * 指定した列をコピーして開始列の前に挿入する
//     *
//     * @param sheet シート
//     * @param target コピーする列
//     * @param startRowNo 開始行
//     * @param endRowNo 終了行
//     * @param startColumnNo 開始列
//     * @param endColumnNo 終了列
//     * @param num コピー数
//     *
//     */
//    public static void copyColumn(Sheet sheet, int target, int startRowNo, int endRowNo, int startColumnNo, int endColumnNo, int num) {
//
//        Row row;
//        Cell fromCell;
//        Cell toCell;
//        int cellWidth;
//
//        if (num <= 0) {
//            return;
//        }
//
//        for (int i = startRowNo; i < endRowNo + 1; i++) {
//            row = sheet.getRow(i);
//
//            for (int j = endColumnNo + num; j > startColumnNo - 1; j--) {
//
//                if (j >= startColumnNo + num) {
//                    // 列をずらす
//                    fromCell = row.getCell(j - num);
//                } else {
//                    // target列をコピーする
//                    fromCell = row.getCell(target);
//                }
//                toCell = row.getCell(j);
//
//                if (i == 0) {
//                    // 列幅設定
//                    cellWidth = sheet.getColumnWidth(j);
//                    sheet.setColumnWidth(j + num, cellWidth);
//
//                    // 列の非表示設定
//                    if (sheet.isColumnHidden(j - num)) {
//                        sheet.setColumnHidden(j, true);
//                    } else {
//                        sheet.setColumnHidden(j, false);
//                    }
//                }
//
//                // セルの書式をコピー
//                toCell.setCellType(fromCell.getCellType());
//                toCell.setCellStyle(fromCell.getCellStyle());
//
////                // セルの値をコピー
////                switch (fromCell.getCellType()) {
////                case Cell.CELL_TYPE_NUMERIC:
////                    if (HSSFDateUtil.isCellDateFormatted(fromCell)) {
////                        toCell.setCellValue(fromCell.getDateCellValue());
////                    } else {
////                        toCell.setCellValue(fromCell.getNumericCellValue());
////                    }
////                    break;
////                case Cell.CELL_TYPE_STRING:
////                    toCell.setCellValue(fromCell.getStringCellValue());
////                    break;
////                case Cell.CELL_TYPE_BOOLEAN:
////                    toCell.setCellValue(fromCell.getBooleanCellValue());
////                    break;
////                case Cell.CELL_TYPE_FORMULA:
////                    toCell.setCellValue(fromCell.getCellFormula());
////                    break;
////                case Cell.CELL_TYPE_BLANK:
////                    break;
////                }
//
//
//                switch (fromCell.getCellType()) {
//                case Cell.CELL_TYPE_BLANK:
//                    if (!Strings.isNullOrEmpty(fromCell.getStringCellValue())) {
//                        toCell.setCellValue(fromCell.getStringCellValue());
//                    }
//                    break;
//                case Cell.CELL_TYPE_BOOLEAN:
//                    toCell.setCellValue(fromCell.getBooleanCellValue());
//                    break;
//                case Cell.CELL_TYPE_ERROR:
//                    toCell.setCellErrorValue(fromCell.getErrorCellValue());
//                    break;
//                case Cell.CELL_TYPE_FORMULA:
//                    toCell.setCellFormula(fromCell.getCellFormula());
//                    break;
//                case Cell.CELL_TYPE_NUMERIC:
//                    toCell.setCellValue(fromCell.getNumericCellValue());
//                    break;
//                case Cell.CELL_TYPE_STRING:
//                    toCell.setCellValue(fromCell.getRichStringCellValue());
//                    break;
//                }
//            }
//        }
//    }

    /**
     *  删除列
    * @param sheet
    * @param columnToDelete
    */
   public static void deleteColumn(Sheet sheet, int columnToDelete) {
       for (int rId = 0; rId <= sheet.getLastRowNum(); rId++) {
           Row row = sheet.getRow(rId);
           for (int cID = columnToDelete; cID <= row.getLastCellNum(); cID++) {
               Cell cOld = row.getCell(cID);
               if (cOld != null) {
                   row.removeCell(cOld);
               }
               Cell cNext = row.getCell(cID + 1);
               if (cNext != null) {
                   Cell cNew = row.createCell(cID, cNext.getCellTypeEnum());
                   cloneCell(cNew, cNext);
                   if (rId == 0) {
                       sheet.setColumnWidth(cID, sheet.getColumnWidth(cID + 1));

                   }
               }
           }
       }
   }

   /**
    * 右边列左移
    * @param cNew
    * @param cOld
    */
   private static void cloneCell(Cell cNew, Cell cOld) {
       cNew.setCellComment(cOld.getCellComment());
       cNew.setCellStyle(cOld.getCellStyle());

       if (CellType.BOOLEAN == cNew.getCellTypeEnum()) {
           cNew.setCellValue(cOld.getBooleanCellValue());
       } else if (CellType.NUMERIC == cNew.getCellTypeEnum()) {
           cNew.setCellValue(cOld.getNumericCellValue());
       } else if (CellType.STRING == cNew.getCellTypeEnum()) {
           cNew.setCellValue(cOld.getStringCellValue());
       } else if (CellType.ERROR == cNew.getCellTypeEnum()) {
           cNew.setCellValue(cOld.getErrorCellValue());
       } else if (CellType.FORMULA == cNew.getCellTypeEnum()) {
           cNew.setCellValue(cOld.getCellFormula());
       }
   }

   public static void setValidationData(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol,
           String[] dataArray) {

       if (sheet == null) {
           return ;
       }

       // 入力規則 Helper
       DataValidationHelper dvHelper = null;
       if (sheet instanceof XSSFSheet) {
           dvHelper = new XSSFDataValidationHelper((XSSFSheet)sheet);
       } else {
           dvHelper = new HSSFDataValidationHelper((HSSFSheet)sheet);
       }

       // 入力データ
       DataValidationConstraint dvConstraint = (DataValidationConstraint) dvHelper
               .createExplicitListConstraint(dataArray);

       // 入力規則を適用させるセルの範囲   (int firstRow, int lastRow, int firstCol, int lastCol)
       CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);

       // 入力規則の定義
       DataValidation validation = (DataValidation) dvHelper.createValidation(dvConstraint, addressList);

       // ドロップダウンリストから選択できるようにする
       validation.setShowErrorBox(true);

       // データの入力規則を設定する
       sheet.addValidationData(validation);
   }

   public static void setForceFormulaRecalculation(Workbook workbook) {
       // エクセル起動する時、公式を実行するように
       int numberOfSheets = workbook.getNumberOfSheets();
       for (int i = 0; i < numberOfSheets; i++) {
           workbook.getSheetAt(i).setForceFormulaRecalculation(true);
       }
   }

   public static void setCellFontColor(Workbook workbook, Cell cell, short color) {

       //フォントを定義する
       Font font = workbook.createFont();
       // 色
       font.setColor(color);

       //セルのフォントを設定する
       CellStyle style = workbook.createCellStyle();
       style.setFont(font);
       cell.setCellStyle(style);
   }

   public static void setCellFontBold(Workbook workbook, Cell cell) {
       setCellFontBold(workbook, cell, (short)14);
   }

   public static void setCellFontBold(Workbook workbook, Cell cell, short points) {
       Font font = workbook.createFont();
       font.setBold(true);
       font.setFontHeightInPoints(points);

       CellStyle style = workbook.createCellStyle();
       style.setFont(font);
       cell.setCellStyle(style);
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

   public static void setFillForegroundColor(Workbook workbook, Cell cell, IndexedColors color) {
       CellStyle cellstyle = workbook.createCellStyle();
       cellstyle.setFillForegroundColor(color.index);  //色の指定
       cellstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);    //塗り潰し
       cell.setCellStyle(cellstyle);
   }

   public static void copyCellStyle(Workbook workbook, Cell srcCell, Cell destCell) {
       // スタイルのコピー
       CellStyle srcCellStyle = workbook.createCellStyle();
       srcCellStyle.cloneStyleFrom(srcCell.getCellStyle());

       CellStyle destCellStyle = workbook.createCellStyle();
       destCellStyle.cloneStyleFrom(srcCellStyle);
       destCell.setCellStyle(destCellStyle);
   }

}
