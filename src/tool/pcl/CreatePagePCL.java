package tool.pcl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import common.Common;
import common.ExcelUtil;
import config.Config;

public class CreatePagePCL {

    public static final String PAGE_ID = Config.getString("PAGE_ID");
    public static final String PAGE_NAME = Config.getString("PAGE_NAME");
    public static final String PGM_ID = Config.getString("PGM_ID");

    // ファイル出力フォルダー
    public static final String TEMPLATE_FILEPATH = Config.getString("TEMPLATE_FILEPATH", "template");
    static final String FILE_P_SUB_ACCESS_DB = TEMPLATE_FILEPATH + File.separator + "P_全SUB_ACCSESS_DB.xlsx";
    private static final Table<Integer, Integer, String> table1 = ExcelUtil.getTableBySXSSF(TEMPLATE_FILEPATH + File.separator + String.format("03B0100_画面レイアウト_%s_%s.xlsx", PAGE_ID, PAGE_NAME), "画面レイアウト");
    private static final Table<Integer, Integer, String> table2 = ExcelUtil.getTableBySXSSF(TEMPLATE_FILEPATH + File.separator + String.format("03B0200_画面項目一覧_%s_%s.xlsx", PAGE_ID, PAGE_NAME), "画面項目一覧");
    private static final Table<Integer, Integer, String> table3 = ExcelUtil.getTableBySXSSF(TEMPLATE_FILEPATH + File.separator + String.format("04C0900_オンライン処理設計書_%s_%s.xlsx", PAGE_ID, PAGE_NAME), "イベント一覧");
    private static final Table<Integer, Integer, String> templateTable = ExcelUtil.getTable(TEMPLATE_FILEPATH + File.separator + Config.getString("CREATE_PAGE_PCL_TEMPLATE"), "PCL (オン①)");

    private static final List<String> BUTTON_EXCLUSION_LIST = Arrays.asList("HELP", "終了");
    private static final List<String> BUTTON_TYPE_INCLUDED_LIST = Arrays.asList("CHECK", "RADIO", "SELECTBOX");
    private static final Map<String, String> buttonNameMap = new HashMap<String, String>();
    static {
        buttonNameMap.put("LABEL", "ラベル");
        buttonNameMap.put("CHECK", "チェックボックス");
        buttonNameMap.put("RADIO", "ラジオボックス");
        buttonNameMap.put("SELECTBOX", "プルダウン");
        buttonNameMap.put("TEXT", "テキストボックス");
        buttonNameMap.put("BUTTON", "ﾎﾞﾀﾝ群");
    }

    private static final List<String> ITEM_TYPE_INPUT_LIST = Arrays.asList("入出力", "入力");
    private static final List<String> ITEM_TYPE_OUTPUT_LIST = Arrays.asList("入出力", "出力");
    private static final List<String> HEADER_EXCLUSION_LIST = Arrays.asList("メッセージ");
    private static final List<String> FULL_NUMBER_DIGITS_EXCLUSION_LIST = Arrays.asList("件数", "現頁", "全頁", "ＳＥＱ");

    public static final String Y = "Y";
    public static final String N = "N";
    public static final String E = "E";
    public static final String L = "L";
    public static final String I = "I";
    public static final String U = "U";
    public static final String D = "D";
    public static final String S = "S";
    public static final String F = "F";


    public static void main(String[] args) throws IOException {

        Workbook workbook = ExcelUtil.getWorkbook("template\\量産用標準チェックリスト（オンライン）正式版_ver2.xls");
        setOn1(workbook);
        setOn2(workbook);
        setOn3(workbook);

    	if (isUseUpdateButton()) {
    		setOn4(workbook);
    	}

        setOn5(workbook);

        ExcelUtil.save(String.format("%s_%s_量産用標準チェックリスト（オンライン）.xls", PAGE_ID, PAGE_NAME), workbook);

    }

    private static void setOn1(Workbook workbook) {
    	System.out.println("Start set On1.");

        Sheet sheet = workbook.getSheet("PCL (オン①)");

        List<Integer> unusedButtonRowNoList = getUnusedButtonRowNoList();
        List<String> addButtonList = getAddButtonList();

        int startColNo = 15;
        Map<String, Collection<String>> itemMap = getItemMap(ITEM_TYPE_INPUT_LIST);
        for (Entry<String, Collection<String>> entry : itemMap.entrySet()) {
            String key = entry.getKey();
            List<String> list = Lists.newArrayList(entry.getValue());

            startColNo = setOn1ColValue(workbook, sheet, key, list, startColNo);
        }

        Map<String, Collection<String>> controlMap = getControlMap();
        for (Entry<String, Collection<String>> entry : controlMap.entrySet()) {
            String key = buttonNameMap.get(entry.getKey());
            List<String> list = Lists.newArrayList(entry.getValue());

            startColNo = setOn1ColValue(workbook, sheet, key, list, startColNo);
        }

        String key = "ﾎﾞﾀﾝ群";
        startColNo = setOn1ColValue(workbook, sheet, key, addButtonList, startColNo);

        // ボタン設定
        startColNo = 3;
        for (int rowNo : unusedButtonRowNoList) {
			Row row = sheet.getRow(rowNo);

			String cellValue = ExcelUtil.getStringValue(row.getCell(2));
			for (int i = startColNo; i < row.getLastCellNum(); i++) {
				ExcelUtil.setCellValue(row.getCell(i), null);

				if (!Strings.isNullOrEmpty(cellValue)) {
					ExcelUtil.setCellValue(sheet.getRow(rowNo + 1).getCell(i), null);
				}
			}
		}

        String lastColIndexName= null;
        Row row2 = sheet.getRow(3);
        for (int i = startColNo; i < row2.getLastCellNum(); i++) {
            String colIndexName = Common.num2alphabet(i + 1);
            ExcelUtil.setCellFormula(sheet.getRow(21).getCell(i), String.format("COUNTA(%s7:%s21)", colIndexName, colIndexName));

            lastColIndexName = colIndexName;
		}

        // 統計
        // PCL件数合計
        ExcelUtil.setCellFormula(sheet.getRow(21).getCell(2), String.format("SUM(D22:%s22)", lastColIndexName));
        // PCL実施数合計
        ExcelUtil.setCellFormula(sheet.getRow(22).getCell(2), String.format("SUM(D23:%s23)", lastColIndexName));


        //列幅の自動調整
        for (Row row : sheet) {
            int lastCellNum = row.getLastCellNum();
            for (int i = 15; i < lastCellNum; i++) {
                sheet.autoSizeColumn(i, true);
            }
        }

    	System.out.println("End set On1.");
    }

    private static void setOn2(Workbook workbook) {
    	System.out.println("Start set On2.");

        Sheet sheet = workbook.getSheet("PCL (オン②)");

        Map<String, Collection<String>> controlMap = getControlMap();
        Map<String, String> itemControlMap = getItemControlMap();
    	Map<String, Collection<String>> itemMap = getItemMap(ITEM_TYPE_OUTPUT_LIST);
    	Map<String, Collection<String>> charTypeMap = getCharTypeMap();
    	Map<String, String> itemDomainMap = getItemDomainMap();

        Map<String, Integer> itemColIndexMap = new HashMap<String, Integer>();
        int startColNo = 7;
        for (Entry<String, Collection<String>> entry : itemMap.entrySet()) {
            String key = entry.getKey();

            if (HEADER_EXCLUSION_LIST.contains(key)) {
            	continue;
            }

            List<String> list = Lists.newArrayList(entry.getValue());

            startColNo = setOn2ColValue(workbook, sheet, key, list, startColNo, itemColIndexMap);
        }

        startColNo = 7;
        Row row2 = sheet.getRow(1);
        for (int i = startColNo; i < row2.getLastCellNum(); i++) {
            ExcelUtil.copyColumn(sheet, i, 0, sheet.getLastRowNum(), i - 1);
		}


        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
        	Row row = sheet.getRow(i);
        	Cell cell = row.getCell(row.getLastCellNum() - 1);
        	CellStyle style = workbook.createCellStyle();
            cell.setCellStyle(style);
        	cell.setCellValue("");
        }

        // レイアウト確認
        if (!isExistSelectBox(controlMap)) {
        	// プルダウンがある場合
        	ExcelUtil.setCellValue(sheet.getRow(10).getCell(5), null);
        }

        if (!isExistCheckbox(controlMap)) {
        	// チェックボックスがある場合
        	ExcelUtil.setCellValue(sheet.getRow(11).getCell(5), null);
        }

        if (!isExistRadio(controlMap)) {
        	// ラジオボックスがある場合
        	ExcelUtil.setCellValue(sheet.getRow(12).getCell(5), null);
        }

        if (!isExistDetailList(itemMap)) {
        	// 明細行がある場合
        	ExcelUtil.setCellValue(sheet.getRow(15).getCell(5), null);
        	ExcelUtil.setCellValue(sheet.getRow(16).getCell(5), null);
        }


        // 個別確認
        List<String> detailColNameList = getDetailColNameList(itemMap);
        for (Entry<String, Integer> entry : itemColIndexMap.entrySet()) {
        	String item = entry.getKey().split("####")[1];
        	Integer itemColIndex = entry.getValue();

            // ------------------名前項目確認------------------
        	if (isName(itemDomainMap, item) ) {
                // 個人の場合
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 27, itemColIndex), N);
                // 法人の場合
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 28, itemColIndex), N);
                // 特約店の場合
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 29, itemColIndex), N);
                // ＮＴユーザの場合
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 30, itemColIndex), N);
        	}


        	// ------------------電話番号------------------
        	if (item.contains("携帯電話番号")) {
        		// 携帯電話番号
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 34, itemColIndex), N);
        	} else if (item.contains("電話番号")) {
        		// 電話番号
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 33, itemColIndex), N);
        	}

        	// ------------------登録番号------------------
        	if (item.contains("登録番号") && item.contains("陸事") && !item.contains("陸事名称")) {
                // 登録番号－陸事
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 35, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 36, itemColIndex), N);
        	}

        	if (item.contains("登録番号") && item.contains("陸事名称")) {
                // 登録番号－陸事名称
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 37, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 38, itemColIndex), N);
        	}

        	if (item.contains("登録番号") && item.contains("車両区分")) {
                // 登録番号－車両区分
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 39, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 40, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 41, itemColIndex), N);
        	}

        	if (item.contains("登録番号") && item.contains("カナ")) {
                // 登録番号－カナ
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 42, itemColIndex), N);
        	}

        	if (item.contains("登録番号") && item.contains("連番")) {
                // 登録番号－連番
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 43, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 44, itemColIndex), N);
        	}

        	// 登録番号
        	if ("登録番号".equals(item)) {
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 35, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 36, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 37, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 38, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 39, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 40, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 41, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 42, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 43, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 44, itemColIndex), N);
        	}

        	// ------------------日付時刻項目確認------------------
        	if (isDate(charTypeMap, item)) {
        		// 区切り記号
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 46, itemColIndex), N);
        		// 過去日
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 47, itemColIndex), N);
        		// 未来日
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 48, itemColIndex), N);
        	}
        	// 和暦表示
        	if (item.contains("元号") || item.contains("和暦") || item.contains("和年")) {
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 49, itemColIndex), N);
        	}

        	// ------------------小数点あり数値------------------
        	if (isDecimal(charTypeMap, item)) {
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 51, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 52, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 53, itemColIndex), N);
        	}

        	// ------------------金額・件数------------------
        	if (item.contains("金額") || item.contains("件数")) {
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 55, itemColIndex), N);
        		if (item.contains("金額")) {
            		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 56, itemColIndex), N);
            		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 57, itemColIndex), N);
        		}
        	}

        	// ------------------プルダウン項目確認------------------
        	if ("プルダウン".equals(itemControlMap.get(item))) {
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 59, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 60, itemColIndex), N);
        	}

        	// ------------------明細行確認------------------
        	if (detailColNameList.contains(item)) {
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 62, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 63, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 64, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 65, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 66, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 67, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 68, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 69, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 70, itemColIndex), N);
        	}

            // 項目共通確認
        	// FULL桁表示確認
        	if (isFullNumberDigitsExclusionList(item) ||
    			checkControlType(itemControlMap, item, "チェックボックス") ||
    			checkControlType(itemControlMap, item, "ラジオボックス") ||
    			checkControlType(itemControlMap, item, "プルダウン") ||
    			checkControlType(itemControlMap, item, "ﾎﾞﾀﾝ群")) {
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 22, itemColIndex), null);
        	}

            // 数字項目の最小値確認
            // 出力値数字項目ゼロ確認
            if ((isNumber(charTypeMap, item) || isDecimal(charTypeMap, item)) &&
        		 (checkControlType(itemControlMap, item, "ラベル") || checkControlType(itemControlMap, item, "テキストボックス"))) {
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 23, itemColIndex), N);
        		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 24, itemColIndex), N);
            }

		}

        // その他の確認
        if(!isUseUpdateButton()) {
        	// 更新ボタンがない場合
    		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 76, 5), null);
    		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 77, 5), null);
        }

        if(!isUsePrintButton()) {
        	// 印刷ボタンがない場合
    		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 79, 5), null);
    		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 80, 5), null);
        }

        if(!isUseCancelButton()) {
        	// 取消ボタンがない場合
    		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 82, 5), null);
    		ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 83, 5), null);
        }

        String lastColIndexName= null;
        startColNo = 5;
        for (int i = startColNo; i < row2.getLastCellNum() - 1; i++) {
            String colIndexName = Common.num2alphabet(i + 1);
            ExcelUtil.setCellFormula(sheet.getRow(84).getCell(i), String.format("COUNTA(%s4:%s84)", colIndexName, colIndexName));

            lastColIndexName = colIndexName;
		}

        // PCL件数合計
        ExcelUtil.setCellFormula(sheet.getRow(84).getCell(3), String.format("SUM(F85:%s85)", lastColIndexName));
        // PCL実施数合計
        ExcelUtil.setCellFormula(sheet.getRow(85).getCell(3), String.format("SUM(F86:%s86)", lastColIndexName));

        //列幅の自動調整
        for (Row row : sheet) {
            int lastCellNum = row.getLastCellNum();
            for (int i = 6; i < lastCellNum; i++) {
                sheet.autoSizeColumn(i, true);
            }
        }

    	System.out.println("End set On2.");
    }

    private static void setOn3(Workbook workbook) {
    	System.out.println("Start set On3.");

    	System.out.println("End set On3.");
    }

    private static void setOn4(Workbook workbook) {
    	System.out.println("Start set On4.");

    	if (!isUseUpdateButton()) {
        	System.out.println("End set On4.");
    		return ;
    	}

    	Table<Integer, Integer, String> table = getPgmTable(PGM_ID);
    	int rowSize = table.rowKeySet().size();

        // PCL (入力件数パターン確認)
        // SF List
        List<Integer> sfList = new ArrayList<Integer>();

        // PCL (マスタ確認)
        // マスタ List
        List<Integer> masterList = new ArrayList<Integer>();

        // PCL (更新エラー確認)
        // IUD List
        List<Integer> iudList = new ArrayList<Integer>();

        for (int rowNo = 0; rowNo < rowSize; rowNo++) {
            if (Strings.isNullOrEmpty(table.get(rowNo, 1))) {
                continue;
            }

            // 論理名
            String tableName = table.get(rowNo, 1);
            // 物理名
            String tableNameStr = table.get(rowNo, 2);
            // 操作区分
            String kubun = table.get(rowNo, 3);

            boolean isS = kubun.contains(S);
            boolean isF = kubun.contains(F);
            boolean isI = kubun.contains(I);
            boolean isU = kubun.contains(U);
            boolean isD = kubun.contains(D);

            if ((isS || isF) && !tableNameStr.contains("マスタ")) {
                sfList.add(rowNo);
            }

            if (isI || isU || isD) {
                iudList.add(rowNo);
            }

            if (tableNameStr.contains("マスタ") && !"PV".equals(tableName.substring(0, 2))) {
                masterList.add(rowNo);
            }
        }

        if (iudList.isEmpty()) {
        	return ;
        }

        Sheet sheet = workbook.getSheet("PCL (オン④)");
        String lastColIndexName= null;
        int lastRowNum = sheet.getLastRowNum();
        int startColNo = 6;
        int count = 1;
        for (int rowNo : iudList) {
        	// 論理名
            String tableName = table.get(rowNo, 1);
            // 操作区分
            String kubun = table.get(rowNo, 3);

            ExcelUtil.copyColumn(sheet, 6, 0, lastRowNum, startColNo);
            ExcelUtil.setCellValue(sheet.getRow(0).getCell(startColNo), Common.leftFilling(count++, 3));
            ExcelUtil.setCellValue(sheet.getRow(2).getCell(startColNo), tableName);
            ExcelUtil.setCellValue(sheet.getRow(5).getCell(startColNo), E);
            ExcelUtil.setCellValue(sheet.getRow(6).getCell(startColNo), E);
            ExcelUtil.setCellValue(sheet.getRow(7).getCell(startColNo), E);
            ExcelUtil.setCellValue(sheet.getRow(8).getCell(startColNo), E);
            ExcelUtil.setCellValue(sheet.getRow(9).getCell(startColNo), E);

            if (kubun.contains("I")) {
                ExcelUtil.setCellValue(sheet.getRow(11).getCell(startColNo), E);
            }

            if (kubun.contains("U")) {
                ExcelUtil.setCellValue(sheet.getRow(12).getCell(startColNo), E);
            }

            if (kubun.contains("D")) {
                ExcelUtil.setCellValue(sheet.getRow(13).getCell(startColNo), E);
            }

            String colIndexName = Common.num2alphabet(startColNo + 1);
            ExcelUtil.setCellFormula(sheet.getRow(14).getCell(startColNo), String.format("COUNTA(%s5:%s14)", colIndexName, colIndexName));

            startColNo++;
            lastColIndexName = colIndexName;
		}

//        // PCL件数合計
//        ExcelUtil.setCellFormula(sheet.getRow(14).getCell(2), String.format("SUM(G15:%s15)", lastColIndexName));
//        // PCL実施数合計
//        ExcelUtil.setCellFormula(sheet.getRow(15).getCell(2), String.format("SUM(G16:%s16)", lastColIndexName));

        //列幅の自動調整
        for (Row row : sheet) {
            int lastCellNum = row.getLastCellNum();
            for (int i = 6; i < lastCellNum; i++) {
            	if (i > 5) {
            		sheet.autoSizeColumn(i, true);
            	}
            }
        }

    	System.out.println("End set On4.");
    }

    private static void setOn5(Workbook workbook) throws IOException {
    	System.out.println("Start set On5.");

        Sheet sheet = workbook.getSheet("PCL (オン⑤)");

    	Map<String, Collection<String>> itemMap = getItemMap(ITEM_TYPE_OUTPUT_LIST);
        if (isExistDetailList(itemMap)) {
        	// 明細が存在する場合
        	ExcelUtil.setCellValue(sheet.getRow(5).getCell(6), N);
        	ExcelUtil.setCellValue(sheet.getRow(5).getCell(7), N);
        	ExcelUtil.setCellValue(sheet.getRow(5).getCell(8), N);
        	ExcelUtil.setCellValue(sheet.getRow(6).getCell(5), N);
        }

        if (isUseUpdateButton()) {
        	// 更新ボタンが使用される場合
        	ExcelUtil.setCellValue(sheet.getRow(7).getCell(5), N);
        	ExcelUtil.setCellValue(sheet.getRow(8).getCell(5), N);
        	ExcelUtil.setCellValue(sheet.getRow(9).getCell(5), N);
        }

        if (isUsePrintButton()) {
        	// 印刷ボタンが使用される場合
        	ExcelUtil.setCellValue(sheet.getRow(10).getCell(5), N);
        }

        String lastColIndexName= null;
        int startColNo = 5;
        Row row = sheet.getRow(0);
        for (int i = startColNo; i < row.getLastCellNum() - 1; i++) {
            String colIndexName = Common.num2alphabet(i + 1);
            ExcelUtil.setCellFormula(sheet.getRow(13).getCell(i), String.format("COUNTA(%s5:%s13)", colIndexName, colIndexName));

            lastColIndexName = colIndexName;
		}

        // PCL件数合計
        ExcelUtil.setCellFormula(sheet.getRow(13).getCell(2), String.format("SUM(F14:%s14)", lastColIndexName));
        // PCL実施数合計
        ExcelUtil.setCellFormula(sheet.getRow(14).getCell(2), String.format("SUM(F15:%s15)", lastColIndexName));

    	System.out.println("End set On5.");
    }

    private static int setOn1ColValue(Workbook workbook, Sheet sheet, String headerValue, List<String> itemList, int startColNo) {
    	boolean isSetHeader = false;
        for (int i = 0; i < itemList.size(); i++) {
		 	String item = itemList.get(i);

            if (!isSetHeader) {
                ExcelUtil.copyColumn(sheet, 3, 4, sheet.getLastRowNum(), startColNo);
            	ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 4, startColNo), headerValue);
            	isSetHeader = true;
            } else {

                ExcelUtil.copyColumn(sheet, 14, 4, sheet.getLastRowNum(), startColNo);
            }

            ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 3, startColNo), Common.leftFilling(startColNo - 2, 3));
            ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 5, startColNo), item);

            startColNo++;
        }

        return startColNo;
    }

    private static int setOn2ColValue(Workbook workbook, Sheet sheet, String headerValue, List<String> itemList, int startColNo, Map<String, Integer> itemIndexMap) {
    	boolean isSetHeader = false;
        for (int i = 0; i < itemList.size(); i++) {
		 	String item = itemList.get(i);

            ExcelUtil.copyColumn(sheet, 6, 1, sheet.getLastRowNum(), startColNo);

            if (!isSetHeader) {
            	Cell cell = ExcelUtil.getCell(sheet, 1, startColNo);
            	ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 1, startColNo), headerValue);

                // スタイルのコピー
                CellStyle srcCellStyle = workbook.createCellStyle();
                srcCellStyle.cloneStyleFrom(ExcelUtil.getCell(sheet, 1, 5).getCellStyle());

                CellStyle destCellStyle = workbook.createCellStyle();
                destCellStyle.cloneStyleFrom(srcCellStyle);
                cell.setCellStyle(destCellStyle);


            	isSetHeader = true;
            }

            ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 0, startColNo), Common.leftFilling(startColNo - 5, 3));
            ExcelUtil.setCellValue(ExcelUtil.getCell(sheet, 2, startColNo), item);

            itemIndexMap.put(String.format("%s####%s", headerValue, item), startColNo - 1);

            startColNo++;
        }

        return startColNo;
    }

    private static Map<String, Collection<String>>  getItemMap(List<String> itemTypeList) {
    	 List<Integer> rowNoList1 = new ArrayList<Integer>();
         List<Integer> rowNoList2 = new ArrayList<Integer>();
         int rowSize = table1.rowKeySet().size();
         for (int rowNo = 0; rowNo < rowSize; rowNo++) {
             if (Strings.isNullOrEmpty(table1.get(rowNo, 0)) ||
                 	!(table1.get(rowNo, 0).contains("項目概要") ||
                 	table1.get(rowNo, 0).contains("備考"))) {
                 continue;
             }

             if (table1.get(rowNo, 0).contains("項目概要")) {

                 rowNoList1.add(rowNo);
             } else if (table1.get(rowNo, 0).contains("備考")) {

                 rowNoList2.add(rowNo);
             }
         }

         int startRowNo = rowNoList1.get(0);
         int endRowNo = rowNoList2.isEmpty() ? table1.size() : rowNoList2.get(0);

         Multimap<String, String> itemMultimap = LinkedHashMultimap.create();
         String itemKey = null;
         for (int rowNo = startRowNo; rowNo < endRowNo; rowNo++) {

             if (Strings.isNullOrEmpty(table1.get(rowNo, 1)) ||
                 	table1.get(rowNo, 1).contains("#")) {
                 continue;
             }

             if (Strings.isNullOrEmpty(table1.get(rowNo, 3))) {
             	itemKey = table1.get(rowNo, 1);
             	continue;
             }

             // 入出力確認
             String str = table1.get(rowNo, 17);
             if (itemTypeList.contains(str.trim())) {
            	 itemMultimap.put(itemKey, table1.get(rowNo, 3));
//            	 System.out.println(String.format("%s\t%s\t%s", itemKey, table1.get(rowNo, 3), str));
             }
         }

         return itemMultimap.asMap();
    }

    private static Map<String, Collection<String>> getControlMap() {

        Multimap<String, String> multimap = ArrayListMultimap.create();
        Map<Integer, Map<Integer, String>> rowMap = table2.rowMap();
		for (Entry<Integer, Map<Integer, String>> entry : rowMap.entrySet()) {
			Map<Integer, String> value = entry.getValue();

            if (Strings.isNullOrEmpty(value.get(3)) ||
                !BUTTON_TYPE_INCLUDED_LIST.contains(value.get(9))) {
                continue;
            }

            multimap.put(value.get(9), value.get(3));
		}

        Map<String, Collection<String>> map = multimap.asMap();

        return map;
    }

    private static Map<String, Collection<String>> getCharTypeMap() {
        Multimap<String, String> multimap = ArrayListMultimap.create();
        Map<Integer, Map<Integer, String>> rowMap = table2.rowMap();
		for (Entry<Integer, Map<Integer, String>> entry : rowMap.entrySet()) {
			Map<Integer, String> value = entry.getValue();

			if (Strings.isNullOrEmpty(value.get(3)) ||
					value.get(14).contains("-")) {
				continue;
			}

//			System.out.println(String.format("%s\t%s -> %s", value.get(0), value.get(14), value.get(3)));
			multimap.put(value.get(14), value.get(3));
		}

//        int rowSize = table2.rowKeySet().size();
//        for (int rowNo = 0; rowNo < rowSize; rowNo++) {
////            if (Strings.isNullOrEmpty(table2.get(rowNo, 3)) ||
////        		table2.get(rowNo, 14).contains("-")) {
////                continue;
////            }
//
//            System.out.println(String.format("%s\t%s -> %s", table2.get(rowNo, 0), table2.get(rowNo, 14), table2.get(rowNo, 3)));
//
//            multimap.put(table2.get(rowNo, 14), table2.get(rowNo, 3));
//        }

        Map<String, Collection<String>> map = multimap.asMap();

        return map;
    }

    private static Map<String, String> getItemDomainMap() {

        Map<String, String> map = new HashMap<String, String>();
        Map<Integer, Map<Integer, String>> rowMap = table2.rowMap();
		for (Entry<Integer, Map<Integer, String>> entry : rowMap.entrySet()) {
			Map<Integer, String> value = entry.getValue();
			map.put(value.get(3), value.get(20));
		}

        return map;
    }

    private static Map<String, String> getItemControlMap() {

        Map<String, String> map = new HashMap<String, String>();
        Map<Integer, Map<Integer, String>> rowMap = table2.rowMap();
		for (Entry<Integer, Map<Integer, String>> entry : rowMap.entrySet()) {
			Map<Integer, String> value = entry.getValue();
        	if (Strings.isNullOrEmpty(buttonNameMap.get(value.get(9)))) {
        		map.put(value.get(3), value.get(9));
        	} else {
        		map.put(value.get(3), buttonNameMap.get(value.get(9)));
        	}
		}

        return map;
    }

    private static List<String> getAllButtonList() {
        List<String> allButtonList = new ArrayList<String>();
        int rowSize = table3.rowKeySet().size();
        for (int rowNo = 0; rowNo < rowSize; rowNo++) {
            if (Strings.isNullOrEmpty(table3.get(rowNo, 2)) ||
                !table3.get(rowNo, 2).contains("「")) {
                continue;
            }
            String buttonName = table3.get(rowNo, 2);
            buttonName = buttonName.substring(buttonName.indexOf("「") + 1, buttonName.indexOf("」"));
            if (BUTTON_EXCLUSION_LIST.contains(buttonName)) {
                continue;
            }

            allButtonList.add(buttonName.replaceAll("ﾁｪｯｸ", "チェック"));
        }

        return allButtonList;
    }

    private static List<String> getUnusedButtonNameList() {
        List<String> unusedButtonNameList = new ArrayList<String>();

        int rowSize = table3.rowKeySet().size();
        for (int rowNo = 0; rowNo < rowSize; rowNo++) {
            if (Strings.isNullOrEmpty(table3.get(rowNo, 2)) ||
                !table3.get(rowNo, 2).contains("「")) {
                continue;
            }
            String buttonName = table3.get(rowNo, 2);
            buttonName = buttonName.substring(buttonName.indexOf("「") + 1, buttonName.indexOf("」"));
            if (BUTTON_EXCLUSION_LIST.contains(buttonName)) {
                continue;
            }

            if (Strings.isNullOrEmpty(table3.get(rowNo, 18)) || table3.get(rowNo, 18).contains("未使用")) {
            	unusedButtonNameList.add(buttonName);
            }
        }

        return unusedButtonNameList;
    }

    private static List<String> getCommonButtonList() {
        List<String> commonButtonList = new ArrayList<String>();

        int rowSize = templateTable.rowKeySet().size();
        for (int rowNo = 0; rowNo < rowSize; rowNo++) {
            if (Strings.isNullOrEmpty(templateTable.get(rowNo, 0)) ||
                Strings.isNullOrEmpty(templateTable.get(rowNo, 1)) ||
                !NumberUtils.isNumber(templateTable.get(rowNo, 0))) {
                continue;
            }

            commonButtonList.add(templateTable.get(rowNo, 1));
        }

        return commonButtonList;
    }

    private static List<Integer> getUnusedButtonRowNoList() {

        List<Integer> unusedButtonRowNoList = new ArrayList<Integer>();
        List<String> unusedButtonNameList = getUnusedButtonNameList();

        int rowSize = templateTable.rowKeySet().size();
        for (int rowNo = 0; rowNo < rowSize; rowNo++) {
            if (Strings.isNullOrEmpty(templateTable.get(rowNo, 0)) ||
                Strings.isNullOrEmpty(templateTable.get(rowNo, 1)) ||
                !NumberUtils.isNumber(templateTable.get(rowNo, 0))) {
                continue;
            }


            for (String unusedButtonName : unusedButtonNameList) {
				if (templateTable.get(rowNo, 1).contains(unusedButtonName)) {
					unusedButtonRowNoList.add(rowNo);
					break;
				}
			}
        }

        return unusedButtonRowNoList;
    }

    private static List<String> getAddButtonList() {
        List<String> allButtonList = getAllButtonList();
        List<String> commonButtonList = getCommonButtonList();
        List<String> addButtonList = new ArrayList<String>();
        for (String buttonName : allButtonList) {
            boolean b = false;
            for (String string : commonButtonList) {
                if(string.contains(buttonName)) {
                    b = true;
                    break;
                }
            }

            if (!b) {
                addButtonList.add(buttonName);
            }
        }

        return addButtonList;
    }

    private static Table<Integer, Integer, String> getPgmTable(String inPgmId) {

        System.out.println("Start read P_全SUB_ACCSESS_DB.xlsx");

        Table<Integer, Integer, String> table = ExcelUtil.getTableBySXSSF(FILE_P_SUB_ACCESS_DB);
        Table<Integer, Integer, String> returnTable = HashBasedTable.create();

        int rowSize = table.rowKeySet().size();
        int startRowNo = 0;
        for (int rowNo = 3; rowNo < rowSize; rowNo++) {
            if (Strings.isNullOrEmpty(table.get(rowNo, 1))) {
                continue;
            }

            String pgmId = table.get(rowNo, 1);

            if (!pgmId.equals(inPgmId)) {
                continue;
            }

            Map<Integer, String> row = table.row(rowNo);
            for (Entry<Integer, String> rowEntry : row.entrySet()) {
                int colNo = rowEntry.getKey();
                String value = rowEntry.getValue().trim();

                if (colNo < 3) {
                    continue;
                }

                if (!Strings.isNullOrEmpty(value)) {
                    String tableName = table.get(1, colNo);
                    String tableNameStr = table.get(2, colNo);

                    returnTable.put(startRowNo, 0, String.valueOf(startRowNo+1));
                    returnTable.put(startRowNo, 1, tableName);
                    returnTable.put(startRowNo, 2, tableNameStr);
                    returnTable.put(startRowNo, 3, value);

                    startRowNo++;
                }
            }

        }

        System.out.println("End read P_全SUB_ACCSESS_DB.xlsx");
        return returnTable;
    }

    private static boolean isUseCancelButton() {
    	List<String> unusedButtonNameList = getUnusedButtonNameList();
        return !unusedButtonNameList.contains("取消");
    }

    private static boolean isUseUpdateButton() {
    	List<String> unusedButtonNameList = getUnusedButtonNameList();
        return !unusedButtonNameList.contains("更新");
    }

    private static boolean isUsePrintButton() {
    	List<String> unusedButtonNameList = getUnusedButtonNameList();
        return !unusedButtonNameList.contains("印刷");
    }

    private static boolean isExistDetailList(Map<String, Collection<String>> itemMap) {
//    	Map<String, Collection<String>> itemMap = getItemMap(ITEM_TYPE_OUTPUT_LIST);
    	Set<String> keySet = itemMap.keySet();
    	for (String key : keySet) {
    		if (key.contains("明細")) {
    			return true;
    		}

		}
        return false;
    }

    private static List<String> getDetailColNameList(Map<String, Collection<String>> itemMap) {
    	List<String> list = new ArrayList<String>();

    	Set<String> keySet = itemMap.keySet();
		for (String key : keySet) {
    		if (key.contains("明細")) {
    			list = Lists.newArrayList(itemMap.get(key));
    			break;
    		}
		}

        return list;
    }

    private static boolean isExistSelectBox(Map<String, Collection<String>> controlMap) {
    	Set<String> keySet = controlMap.keySet();
    	return keySet.contains("SELECTBOX");
    }

    private static boolean isExistCheckbox(Map<String, Collection<String>> controlMap) {
    	Set<String> keySet = controlMap.keySet();
    	return keySet.contains("CHECK");
    }

    private static boolean isExistRadio(Map<String, Collection<String>> controlMap) {
    	Set<String> keySet = controlMap.keySet();
    	return keySet.contains("RADIO");
    }

    private static boolean isDate(Map<String, Collection<String>> charTypeMap, String item) {
    	return checkCharType(charTypeMap, item, "有効日付");
    }

    private static boolean isNumber(Map<String, Collection<String>> charTypeMap, String item) {
    	return checkCharType(charTypeMap, item, "半角数字");
    }

    private static boolean isDecimal(Map<String, Collection<String>> charTypeMap, String item) {
    	return checkCharType(charTypeMap, item, "半角数字(符号少数あり)");
    }

    private static boolean checkCharType(Map<String, Collection<String>> charTypeMap, String item, String type) {
    	Collection<String> collection = charTypeMap.get(type);
    	if (collection == null) {
    		return false;
    	}

    	List<String> list = Lists.newArrayList(collection);
    	for (String tmpItem : list) {
			if (item.contains(tmpItem)) {
				return true;
			}
		}

    	return false;
    }

    private static boolean isName(Map<String, String> itemDomainMap, String item) {
    	for (Entry<String, String> entry : itemDomainMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (item.contains(key) && value.contains("氏名")) {
				return true;
			}
		}

    	return false;
    }

    private static boolean checkControlType(Map<String, String> itemControlMap, String item, String type) {

    	for (Entry<String, String> entry : itemControlMap.entrySet()) {
    		String tmpItem = entry.getKey();
    		String tmpType = entry.getValue();
    		if (item.contains(tmpItem) && type.equals(tmpType)) {
    			return true;
    		}
		}

    	return false;
    }

    private static boolean isFullNumberDigitsExclusionList(String item) {

    	for (String string : FULL_NUMBER_DIGITS_EXCLUSION_LIST) {
			if (item.contains(string)) {
				return true;
			}
		}

    	return false;
    }

//	private static boolean isUseFirstPageButton() {
//    	List<String> unusedButtonNameList = getUnusedButtonNameList();
//        return !unusedButtonNameList.contains("先頭頁");
//    }
//
//	private static boolean isUseLastPageButton() {
//    	List<String> unusedButtonNameList = getUnusedButtonNameList();
//        return !unusedButtonNameList.contains("最終頁");
//    }
//
//	private static boolean isUseNextPageButton() {
//    	List<String> unusedButtonNameList = getUnusedButtonNameList();
//        return !unusedButtonNameList.contains("次頁");
//    }
//
//	private static boolean  isUsePreviousPageButton() {
//    	List<String> unusedButtonNameList = getUnusedButtonNameList();
//        return !unusedButtonNameList.contains("前頁");
//    }
}
