package test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Table;

import common.ExcelUtil;

public class Test2 {

	public static void main(String[] args) {

		Table<Integer, Integer, String> table = ExcelUtil.getTable("COBOL解析結果(バッチ本体)_PBB20301_ＳＰＤ買掛金計上処理.xlsx", "呼出階層");

		String keyword1 = "検索SQL";
		String keyword2 = "登録SQL";
		String keyword3 = "更新SQL";
		String keyword4 = "削除SQL";
		String keyword5 = "カーソル定義";

		int rowSize = table.rowKeySet().size();
		int count = 1;
		boolean b = false;
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

				if (b || (value.contains(keyword1) || value.contains(keyword2) || value.contains(keyword3) || value.contains(keyword4) || value.contains(keyword5))) {
					cellValue.append(value);

					b = true;

					if (value.contains(keyword1)) {
						isSearch = true;
					} else {
						if (value.contains(keyword2) || value.contains(keyword3) || value.contains(keyword4) || value.contains(keyword5)) {
							isSearch = false;
						}
					}
				}
			}

			if (b) {

				if (isSearch) {
					// 検索

					if (cellValue.toString().contains("-------")) {
						if (searchOne) {
							b = false;
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

				} else {
					// 登録・更新
					if (cellValue.toString().contains("-------")) {
						b = false;

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

		for (String string : list) {
			System.out.println(string);
		}

		System.out.println(list.size());
	}

}
