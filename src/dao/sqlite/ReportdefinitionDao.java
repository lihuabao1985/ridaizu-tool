package dao.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dao.db.SqliteDao;

public class ReportdefinitionDao extends SqliteDao {

    public boolean isExistData(String sql) throws SQLException {
        ResultSet rs = doSelect(sql);
        return rs.next();
    }

    public String getTableComment(String tableName) throws SQLException {

        return "TM_REPORTDEFINITION";
    }

    public List<String> getColumnNameList(String tableName) throws SQLException {

        return Arrays.asList("SYSTEM_ID", "REPORT_ID", "REPORT_SUB", "REPORT_TYPE", "REPORT_NAME", "LIMITED_TYPE",
                "LIMITED_PERIOD", "TEKIYO_KAISHI_YMD", "TEKIYO_SHURYO_YMD", "SAKUSEI_YMD", "SAKUSEI_HMS",
                "SAKUSEI_PROGRAM_ID", "SAKUSEI_TANMATSU_IP_ADDRESS", "SAKUSEISHA_LOGIN_ID", "KOSHIN_YMD", "KOSHIN_HMS",
                "KOSHIN_PROGRAM_ID", "KOSHIN_TANMATSU_IP_ADDRESS", "KOSHINSHA_LOGIN_ID", "RECORD_VERSION",
                "MAINTENANCE_COMMENT");
    }

    public static void main(String...strings) throws SQLException {
        ReportdefinitionDao dao = new ReportdefinitionDao();
        List<List<String>> columnInfoList = dao.getColumnNameList("TM_REPORTDEFINITION");
        for (List<String> list : columnInfoList) {
            System.out.println(String.format("%s\t%s\t%s", list.get(0), list.get(1), list.get(2)));
        }

        System.out.println(dao.getTableComment("TM_REPORTDEFINITION"));
    }

    public List<String> getPrimaryKeyList(String tableName) throws SQLException {

        return Arrays.asList("SYSTEM_ID", "REPORT_ID", "REPORT_SUB");
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
