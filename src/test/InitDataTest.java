package test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Workbook;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import common.Def;
import common.ExcelUtil;
import dao.ExecDao;

public class InitDataTest {

	public static void main(String[] args) throws Exception {
		Workbook workbook = ExcelUtil.getWorkbook("data\\PBB20301_ＳＰＤ買掛金計上処理.xlsx");
		Table<Integer, Integer, String> table = ExcelUtil.getTable(workbook, 0);
		int rowSize = table.rowKeySet().size();

		Multimap<String, List<String>> multimap = ArrayListMultimap.create();
		boolean isTableName = true;
		String tableName = null;
		for (int rowNo = 0; rowNo < rowSize; rowNo++) {
			if (isTableName) {
				tableName = table.get(rowNo, 0);

				if (Strings.isNullOrEmpty(tableName)) {
					break;
				}

				isTableName = false;
				continue;
			}

			if (Strings.isNullOrEmpty(table.get(rowNo, 0))) {
				isTableName = true;
				continue;
			}

			int colSize = table.columnKeySet().size();
			List<String> valueList = new ArrayList<String>();
			for (int colNo = 0; colNo < colSize; colNo++) {
				String colValue = table.get(rowNo, colNo);
				if (!Strings.isNullOrEmpty(colValue)) {
					valueList.add(colValue);
				}
			}

			multimap.put(tableName, valueList);
		}

		ExecDao dao = new ExecDao();

		Map<String, Collection<List<String>>> map = multimap.asMap();
		for (Entry<String, Collection<List<String>>> entry : map.entrySet()) {
			String tmpTableName = getTableName(entry.getKey());
			List<List<String>> valueList = Lists.newArrayList(entry.getValue());

			List<String> primaryKeyList = dao.getPrimaryKeyList(tmpTableName);
            if (primaryKeyList.isEmpty() || valueList.size() < 2) {
                continue;
            }

			List<String> columnList = formatColumnList(valueList.get(0));

            int insertSeccueeCount = 0;
            int insertErrorCount = 0;
            int updateSeccueeCount = 0;
            int updateErrorCount = 0;

			for (int i = 1; i < valueList.size(); i++) {
				List<String> list = formatValueList(valueList.get(i));

				String selectSql = getSelectSql(tableName, primaryKeyList, columnList, list);
                System.out.println(String.format("SELECT SQL: %s", selectSql));

                if (dao.isExistData(selectSql)) {
                    // データが存在する場合、更新処理を行う
                    String updateSql = getUpdateSql(tableName, primaryKeyList, columnList, list);
                    System.out.println(String.format("Update start. SQL: %s", updateSql));
                    boolean updateReslut = dao.updateData(updateSql);
                    if (!updateReslut) {
                        System.out.println(String.format("Update error. SQL: %s", updateSql));
                        updateErrorCount++;
                    } else {
                        updateSeccueeCount++;
                    }
                    System.out.println(String.format("Update end. SQL: %s", updateSql));
                } else {
                    // データが存在しない場合、登録処理を行う
                    String inserSql = getInserSql(tableName, columnList, list);
                    System.out.println(String.format("Insert start. SQL: %s", inserSql));
                    boolean insertReslut = dao.insertData(inserSql);
                    if (!insertReslut) {
                        System.out.println(String.format("Insert error. SQL: %s", inserSql));
                        insertErrorCount++;
                    } else {
                        insertSeccueeCount++;
                    }
                    System.out.println(String.format("Insert end. SQL: %s", inserSql));
                }
			}

            System.out.println(String.format("Table[%s], insert success count[%d], error count[%d], update success count[%d], error count[%d]",
                    tableName, insertSeccueeCount, insertErrorCount, updateSeccueeCount, updateErrorCount));

            System.out.println("----------------------------------------------------------------------------------------------------");
		}

	}

	private static String getTableName(String tableName) {

		return tableName.split("・")[1];
	}

	private static List<String> formatColumnList(List<String> columnList) {
		List<String> tmpColumnList = new ArrayList<String>();
		for (String column : columnList) {
			tmpColumnList.add(String.format("\"%s\"", column));
		}

		return tmpColumnList;
	}

	private static List<String> formatValueList(List<String> valueList) {
		List<String> tmpList = new ArrayList<String>();
		for (String str : valueList) {
			if (str == null) {
				tmpList.add(str);
			} else {
				tmpList.add(String.format("'%s'", str));
			}
		}

		return tmpList;
	}

    private static String getSelectSql(String tableName, List<String> primaryKeyList, List<String> columnList, List<String> valueList) {
        padData(columnList, valueList);

        String keyValueConditionsFormat = "%s=%s";
        List<String> keyValueConditionsList = new ArrayList<String>();
        for (String primaryKey : primaryKeyList) {
            keyValueConditionsList.add(String.format(keyValueConditionsFormat, primaryKey,
                    valueList.get(columnList.indexOf(primaryKey))));
        }

        return String.format(Def.FORMAT_SELECT_SQL, tableName, Joiner.on(" AND ").join(keyValueConditionsList));
    }

    private static String getInserSql(String tableName, List<String> columnList,
            List<String> valueList) {
        padData(columnList, valueList);
        return String.format(Def.FORMAT_INSERT_SQL, tableName, Joiner.on(",").join(columnList), Joiner.on(",").join(valueList));
    }

    private static String getUpdateSql(String tableName, List<String> primaryKeyList, List<String> columnList, List<String> valueList) {

        padData(columnList, valueList);

        String keyValueConditionsFormat = "%s=%s";
        List<String> setKeyValueList1 = new ArrayList<String>();
        for (int i = 0; i < columnList.size(); i++) {
            if (primaryKeyList.contains(columnList.get(i))) {
                continue;
            }

            setKeyValueList1.add(String.format(keyValueConditionsFormat, columnList.get(i), valueList.get(i)));
        }
        List<String> keyValueConditionsList = new ArrayList<String>();
        for (String primaryKey : primaryKeyList) {
            keyValueConditionsList.add(String.format(keyValueConditionsFormat, primaryKey,
                    valueList.get(columnList.indexOf(primaryKey))));
        }

        return String.format(Def.FORMAT_UPDATE_SQL, tableName, Joiner.on(", ").join(setKeyValueList1), Joiner.on(" AND ").join(keyValueConditionsList));
    }

    private static void padData(List<String> columnList, List<String> valueList) {
        int diffLength = columnList.size() - valueList.size();
        for (int i = 0; i < diffLength; i++) {
            valueList.add(Def.NULL_STRING);
        }
    }

}
