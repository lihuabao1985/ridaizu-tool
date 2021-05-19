package test;

import java.sql.SQLException;
import java.util.List;

import dao.ExecDao;
import dao.MemoryInfo;

public class TestDao {

    public static void main(String[] args) throws SQLException {
        // TODO 自動生成されたメソッド・スタブ
        ExecDao execDao = new ExecDao();
        List<MemoryInfo> memoryInfoList = execDao.getMemoryInfoList();
        for (int i = 0; i < memoryInfoList.size(); i++) {
            MemoryInfo memoryInfo = memoryInfoList.get(i);
            System.out.println(String.format("%d: %s", i, memoryInfo.getSqlId()));

            List<String> execPlanInfoList = execDao.getExecPlanInfoList(memoryInfo.getSqlId());
            for (String string : execPlanInfoList) {
                System.out.println(string);
            }
        }
    }

}
