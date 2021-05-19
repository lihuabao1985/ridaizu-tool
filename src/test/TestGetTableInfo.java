package test;

import java.sql.SQLException;
import java.util.List;

import dao.ExecDao;

public class TestGetTableInfo {

    public static void main(String[] args) throws SQLException {

        String tableName = "TM_REPORTDEFINITION";

        ExecDao dao  = new ExecDao();
        List<String> columnNameList = dao.getColumnNameList(tableName);
        for (String columnName : columnNameList) {
            System.out.println(columnName);
        }
    }

}
