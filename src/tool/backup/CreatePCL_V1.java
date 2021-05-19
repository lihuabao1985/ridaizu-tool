package tool.backup;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.google.common.base.Strings;
import com.google.common.collect.Table;

import common.ExcelUtil;

public class CreatePCL_V1 {

	public static void main(String[] args) throws Exception {

		// No.	論理名	物理名	操作区分
		String pgmId = "PBB20301";
		String pgmName = "ＳＰＤ買掛金計上処理";

		Table<Integer, Integer, String> table = ExcelUtil.getTable(String.format("%s.xlsx", pgmId));

		int rowSize = table.rowKeySet().size();
		if (rowSize == 0) {
			System.exit(0);
		}

		// PCL (入力件数パターン確認)
		// SF List
		List<Integer> sfList = new ArrayList<Integer>();

		// PCL (マスタ確認)
		// マスタ List
		List<Integer> masterList = new ArrayList<Integer>();

		// PCL (更新エラー確認)
		// IUD List
		List<Integer> iudList = new ArrayList<Integer>();

		for (int rowNo = 1; rowNo < rowSize; rowNo++) {
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

			if (tableNameStr.contains("マスタ")) {
				masterList.add(rowNo);
			}

			System.out.println(String.format("%s\t%s\t%s", tableName, tableNameStr, kubun));
		}

		System.out.println("");
		System.out.println("--------------sfList--------------");
		for (int rowNo : sfList) {
			System.out.println(String.format("%s\t%s\t%s", table.get(rowNo, 1), table.get(rowNo, 2), table.get(rowNo, 3)));
		}

		System.out.println("");
		System.out.println("--------------masterList--------------");
		for (int rowNo : masterList) {
			System.out.println(String.format("%s\t%s\t%s", table.get(rowNo, 1), table.get(rowNo, 2), table.get(rowNo, 3)));
		}

		System.out.println("");
		System.out.println("--------------iudList--------------");
		for (int rowNo : iudList) {
			System.out.println(String.format("%s\t%s\t%s", table.get(rowNo, 1), table.get(rowNo, 2), table.get(rowNo, 3)));
		}

		Workbook workbook = ExcelUtil.getWorkbook("機能ID_機能名_標準チェックリスト（バッチ）.xlsm");
		Sheet sheet = workbook.getSheet("集計");

		Workbook templateWorkbook = ExcelUtil.getWorkbook("template.xlsm");

		ExcelUtil.setCellValue(sheet.getRow(11).getCell(21), pgmId);
		ExcelUtil.setCellValue(sheet.getRow(12).getCell(21), pgmName);

		int startRowNo1 = 11;
		int addRowCount = 0;
		int tmpCount = 1;
		int tmpStartColNo = 23;
		String Y = "Y";

		System.out.println("Start create PCL (入力件数パターン確認)");
		for (int rowNo : sfList) {

			// 論理名
			String tableName = table.get(rowNo, 1);
			// 物理名
			String tableNameStr = table.get(rowNo, 2);

			String name = String.format("%s・%s", tableNameStr, tableName);

			ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 1, workbook, "PCL (入力件数パターン確認)", startRowNo1);
			String stringValue = ExcelUtil.getStringValue(workbook.getSheet("PCL (入力件数パターン確認)").getRow(startRowNo1).getCell(5)) + tmpCount;
			ExcelUtil.setCellValue(workbook.getSheet("PCL (入力件数パターン確認)").getRow(startRowNo1).getCell(5), stringValue);
			startRowNo1++;

			ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 2, workbook, "PCL (入力件数パターン確認)", startRowNo1);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (入力件数パターン確認)").getRow(startRowNo1).getCell(6), name);
			startRowNo1++;

			ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 3, workbook, "PCL (入力件数パターン確認)", startRowNo1);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (入力件数パターン確認)").getRow(startRowNo1).getCell(tmpStartColNo++), Y);
			startRowNo1++;

			ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 4, workbook, "PCL (入力件数パターン確認)", startRowNo1);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (入力件数パターン確認)").getRow(startRowNo1).getCell(tmpStartColNo++), Y);
			startRowNo1++;

			ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 5, workbook, "PCL (入力件数パターン確認)", startRowNo1);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (入力件数パターン確認)").getRow(startRowNo1).getCell(tmpStartColNo++), Y);
			startRowNo1++;

			ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 6, workbook, "PCL (入力件数パターン確認)", startRowNo1);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (入力件数パターン確認)").getRow(startRowNo1).getCell(tmpStartColNo++), Y);
			startRowNo1++;

			ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 7, workbook, "PCL (入力件数パターン確認)", startRowNo1++);

			addRowCount += 7;
			tmpCount++;
		}

		int startRowNo = 9;
		int endRowNo = 13;
		int startColNo = 2;
		int endColNo = 3;

		endRowNo += addRowCount;

		workbook.getSheet("PCL (入力件数パターン確認)").addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));

		int startRowNo2 = 21 + addRowCount;
		int addRowCount2 = 0;
		for (int rowNo : iudList) {

			// 論理名
			String tableName = table.get(rowNo, 1);
			// 物理名
			String tableNameStr = table.get(rowNo, 2);

			String name = String.format("%s・%s", tableNameStr, tableName);

			ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 8, workbook, "PCL (入力件数パターン確認)", startRowNo2);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (入力件数パターン確認)").getRow(startRowNo2).getCell(5), name);
			startRowNo2++;
			addRowCount2++;
		}

		ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 9, workbook, "PCL (入力件数パターン確認)", startRowNo2++);addRowCount2++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 10, workbook, "PCL (入力件数パターン確認)", startRowNo2++);addRowCount2++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 11, workbook, "PCL (入力件数パターン確認)", startRowNo2++);addRowCount2++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 12, workbook, "PCL (入力件数パターン確認)", startRowNo2++);addRowCount2++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 13, workbook, "PCL (入力件数パターン確認)", startRowNo2++);addRowCount2++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 14, workbook, "PCL (入力件数パターン確認)", startRowNo2++);addRowCount2++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 15, workbook, "PCL (入力件数パターン確認)", startRowNo2++);addRowCount2++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (入力件数パターン確認)", 16, workbook, "PCL (入力件数パターン確認)", startRowNo2++);addRowCount2++;

		startRowNo = startRowNo1 + 3;
		endRowNo = startRowNo + 24 + addRowCount2;

//		System.out.println(startRowNo);
//		System.out.println(endRowNo);
		workbook.getSheet("PCL (入力件数パターン確認)").addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));

		System.out.println("End create PCL (入力件数パターン確認)");

		int startRowNo3 = 11;
		int addRowCount3 = 0;

		System.out.println("Start create PCL (マスタ確認)");
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

			ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 2, workbook, "PCL (マスタ確認)", startRowNo3++);
			addRowCount3++;

			ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 3, workbook, "PCL (マスタ確認)", startRowNo3++);
			addRowCount3++;

		}

		startRowNo = 9;
		endRowNo = 14;
		startColNo = 2;
		endColNo = 3;

		endRowNo += addRowCount3;
		workbook.getSheet("PCL (マスタ確認)").addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));

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
		ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 9, workbook, "PCL (マスタ確認)", startRowNo4++);addRowCount4++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 10, workbook, "PCL (マスタ確認)", startRowNo4++);addRowCount4++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 11, workbook, "PCL (マスタ確認)", startRowNo4++);addRowCount4++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 12, workbook, "PCL (マスタ確認)", startRowNo4++);addRowCount4++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (マスタ確認)", 13, workbook, "PCL (マスタ確認)", startRowNo4++);addRowCount4++;

		startRowNo = startRowNo3 + 4;
		endRowNo = startRowNo + 22 + addRowCount4;

//		System.out.println(startRowNo);
//		System.out.println(endRowNo);
		workbook.getSheet("PCL (マスタ確認)").addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));

		System.out.println("End create PCL (マスタ確認)");

		int startRowNo5 = 10;
		int addRowCount5 = 0;
		int tmpCount5 = 1;

		System.out.println("Start create PCL (更新エラー確認)");
		for (int rowNo : iudList) {
			// 論理名
			String tableName = table.get(rowNo, 1);
			// 物理名
			String tableNameStr = table.get(rowNo, 2);
			// 操作区分
			String kubun = table.get(rowNo, 3);

			String name = String.format("%s・%s", tableNameStr, tableName);

			ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 1, workbook, "PCL (更新エラー確認)", startRowNo5);
			String stringValue = ExcelUtil.getStringValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(4)) + tmpCount5;
			ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(4), stringValue);
			startRowNo5++;
			tmpCount5++;
			addRowCount5++;

			ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 2, workbook, "PCL (更新エラー確認)", startRowNo5);
			ExcelUtil.setCellValue(workbook.getSheet("PCL (更新エラー確認)").getRow(startRowNo5).getCell(5), name);
			startRowNo5++;
			addRowCount5++;


			if (kubun.contains("I")) {
				ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 3, workbook, "PCL (更新エラー確認)", startRowNo5++);
				addRowCount5++;
			}

			if (kubun.contains("U")) {
				ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 4, workbook, "PCL (更新エラー確認)", startRowNo5++);
				addRowCount5++;
			}

			if (kubun.contains("D")) {
				ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 5, workbook, "PCL (更新エラー確認)", startRowNo5++);
				addRowCount5++;
			}

			ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 6, workbook, "PCL (更新エラー確認)", startRowNo5++);
			addRowCount5++;
		}

		startRowNo = 9;
		endRowNo = 16;
		startColNo = 2;
		endColNo = 3;

		endRowNo += addRowCount5;

//		System.out.println(startRowNo);
//		System.out.println(endRowNo);
		workbook.getSheet("PCL (更新エラー確認)").addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));

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
			startRowNo6++;
			addRowCount6++;
		}

		ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 8, workbook, "PCL (更新エラー確認)", startRowNo6++);addRowCount6++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 9, workbook, "PCL (更新エラー確認)", startRowNo6++);addRowCount6++;
		ExcelUtil.copyRow(templateWorkbook, "PCL (更新エラー確認)", 10, workbook, "PCL (更新エラー確認)", startRowNo6++);addRowCount6++;

		startRowNo = endRowNo + 1;
		endRowNo = startRowNo + 27 + addRowCount6;

//		System.out.println(startRowNo);
//		System.out.println(endRowNo);

		workbook.getSheet("PCL (更新エラー確認)").addMergedRegion(new CellRangeAddress(startRowNo,endRowNo,startColNo,endColNo));

		System.out.println("End create PCL (更新エラー確認)");

		String filePath = String.format("%s_%s_標準チェックリスト（バッチ）.xlsm", pgmId, pgmName);
		ExcelUtil.save(String.format("%s_%s_標準チェックリスト（バッチ）.xlsm", pgmId, pgmName), workbook);

		System.out.println(String.format("「%s」ファイルが保存しました。", filePath));
	}

}
