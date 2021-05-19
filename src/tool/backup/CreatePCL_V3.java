package tool.backup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import common.Common;
import common.DateUtil;
import common.Def;
import common.ExcelUtil;

public class CreatePCL_V3 {



	public static final String Y = "Y";
	public static final String N = "N";
	public static final String E = "E";
	public static final String L = "L";
	public static final String MARU = "○";

	public static void main(String[] args) throws Exception {
		String pgmId = null;
		String pgmName = null;
		String author = "作成者";
		String createDate = DateUtil.dateToString(DateUtil.getCurrentDateTime(), DateUtil.SHORT_DATE);

//		if (args.length < 3) {
//
//			System.out.println("PGM_ID、PGM_NAME、作成者または作成日を指定してください。作成日を指定しない場合、デフォルトはシステム日付となる");
//			System.exit(0);
//		} else if (args.length == 3) {
//
//			pgmId = args[0];
//			pgmName = args[1];
//			author = args[2];
//			createDate = DateUtil.dateToString(DateUtil.getCurrentDateTime(), DateUtil.SHORT_DATE);
//		} else if (args.length == 4) {
//
//			pgmId = args[0];
//			pgmName = args[1];
//			author = args[2];
//			createDate = args[3];
//		} else {
//
//			System.out.println("パラメータのサイズは３または４つで指定してください。");
//			System.exit(0);
//		}

		List<List<String>> pgmList = new ArrayList<List<String>>();

		String filepath = "template\\COBOL解析結果";
		File file = new File(filepath);
		File[] fileArray = file.listFiles();
		for (File tmpFile : fileArray) {
			List<String> pgmInfoList = new ArrayList<String>();
			String filename = tmpFile.getName();
			filename = filename.replaceAll(".xlsx", "");
			String[] split = filename.split("_");

			pgmInfoList.add(split[1]);
			pgmInfoList.add(split[2]);
			pgmInfoList.add(author);
			pgmInfoList.add(createDate);
			pgmInfoList.add(filename + ".xlsx");

			pgmList.add(pgmInfoList);
		}

		Workbook templateWorkbook = ExcelUtil.getWorkbook("template\\template.xlsm");

//		Table<Integer, Integer, String> table = ExcelUtil.getTable(String.format("%s.xlsx", pgmId));
		Table<Integer, Integer, String> accessDbtable = ExcelUtil.getTable("template\\P_全SUB_ACCSESS_DB.xlsx");


		for (int i = 0; i < pgmList.size(); i++) {
			List<String> pgmInfoList = pgmList.get(i);
			Table<Integer, Integer, String> table = getPgmTable(accessDbtable, pgmInfoList.get(0));

			if (table.rowKeySet().size() == 0) {
				continue;
			}

			System.out.println(String.format("%s->%s\t%s", i, pgmInfoList.get(0), pgmInfoList.get(1)));
			exec(pgmInfoList.get(0), pgmInfoList.get(1), author, createDate, templateWorkbook, table, pgmInfoList.get(4));
		}
	}

	public static void exec(String pgmId, String pgmName, String author, String createDate, Workbook templateWorkbook, Table<Integer, Integer, String> table, String filepath) throws IOException {

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

		System.out.println("");
		System.out.println("Start create 標準チェックリスト（バッチ）。");
		System.out.println("--------------関連テーブル一覧--------------");
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

			boolean isS = kubun.contains("S");
			boolean isF = kubun.contains("F");
			boolean isI = kubun.contains("I");
			boolean isU = kubun.contains("U");
			boolean isD = kubun.contains("D");

			if ((isS || isF) && !tableNameStr.contains("マスタ")) {
				sfList.add(rowNo);
			}

			if (isI || isU || isD) {
				iudList.add(rowNo);
			}

			if (tableNameStr.contains("マスタ") && !"PV".equals(tableName.substring(0, 2))) {
				masterList.add(rowNo);
			}

			System.out.println(String.format("%s\t%s\t%s", tableName, tableNameStr, kubun));
		}

		// データがない場合、処理終了
		if (sfList.isEmpty() && masterList.isEmpty() && iudList.isEmpty()) {
			return ;
		}

		System.out.println("");
		System.out.println("--------------SFテーブル一覧--------------");
		for (int rowNo : sfList) {
			System.out.println(String.format("%s\t%s\t%s", table.get(rowNo, 1), table.get(rowNo, 2), table.get(rowNo, 3)));
		}

		System.out.println("");
		System.out.println("--------------マスタテーブル一覧--------------");
		for (int rowNo : masterList) {
			System.out.println(String.format("%s\t%s\t%s", table.get(rowNo, 1), table.get(rowNo, 2), table.get(rowNo, 3)));
		}

		System.out.println("");
		System.out.println("--------------IUDテーブル一覧--------------");
		for (int rowNo : iudList) {
			System.out.println(String.format("%s\t%s\t%s", table.get(rowNo, 1), table.get(rowNo, 2), table.get(rowNo, 3)));
		}

		System.out.println("");

		// プログラム変更票を作成
		createChangePgmP(pgmId, pgmName, author, createDate);

		Workbook workbook = ExcelUtil.getWorkbook("template\\機能ID_機能名_標準チェックリスト（バッチ）.xlsm");
		Sheet sheet = workbook.getSheet("集計");

		// シート「集計」情報を設定--------------------------------------------------------------------------------------------------
		ExcelUtil.setCellValue(sheet.getRow(11).getCell(21), pgmId);
		ExcelUtil.setCellValue(sheet.getRow(12).getCell(21), pgmName);
		ExcelUtil.setCellValue(sheet.getRow(3).getCell(38), author);
		ExcelUtil.setCellValue(sheet.getRow(3).getCell(41), createDate);


		int addPageCount = 0;

		// シート「PCL (入力件数パターン確認)」情報を設定----------------------------------------------------------------------------

		if (sfList.isEmpty()) {
			workbook.removeSheetAt(3);
			addPageCount--;
		} else {
			setSFInfo(workbook, templateWorkbook, table, sfList, iudList);
		}

		// シート「PCL (マスタ確認)」情報を設定----------------------------------------------------------------------------

		if (masterList.isEmpty()) {
			workbook.removeSheetAt(workbook.getSheetIndex("PCL (マスタ確認)"));
			addPageCount--;
		} else {
			setMasterInfo(workbook, templateWorkbook, table, masterList, iudList);
		}

		// シート「PCL (更新エラー確認)」情報を設定----------------------------------------------------------------------------

		if (iudList.isEmpty()) {
			workbook.removeSheetAt(workbook.getSheetIndex("PCL (更新エラー確認)"));
			addPageCount--;
		} else {
			setIudInfo(workbook, templateWorkbook, table, iudList);
		}

		// SQL文一覧作成
		setSqlListInfo(workbook, filepath, pgmId);

		// エクセル起動する時、公式を実行するように
		int numberOfSheets = workbook.getNumberOfSheets();
		for (int i = 0; i < numberOfSheets; i++) {
			workbook.getSheetAt(i).setForceFormulaRecalculation(true);
		}

//		workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();

		String filePath = String.format("output\\%s_%s_標準チェックリスト（バッチ）.xlsm", pgmId, pgmName);
		ExcelUtil.save(filePath, workbook);
		workbook.close();

		System.out.println(String.format("「%s」ファイルが保存されました。", filePath));
		System.out.println("End create 標準チェックリスト（バッチ）。");
	}

	private static void setSFInfo(Workbook workbook, Workbook templateWorkbook, Table<Integer, Integer, String> table, List<Integer> sfList, List<Integer> iudList) {

		// シート「PCL (入力件数パターン確認)」情報を設定----------------------------------------------------------------------------

		// 【入力件数パターン確認】　情報設定
		System.out.println("Start update sheet「PCL (入力件数パターン確認)」 ");

		int tmpStartColNo = 23;
		int startRowNo = 0;
		int endRowNo = 0;
		int startColNo = 0;
		int endColNo = 0;

		int sheetCount = sfList.size() / 7;
		if (sfList.size() % 7 != 0) {
			sheetCount++;
		}

		int startSheetNo = 3;
		if (sheetCount > 1) {

			for (int i = 1; i < sheetCount; i++) {
				workbook.cloneSheet(startSheetNo);
				workbook.setSheetOrder(workbook.getSheetAt(workbook.getNumberOfSheets() - 1).getSheetName(), startSheetNo + i);

				Sheet tmpSheet = workbook.getSheetAt(startSheetNo + i);
				Cell cell = ExcelUtil.getCell(tmpSheet, 10, 4);
				String value = ExcelUtil.getStringValue(cell);
				value = value.replaceAll("】", i + 1 + "】");
				ExcelUtil.setCellValue(cell, value);
			}


			Sheet sheetDetail = workbook.getSheet("詳細");
			ExcelUtil.setCellValue(sheetDetail.getRow(11).getCell(14), String.valueOf(3 + sheetCount - 1));

			Sheet tmpSheet = workbook.getSheetAt(startSheetNo);
			Cell cell = ExcelUtil.getCell(tmpSheet, 10, 4);
			String value = ExcelUtil.getStringValue(cell);
			value = value.replaceAll("】", "1】");
			ExcelUtil.setCellValue(cell, value);


		}

		String srcSheet1Name = "PCL (入力件数パターン確認)";

		int tmpCount = 1;

		for (int i = 0; i < sheetCount; i++) {

			List<Integer> value0List = new ArrayList<Integer>();
			Multimap<String, Integer> tableNameMultimap1 = ArrayListMultimap.create();

			int startRowNo1 = 11;
			int addRowCount = 0;
			int Y1Count = 0;
			int rowCount = 1;

			String sheetName = workbook.getSheetAt(startSheetNo + i).getSheetName();

			for (int j = 0; j < sfList.size(); ) {
				int rowNo = sfList.get(j);

				if (rowCount > 7) {
					rowCount = 1;
					break;
				}

				// 論理名
				String tableName = table.get(rowNo, 1);
				// 物理名
				String tableNameStr = table.get(rowNo, 2);

				String name = String.format("%s・%s", tableNameStr, tableName);

				ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 1, workbook, sheetName, startRowNo1);
				String stringValue = ExcelUtil.getStringValue(workbook.getSheet(sheetName).getRow(startRowNo1).getCell(5)) + Def.zenkakuNumberMap.get(tmpCount);
				ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(startRowNo1).getCell(5), stringValue);
				startRowNo1++;

				ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 2, workbook, sheetName, startRowNo1);
				ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(startRowNo1).getCell(6), name);
				startRowNo1++;

				ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 3, workbook, sheetName, startRowNo1);
				ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(startRowNo1).getCell(tmpStartColNo), Y);
				tableNameMultimap1.put(tableName, tmpStartColNo);
				value0List.add(tmpStartColNo);
				startRowNo1++;
				Y1Count++;
				tmpStartColNo++;

				ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 4, workbook, sheetName, startRowNo1);
				ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(startRowNo1).getCell(tmpStartColNo), Y);
				tableNameMultimap1.put(tableName, tmpStartColNo);
				startRowNo1++;
				Y1Count++;
				tmpStartColNo++;

				ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 5, workbook, sheetName, startRowNo1);
				ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(startRowNo1).getCell(tmpStartColNo), Y);
				tableNameMultimap1.put(tableName, tmpStartColNo);
				startRowNo1++;
				Y1Count++;
				tmpStartColNo++;

				ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 6, workbook, sheetName, startRowNo1);
				ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(startRowNo1).getCell(tmpStartColNo), Y);
				tableNameMultimap1.put(tableName, tmpStartColNo);
				startRowNo1++;
				Y1Count++;
				tmpStartColNo++;

				ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 7, workbook, sheetName, startRowNo1++);

				addRowCount += 7;
				tmpCount++;

				rowCount++;
				sfList.remove(j);
			}

			startRowNo = 9;
			endRowNo = 13;
			startColNo = 2;
			endColNo = 3;

			endRowNo += addRowCount;

			workbook.getSheet(sheetName).addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));


			// バッチのリターンコードが現・新で同じであること
			// 正常 = 0 情報設定
			for (int j = 0; j < Y1Count; j++) {
				tmpStartColNo = 23;
				int tmpRowNo = endRowNo + 3;
				ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
			}

			// 出力ＤＢ確認　情報設定
			int startRowNo2 = 21 + addRowCount;
			int addRowCount2 = 0;
			for (int rowNo : iudList) {

				// 論理名
				String tableName = table.get(rowNo, 1);
				// 物理名
				String tableNameStr = table.get(rowNo, 2);

				String name = String.format("%s・%s", tableNameStr, tableName);

				ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 8, workbook, sheetName, startRowNo2);
				ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(startRowNo2).getCell(5), name);

				List<Integer> colIndexList = Lists.newArrayList(tableNameMultimap1.get(tableName));
				for (Integer colIndex : colIndexList) {
					ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(startRowNo2).getCell(colIndex), MARU);
				}

				startRowNo2++;
				addRowCount2++;
			}

			ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 9, workbook, sheetName, startRowNo2++);addRowCount2++;
			ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 10, workbook, sheetName, startRowNo2++);addRowCount2++;
			ExcelUtil.copyRow(templateWorkbook, srcSheet1Name, 11, workbook, sheetName, startRowNo2++);addRowCount2++;

			startRowNo = startRowNo1 + 3;
			endRowNo = startRowNo + 24 + addRowCount2;

			workbook.getSheet(sheetName).addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));

			// 出力ＤＢ確認
			for (int j = 0; j < Y1Count; j++) {
				tmpStartColNo = 23;

				if (value0List.contains(tmpStartColNo + j)) {
					int tmpRowNo = endRowNo - 20;
					ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
				} else {
					int tmpRowNo = endRowNo - 19;
					ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
					tmpRowNo = endRowNo - 18;
					ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
				}
			}

			// ＳＹＳＯＵＴログ 情報設定
			//   開始・終了メッセージ出力
			for (int j = 0; j < Y1Count; j++) {
				tmpStartColNo = 23;
				int tmpRowNo = endRowNo - 12;
				ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
			}

			// ＳＹＳＯＵＴログ 情報設定
			//   入出力件数出力
			for (int j = 0; j < Y1Count; j++) {
				tmpStartColNo = 23;
				int tmpRowNo = 0;

				if (value0List.contains(tmpStartColNo + j)) {
					tmpRowNo = endRowNo - 9;
				} else {
					tmpRowNo = endRowNo - 8;
				}

				ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(tmpRowNo).getCell(tmpStartColNo + j), MARU);
			}

			// PCL区分
			for (int j = 0; j < Y1Count; j++) {
				tmpStartColNo = 23;

				int tmpRowNo = endRowNo + 5;
				if (value0List.contains(tmpStartColNo + j)) {
					ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(tmpRowNo).getCell(tmpStartColNo + j), L);
				} else {
					ExcelUtil.setCellValue(workbook.getSheet(sheetName).getRow(tmpRowNo).getCell(tmpStartColNo + j), N);
				}
			}

		}
		System.out.println("End update sheet「PCL (入力件数パターン確認)」");

	}

	private static void setMasterInfo(Workbook workbook, Workbook templateWorkbook, Table<Integer, Integer, String> table, List<Integer> masterList, List<Integer> iudList) {

		// シート「PCL (マスタ確認)」情報を設定----------------------------------------------------------------------------

		int startRowNo3 = 11;
		int addRowCount3 = 0;
		int tmpStartColNo = 23;
		int Y2Count = 0;
		List<Integer> notTableList = new ArrayList<Integer>();

		System.out.println("Start update sheet「PCL (マスタ確認)」");
		for (int rowNo : masterList) {

			// 論理名
			String tableName = table.get(rowNo, 1);
			// 物理名
			String tableNameStr = table.get(rowNo, 2);

			ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 1, workbook, "PCL (マスタ確認)", startRowNo3);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(startRowNo3).getCell(5), tableNameStr);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(startRowNo3).getCell(15), tableName);
			startRowNo3++;
			addRowCount3++;

			ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 2, workbook, "PCL (マスタ確認)", startRowNo3);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(startRowNo3).getCell(tmpStartColNo), Y);
			notTableList.add(tmpStartColNo);
			addRowCount3++;
			startRowNo3++;
			Y2Count++;
			tmpStartColNo++;

			ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 3, workbook, "PCL (マスタ確認)", startRowNo3);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(startRowNo3).getCell(tmpStartColNo++), Y);
			addRowCount3++;
			startRowNo3++;
			Y2Count++;

			ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 4, workbook, "PCL (マスタ確認)", startRowNo3);
			addRowCount3++;
			startRowNo3++;

		}

		int startRowNo = 9;
		int endRowNo = 14;
		int startColNo = 2;
		int endColNo = 3;

		endRowNo += addRowCount3;
		workbook.getSheet("PCL (マスタ確認)").addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));


		// バッチのリターンコードが現・新で同じであること
		// 正常・異常
		for (int i = 0; i < Y2Count; i++) {
			tmpStartColNo = 23;
			int tmpRowNo = 0;

			if (notTableList.contains(tmpStartColNo + i)) {
				tmpRowNo = endRowNo + 4;
			} else {
				tmpRowNo = endRowNo + 3;
			}

			ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
		}

		// 出力ＤＢ更新確認
		int startRowNo4 = 22 + addRowCount3;
		int addRowCount4 = 0;
		for (int rowNo : iudList) {

			// 論理名
			String tableName = table.get(rowNo, 1);
			// 物理名
			String tableNameStr = table.get(rowNo, 2);

			String name = String.format("%s・%s", tableNameStr, tableName);

			ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 5, workbook, "PCL (マスタ確認)", startRowNo4);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(startRowNo4).getCell(5), name);
			startRowNo4++;
			addRowCount4++;
		}

		ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 6, workbook, "PCL (マスタ確認)", startRowNo4++);addRowCount4++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 7, workbook, "PCL (マスタ確認)", startRowNo4++);addRowCount4++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 8, workbook, "PCL (マスタ確認)", startRowNo4++);addRowCount4++;

		startRowNo = startRowNo3 + 4;
		endRowNo = startRowNo + 22 + addRowCount4;

		workbook.getSheet("PCL (マスタ確認)").addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));

		// 出力ＤＢ更新確認
		for (int i = 0; i < Y2Count; i++) {
			tmpStartColNo = 23;
			int tmpRowNo = endRowNo - 18;
			ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
		}


		// ＳＹＳＯＵＴログ 情報設定
		//   開始・終了メッセージ出力
		for (int i = 0; i < Y2Count; i++) {
			tmpStartColNo = 23;
			int tmpRowNo = endRowNo - 10;
			ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
		}

		// ＳＹＳＯＵＴログ 情報設定
		//   入出力件数出力
		for (int i = 0; i < Y2Count; i++) {
			tmpStartColNo = 23;
			int tmpRowNo = endRowNo - 7;
			ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
		}

		// PCL区分
		for (int i = 0; i < Y2Count; i++) {
			tmpStartColNo = 23;
			int tmpRowNo = endRowNo + 5;

			if (notTableList.contains(tmpStartColNo + i)) {
				ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), E);
			} else {
				ExcelUtil.setCellValue(workbook.getSheet("PCL (マスタ確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), N);
			}
		}

		System.out.println("End update sheet「PCL (マスタ確認)」");

	}

	private static void setIudInfo(Workbook workbook, Workbook templateWorkbook, Table<Integer, Integer, String> table, List<Integer> iudList) {

		// シート「PCL (更新エラー確認)」情報を設定----------------------------------------------------------------------------

		int startRowNo5 = 10;
		int addRowCount5 = 0;
		int tmpCount5 = 1;
		int tmpStartColNo = 23;
		int Y3Count = 0;
		Multimap<String, Integer> tableNameMultimap5 = ArrayListMultimap.create();

		System.out.println("Start update sheet「PCL (更新エラー確認)」");
		for (int rowNo : iudList) {
			// 論理名
			String tableName = table.get(rowNo, 1);
			// 物理名
			String tableNameStr = table.get(rowNo, 2);
			// 操作区分
			String kubun = table.get(rowNo, 3);

			String name = String.format("%s・%s", tableNameStr, tableName);

			ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 1, workbook, "PCL (更新エラー確認)", startRowNo5);
			String stringValue = ExcelUtil.getStringValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(4)) + Def.zenkakuNumberMap.get(tmpCount5);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(4), stringValue);
			startRowNo5++;
			tmpCount5++;
			addRowCount5++;

			ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 2, workbook, "PCL (更新エラー確認)", startRowNo5);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(5), name);
			startRowNo5++;
			addRowCount5++;


			if (kubun.contains("I")) {
				ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 3, workbook, "PCL (更新エラー確認)", startRowNo5);
				ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(tmpStartColNo), Y);
				tableNameMultimap5.put(tableName, tmpStartColNo);
				addRowCount5++;
				startRowNo5++;
				Y3Count++;
				tmpStartColNo++;
			}

			if (kubun.contains("U")) {
				ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 4, workbook, "PCL (更新エラー確認)", startRowNo5);
				ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(tmpStartColNo), Y);
				tableNameMultimap5.put(tableName, tmpStartColNo);
				addRowCount5++;
				startRowNo5++;
				Y3Count++;
				tmpStartColNo++;
			}

			if (kubun.contains("D")) {
				ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 5, workbook, "PCL (更新エラー確認)", startRowNo5);
				ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(tmpStartColNo), Y);
				tableNameMultimap5.put(tableName, tmpStartColNo);
				addRowCount5++;
				startRowNo5++;
				Y3Count++;
				tmpStartColNo++;
			}

			ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 6, workbook, "PCL (更新エラー確認)", startRowNo5++);
			addRowCount5++;
		}

		int startRowNo = 9;
		int endRowNo = 16;
		int startColNo = 2;
		int endColNo = 3;
		endRowNo += addRowCount5;

		workbook.getSheet("PCL (更新エラー確認)").addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));


		// バッチのリターンコードが現・新で同じであること
		// 異常
		for (int i = 0; i < Y3Count; i++) {
			tmpStartColNo = 23;
			int tmpRowNo = endRowNo + 4;

			ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
		}

		// 出力ＤＢロールバック確認
		int startRowNo6 = 24 + addRowCount5;
		int addRowCount6 = 0;
		for (int rowNo : iudList) {

			// 論理名
			String tableName = table.get(rowNo, 1);
			// 物理名
			String tableNameStr = table.get(rowNo, 2);

			String name = String.format("%s・%s", tableNameStr, tableName);

			ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 7, workbook, "PCL (更新エラー確認)", startRowNo6);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo6).getCell(5), name);

			List<Integer> colIndexList = Lists.newArrayList(tableNameMultimap5.get(tableName));
			for (Integer colIndex : colIndexList) {
				ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo6).getCell(colIndex), MARU);
			}

			startRowNo6++;
			addRowCount6++;
		}

		ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 8, workbook, "PCL (更新エラー確認)", startRowNo6++);addRowCount6++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 9, workbook, "PCL (更新エラー確認)", startRowNo6++);addRowCount6++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 10, workbook, "PCL (更新エラー確認)", startRowNo6++);addRowCount6++;

		startRowNo = endRowNo + 1;
		endRowNo = startRowNo + 27 + addRowCount6;

		workbook.getSheet("PCL (更新エラー確認)").addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));

		// 出力ＤＢ更新確認
		for (int i = 0; i < Y3Count; i++) {
			tmpStartColNo = 23;
			int tmpRowNo = endRowNo - 23;
			ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
		}

		// ＳＹＳＯＵＴログ 情報設定
		//   開始・終了メッセージ出力
		for (int i = 0; i < Y3Count; i++) {
			tmpStartColNo = 23;
			int tmpRowNo = endRowNo - 15;
			ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
		}

		// ＳＹＳＯＵＴログ 情報設定
		//   入出力件数出力
		for (int i = 0; i < Y3Count; i++) {
			tmpStartColNo = 23;
			int tmpRowNo = endRowNo - 12;
			ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), MARU);
		}

		// PCL区分
		for (int i = 0; i < Y3Count; i++) {
			tmpStartColNo = 23;
			int tmpRowNo = endRowNo + 5;
			ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(tmpRowNo).getCell(tmpStartColNo + i), E);
		}

		System.out.println("End update PCL sheet「(更新エラー確認)」");

	}

	private static void setSqlListInfo(Workbook workbook, String filepath, String pgmId) throws IOException {
		// SQL文一覧作成
		List<String> sqlList = getSqlList(filepath);
		if (sqlList != null && !sqlList.isEmpty()) {
			Sheet tmpSheet = workbook.getSheet("SQL文一覧");
//			Sheet tmpSheet = workbook.createSheet("SQL文一覧");
//			ExcelUtil.createRow(tmpSheet, 0, Arrays.asList("№", "SQL文ファイル名", "テーブル対象", "CURD", "スクリプト", "テスト済"));
			// "№", "SQL文ファイル名", "テーブル対象", "CURD", "スクリプト", "テスト済"

			Map<String, String> sqlMap2 = getSqlMap(pgmId);
			if (sqlMap2 == null || sqlMap2.isEmpty()) {
				// SQL文ファイルがない場合
				List<List<String>> rowValueList = new ArrayList<List<String>>();
				for (int i = 0; i < sqlList.size(); i++) {
					// "№"
					int no = i + 1;
					// SQL文ファイル名
					String filename = "";
					// スクリプト
					String sql = sqlList.get(i);
					String[] strArray = sql.split(" ");

					// CURD
					String curd = strArray[0];

					// テーブル対象
					List<String> tableList = new ArrayList<String>();
					for (String str : strArray) {
						// PS, PT, PV
						str = str.replaceAll("\"", "");
						if (str.startsWith("PS") || str.startsWith("PT") || str.startsWith("PV")) {
							tableList.add(str);
						}
					}

					List<String> colValueList = new ArrayList<String>();
					colValueList.add(String.valueOf(no));
					colValueList.add(filename);
					colValueList.add(Joiner.on(", ").join(tableList));
					colValueList.add(curd);
					colValueList.add(sql);
					colValueList.add("");

					rowValueList.add(colValueList);
				}

				for (int i = 0; i < rowValueList.size(); i++) {
					ExcelUtil.createRow(tmpSheet, i + 1, rowValueList.get(i));
				}

			} else {
				// SQL文ファイルがある場合

				int no = 0;
				List<List<String>> rowValueList = new ArrayList<List<String>>();
				for (Entry<String, String> entry : sqlMap2.entrySet()) {
					// "№"
					no++;
					// SQL文ファイル名
					String filename = entry.getKey();
					// スクリプト
					String sql = entry.getValue();
					String[] strArray = sql.split(" ");

					// CURD
					String curd = strArray[0];

					// テーブル対象
					List<String> tableList = new ArrayList<String>();
					for (String str : strArray) {
						// PS, PT, PV
						str = str.replaceAll("\"", "");
						if (str.startsWith("PS") || str.startsWith("PT") || str.startsWith("PV")) {
							tableList.add(str);
						}
					}

					List<String> colValueList = new ArrayList<String>();
					colValueList.add(String.valueOf(no));
					colValueList.add(filename);
					colValueList.add(Joiner.on(", ").join(tableList));
					colValueList.add(curd);
					colValueList.add(sql);
					colValueList.add("");

					rowValueList.add(colValueList);
				}

				for (int i = 0; i < rowValueList.size(); i++) {
					List<String> list = rowValueList.get(i);

					if (sqlList.contains(list.get(4))) {
						ExcelUtil.createRow(tmpSheet, i + 1, list);
					} else {
						ExcelUtil.createRow(tmpSheet, i + 1, list, workbook, IndexedColors.GREY_50_PERCENT);
					}

				}
			}

		}
	}

	private static Table<Integer, Integer, String> getPgmTable(Table<Integer, Integer, String> table, String inPgmId) {
		System.out.println("Start read P_全SUB_ACCSESS_DB.xlsx");

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

	private static void createChangePgmP(String pgmId, String pgmName, String author, String createDate) throws IOException {
		System.out.println("Start create プログラム変更票。");

		Workbook workbook = ExcelUtil.getWorkbook("template\\機能ID_機能名_プログラム変更票.xls");
		workbook.setSheetName(0, String.format("%s_%s", pgmId, pgmName));
		Sheet sheet = workbook.getSheetAt(0);
		// 作成者を設定
		ExcelUtil.setCellValue(sheet.getRow(2).getCell(36), String.format("CIT%s", author));

		// 作成日を設定
		ExcelUtil.setCellValue(sheet.getRow(2).getCell(47), createDate);

		// プログラムIDを設定
		ExcelUtil.setCellValue(sheet.getRow(7).getCell(6), pgmId);

		// プログラム名を設定
		ExcelUtil.setCellValue(sheet.getRow(9).getCell(6), pgmName);

		// BatchLoaderクラス名を設定
		String batchLoaderClassName = ExcelUtil.getStringValue(ExcelUtil.getCell(sheet, 15, 0));
		ExcelUtil.setCellValue(sheet.getRow(15).getCell(0), batchLoaderClassName.replaceAll("機能ID", pgmId));

		// 業務処理クラス名を設定
		String businessProcessClassName = ExcelUtil.getStringValue(ExcelUtil.getCell(sheet, 16, 2));
		ExcelUtil.setCellValue(sheet.getRow(16).getCell(2), businessProcessClassName.replaceAll("機能ID", pgmId));

		String filePath = String.format("output\\%s_%s_プログラム変更票.xls", pgmId, pgmName);
		ExcelUtil.save(filePath, workbook);
		workbook.close();

		System.out.println(String.format("「%s」ファイルが保存しました。", filePath));
		System.out.println("End create プログラム変更票。");
	}

	private static List<String> getSqlList(String filename) {
//		String filepath = String.format("template\\COBOL解析結果\\COBOL解析結果(バッチ本体)_%s_%s.xlsx", pgmId, pgmName);
		String filepath = String.format("template\\COBOL解析結果\\%s", filename);
		System.out.println("Start read " + filepath);

		Table<Integer, Integer, String> table = ExcelUtil.getTable(filepath, "呼出階層");
		if (table == null || table.rowKeySet().size() == 0) {
			return null;
		}

		String keyword1 = "検索SQL";
		String keyword2 = "登録SQL";
		String keyword3 = "更新SQL";
		String keyword4 = "削除SQL";
		String keyword5 = "カーソル定義";

		int rowSize = table.rowKeySet().size();

		boolean bRead = false;
		boolean isSearch = false;
		boolean searchOne = false;
		boolean isCursor = false;
		boolean cursorOne = false;
		List<String> list = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		for (int rowNo = 0; rowNo < rowSize; rowNo++) {

			StringBuffer cellValue = new StringBuffer();

			Map<Integer, String> row = table.row(rowNo);
			for (Entry<Integer, String> entry : row.entrySet()) {
				if (entry.getKey() < 5) {
					continue;
				}

				String value = entry.getValue();

				if (Strings.isNullOrEmpty(value)) {
					continue;
				}

				cellValue.append(value);
			}

			if (cellValue.toString().contains(keyword1)) {
				// 検索
				bRead = true;
				isSearch = true;
				isCursor = false;

			} else if (cellValue.toString().contains(keyword2)) {
				// 登録
				bRead = true;
				isSearch = false;
				isCursor = false;

			} else if (cellValue.toString().contains(keyword3)) {
				// 更新
				bRead = true;
				isSearch = false;
				isCursor = false;

			} else if (cellValue.toString().contains(keyword4)) {
				// 削除
				bRead = true;
				isSearch = false;
				isCursor = false;

			} else if (cellValue.toString().contains(keyword5)) {
				// カーソル定義
				bRead = true;
				isSearch = false;
				isCursor = true;
			}


			if (bRead) {
				if (isSearch) {

					if (cellValue.toString().contains("-------")) {
						if (searchOne) {
							bRead = false;
							searchOne = false;

							list.add(Common.changeSql(sb.toString()).replaceAll("  ", " ").replaceAll("  ", " ").replaceAll("  ", " ").replace(" )", ")").replace("( ", "(").replace("WHERE(", "WHERE ("));
							sb = new StringBuffer();
						} else {
							searchOne = true;
						}
					} else {
						if (!(cellValue.toString().contains(keyword1) || cellValue.toString().contains(keyword2) || cellValue.toString().contains(keyword3) || cellValue.toString().contains(keyword4) || cellValue.toString().contains(keyword5))) {
							sb.append(cellValue);
						}
					}

				} else if (isCursor) {

					if (cellValue.toString().contains("-------")) {
						if (cursorOne) {
							bRead = false;
							cursorOne = false;

							list.add(Common.changeSql(sb.toString()).replaceAll("  ", " ").replaceAll("  ", " ").replaceAll("  ", " ").replace(" )", ")").replace("( ", "(").replace("WHERE(", "WHERE ("));
							sb = new StringBuffer();
						} else {
							cursorOne = true;
						}
					} else {
						if (!(cellValue.toString().contains(keyword1) || cellValue.toString().contains(keyword2) || cellValue.toString().contains(keyword3) || cellValue.toString().contains(keyword4) || cellValue.toString().contains(keyword5))) {
							sb.append(cellValue);
						}
					}

				} else {
					if (cellValue.toString().contains("-------")) {
						bRead = false;

						list.add(Common.changeSql(sb.toString()).replaceAll("  ", " ").replaceAll("  ", " ").replaceAll("  ", " ").replace(" )", ")").replace("( ", "(").replace("WHERE(", "WHERE ("));
						sb = new StringBuffer();

					} else {
						if (!(cellValue.toString().contains(keyword1) || cellValue.toString().contains(keyword2) || cellValue.toString().contains(keyword3) || cellValue.toString().contains(keyword4) || cellValue.toString().contains(keyword5))) {
							sb.append(cellValue);
						}
					}
				}

			}

		}

		System.out.println("End read " + filepath);
		return list;
	}

	private static Map<String, String> getSqlMap(String pgmId) throws IOException {

		Map<String, String> sqlMap = new LinkedHashMap<String, String>();

		File file = new File("template\\sql\\" + pgmId);
		if (!file.isDirectory()) {
			return null;
		}

		File[] listFiles = file.listFiles();
		for (File file2 : listFiles) {
			String fileName = file2.getName();
			if (!fileName.contains(".sql")) {
				continue;
			}

			Path path = Paths.get(file2.getAbsolutePath());
		    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		    StringBuffer sb = new StringBuffer();
		    for (String line : lines) {

		    	if (Strings.isNullOrEmpty(line.trim())) {
		    		continue;
		    	}

		    	sb.append(line.trim() + " ");
			}

		    String sql = sb.toString().replaceAll(" ,", ", ").replaceAll("  ", " ").replaceAll("  ", " ").replaceAll("  ", " ").replace(" )", ")").replace("( ", "(").replace("WHERE(", "WHERE (").trim();
		    sqlMap.put(fileName,  sql);
		}

		return sqlMap;
	}

}
