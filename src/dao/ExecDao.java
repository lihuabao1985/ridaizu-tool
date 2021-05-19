package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import config.Config;
import dao.db.Dao;

public class ExecDao extends Dao {

    public boolean isExistData(String sql) throws SQLException {
        ResultSet rs = doSelect(sql);
        return rs.next();
    }

    public String getTableComment(String tableName) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT TABLE_NAME, COMMENTS\n");
        sb.append("FROM USER_TAB_COMMENTS\n");
        sb.append("WHERE \n");
        sb.append("TABLE_NAME = '%s'\n");

        ResultSet rs = doSelect(String.format(sb.toString(), tableName));

        String comments = null;

        if (rs.next()) {
            comments = rs.getString("COMMENTS");
        }

        return comments;
    }

    public List<String> getColumnNameList(String tableName) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT \n");
        sb.append("  column_name \n");
        sb.append("FROM \n");
        sb.append("  all_tab_columns \n");
        sb.append("WHERE \n");
        sb.append("  table_name = '%s' \n");
        sb.append("  AND OWNER = '%s' \n");
        sb.append("ORDER BY \n");
        sb.append("  column_id \n");

        ResultSet rs = doSelect(String.format(sb.toString(), tableName, Config.getString("DB_USERNAME")));

        List<String> list = new ArrayList<String>();

        while (rs.next()) {
            list.add(rs.getString("column_name"));
        }

        return list;
    }

    public List<List<String>> getColumnInfoList(String tableName) throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT \n");
        sb.append("column_name \n");
        sb.append(", data_type \n");
        sb.append(", data_length \n");
        sb.append(", data_precision \n");
        sb.append(", data_scale \n");
        sb.append("FROM \n");
        sb.append("  all_tab_columns \n");
        sb.append("WHERE \n");
        sb.append("  table_name = '%s' \n");
        sb.append("  AND OWNER = '%s' \n");
        sb.append("ORDER BY \n");
        sb.append("  column_id \n");

        ResultSet rs = doSelect(String.format(sb.toString(), tableName, Config.getString("DB_USERNAME")));

        List<List<String>> columnInfoList = new ArrayList<List<String>>();

        while (rs.next()) {
            List<String> list = new ArrayList<String>();
            list.add(rs.getString("column_name"));
            list.add(rs.getString("data_type"));
            if ("NUMBER".equals(rs.getString("data_type"))) {
                list.add(String.format("%s,%s", rs.getString("data_precision"), rs.getString("data_scale")));
            }
            else {
                list.add(rs.getString("data_length"));
            }
            columnInfoList.add(list);
        }

        return columnInfoList;
    }

    public static void main(String... strings) throws SQLException {
        ExecDao dao = new ExecDao();
        List<List<String>> columnInfoList = dao.getColumnInfoList("PSBT9C20");
        for (List<String> list : columnInfoList) {
            System.out.println(String.format("%s\t%s\t%s", list.get(0), list.get(1), list.get(2)));
        }

        System.out.println(dao.getTableComment("PSBT9C20"));
    }

    public List<String> getPrimaryKeyList(String tableName) throws SQLException {
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT \n");
        sb.append("	COLUMN_NAME \n");
        sb.append("FROM \n");
        sb.append("	USER_CONS_COLUMNS \n");
        sb.append("WHERE \n");
        sb.append("	TABLE_NAME = '%s' \n");
        sb.append("	AND CONSTRAINT_NAME IN ( \n");
        sb.append("		SELECT \n");
        sb.append("			CONSTRAINT_NAME \n");
        sb.append("		FROM \n");
        sb.append("			USER_CONSTRAINTS \n");
        sb.append("		WHERE \n");
        sb.append("			TABLE_NAME = '%s' \n");
        sb.append("  	AND OWNER = '%s' \n");
        sb.append("		AND \n");
        sb.append("			CONSTRAINT_TYPE = 'P' \n");
        sb.append("	) \n");

        ResultSet rs = doSelect(String.format(sb.toString(), tableName, tableName, Config.getString("DB_USERNAME")));

        List<String> list = new ArrayList<String>();

        while (rs.next()) {
            list.add(rs.getString("COLUMN_NAME"));
        }

        return list;
    }

    public List<List<String>> getDataList(String sql, List<String> columnNameList) throws SQLException {
        ResultSet rs = doSelect(sql);

        List<List<String>> list = new ArrayList<List<String>>();

        while (rs.next()) {
            List<String> dataList = new ArrayList<String>();

            for (String columnName : columnNameList) {
                dataList.add(rs.getString(columnName));
            }

            list.add(dataList);
        }

        return list;
    }

    public List<MemoryInfo> getMemoryInfoList() throws SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("select \n");
        sb.append(" a.sql_id \n");
        sb.append(",a.plan_hash_value \n");
        sb.append(",a.parsing_schema_name \n");
        sb.append(",max(a.SQL_TEXT) sql_text \n");
        sb.append(",max(b.SQL_TEXT) sql_full_text \n");
        sb.append(",max(a.MODULE) module \n");
        sb.append(",sum(a.EXECUTIONS) EXECUTIONS \n");
        sb.append(",sum(a.DISK_READS) DISK_READS \n");
        sb.append(",sum(a.buffer_gets) buffer_gets \n");
        sb.append(",sum(a.elapsed_time) elapsed_time \n");
        sb.append(",max(a.LAST_ACTIVE_TIME) LAST_ACTIVE_TIME \n");
        sb.append(",max(a.BIND_DATA) BIND_DATA \n");
        sb.append("from v$sql a,v$sqltext b \n");
        sb.append("where a.MODULE like 'P%' \n");
        sb.append("and a.PARSING_SCHEMA_NAME = 'PT3704' \n");
        sb.append("and a.LAST_ACTIVE_TIME BETWEEN to_timestamp('2021/02/24 11:20:00') AND to_timestamp('2021/02/24 23:00:00') \n");
        sb.append("and a.SQL_ID = b.sql_id \n");
        sb.append("group by a.sql_id, a.plan_hash_value, a.parsing_schema_name \n");

        ResultSet rs = doSelect(sb.toString());

        List<MemoryInfo> list = new ArrayList<MemoryInfo>();

        while (rs.next()) {
            MemoryInfo memoryInfo = new MemoryInfo();
            memoryInfo.setSqlId(rs.getString("sql_id"));
            memoryInfo.setPlanHashValue(rs.getString("plan_hash_value"));
            memoryInfo.setParsingSchemaName(rs.getString("parsing_schema_name"));
            memoryInfo.setSqlText(rs.getString("sql_text"));
            memoryInfo.setSqlFullText(rs.getString("sql_full_text"));
            memoryInfo.setModule(rs.getString("module"));
            memoryInfo.setExecutions(rs.getString("EXECUTIONS"));
            memoryInfo.setDiskReads(rs.getString("DISK_READS"));
            memoryInfo.setBufferGets(rs.getString("buffer_gets"));
            memoryInfo.setElapsedTime(rs.getString("elapsed_time"));
            memoryInfo.setLastActiveTime(rs.getString("LAST_ACTIVE_TIME"));
            memoryInfo.setBindData(rs.getString("BIND_DATA"));

            list.add(memoryInfo);
        }

        return list;
    }




    public List<String> getExecPlanInfoList(String sqlId) throws SQLException {
        String sql = "SELECT * FROM TABLE (dbms_xplan.Display_cursor);";
        ResultSet rs = doSelect(String.format(sql, sqlId));

        List<String> list = new ArrayList<String>();
        while (rs.next()) {
            list.add(rs.getString(0));
        }

        return list;
    }

    public boolean insertData(String sql) {

        return doUpdate(sql) > 0;
    }

    public boolean updateData(String sql) {

        return doUpdate(sql) > 0;
    }

    public int deleteData(String sql) {

        return doUpdate(sql);
    }

}
