package test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Strings;
import com.google.common.collect.Table;

import common.Common;
import common.ExcelUtil;

public class Test3 {

	public static void main(String[] args) throws IOException {

		String pgmId = "PBB08111";
		String pgmName = "仕入情報在庫反映処理";

		Table<Integer, Integer, String> table = ExcelUtil.getTable(String.format("COBOL解析結果(バッチ本体)_%s_%s.xlsx", pgmId, pgmName), "呼出階層");

		String keyword1 = "検索SQL";
		String keyword2 = "登録SQL";
		String keyword3 = "更新SQL";
		String keyword4 = "削除SQL";
		String keyword5 = "カーソル定義";

		int rowSize = table.rowKeySet().size();
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

				if (value.contains(keyword3)) {
					System.out.println(23123);
				}

				if (b || (value.contains(keyword1) || value.contains(keyword2) || value.contains(keyword3) || value.contains(keyword4) || value.contains(keyword5))) {
					cellValue.append(value);

					b = true;

					if (value.contains(keyword1) || value.contains(keyword5)) {
						if (value.contains(keyword1)) {

							isSearch = true;
						} else {

							isCursor = true;
						}
					} else {
						if (value.contains(keyword2) || value.contains(keyword3) || value.contains(keyword4)) {
							if (value.contains(keyword1)) {

								isSearch = false;
							} else {

								isCursor = false;
							}

						}
					}
				}
			}

			if (b) {

				if (isSearch || isCursor) {
					// 検索

					if (isSearch) {
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
						if (cellValue.toString().contains("-------")) {
							if (cursorOne) {
								b = false;
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

//		Map<String, String> sqlMap = getSqlMap(pgmId);

		for (String string : list) {

			System.out.println(Common.changeSql(string).replaceAll("  ", " "));

//			String sql = Common.changeSql(string).replaceAll("  ", " ").replace(" )", ")").replace("( ", "(");
//			String fileName = sqlMap.get(sql);
//
//			System.out.println(String.format("%s: %s", fileName, sql));
		}

		System.out.println(list.size());
//		System.out.println(sqlMap.size());

	}

	private static Map<String, String> getSqlMap(String pgmId) throws IOException {

		Map<String, String> sqlMap = new HashMap<String, String>();

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
		    System.out.println(sb.toString().replaceAll(" ,", ", ").replaceAll("  ", " ").replace(" )", ")").replace("( ", "(").trim());

		    sqlMap.put(sb.toString().replaceAll(" ,", ", ").replaceAll("  ", " ").replace(" )", ")").replace("( ", "(").trim(), fileName);
		}

		return sqlMap;
	}

}
