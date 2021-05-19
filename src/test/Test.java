package test;

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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Table;

import common.Common;
import common.ExcelUtil;

public class Test {

	public static void main(String[] args) throws IOException {

		String pgmId = "PBB08111";
		String pgmName = "仕入情報在庫反映処理";


		// "№", "SQL文ファイル名", "テーブル対象", "CURD", "スクリプト", "テスト済"

		Map<String, String> sqlMap2 = getSqlMap(pgmId);
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

			List<String> tableList = new ArrayList<String>();
			for (String str : strArray) {
				// PS, PT, PV
				str = str.replaceAll("\"", "");
				if (str.startsWith("PS") || str.startsWith("PT") || str.startsWith("PV")) {
					tableList.add(str);
				}
			}

			// テーブル対象



			List<String> colValueList = new ArrayList<String>();
			colValueList.add(String.valueOf(no));
			colValueList.add(filename);
			colValueList.add(Joiner.on(", ").join(tableList));
			colValueList.add(curd);
			colValueList.add(sql);
			colValueList.add("");

			rowValueList.add(colValueList);
		}


		for (List<String> rowValue : rowValueList) {
			System.out.println(Joiner.on("\t").join(rowValue));
		}


		Table<Integer, Integer, String> table = ExcelUtil.getTable(String.format("COBOL解析結果(バッチ本体)_%s_%s.xlsx", pgmId, pgmName), "呼出階層");

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

							list.add(sb.toString());
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

							list.add(sb.toString());
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

						list.add(sb.toString());
						sb = new StringBuffer();

					} else {
						if (!(cellValue.toString().contains(keyword1) || cellValue.toString().contains(keyword2) || cellValue.toString().contains(keyword3) || cellValue.toString().contains(keyword4) || cellValue.toString().contains(keyword5))) {
							sb.append(cellValue);
						}
					}
				}

			}

		}

		Map<String, String> sqlMap = getSqlMap(pgmId);

		for (String string : list) {


			String sql = Common.changeSql(string).replaceAll("  ", " ").replaceAll("  ", " ").replaceAll("  ", " ").replace(" )", ")").replace("( ", "(").replace("WHERE(", "WHERE (");
			String fileName = sqlMap.get(sql);

			System.out.println(String.format("%s: %s", fileName, sql));
//			System.out.println(sql);
		}

		System.out.println(list.size());
		System.out.println(sqlMap.size());

	}

	private static Map<String, String> getSqlMap(String pgmId) throws IOException {

		Map<String, String> sqlMap = new LinkedHashMap<String, String>();

		File file = new File("template\\" + pgmId);
		if (!file.isDirectory()) {
			return null;
		}

		File[] listFiles = file.listFiles();
		System.out.println(listFiles.length);
		for (File file2 : listFiles) {
			String fileName = file2.getName();
			if (!fileName.contains(".sql")) {
				System.out.println(123);
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
		    if (sqlMap.containsKey(sql)) {
		    	System.out.println(fileName);
		    }
		    sqlMap.put(fileName,  sql);
		}

		return sqlMap;
	}

}
