package tool.backup.datav1;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import tool.data.constant.OptTypeEnum;

public class TableDataOptTool {

    public static void main(String[] args) throws IOException, SQLException {

        if (args.length == 0) {
            System.out.println("パラメータを指定してください。");
        }

        String[] prmArgs = Arrays.copyOfRange(args, 1, args.length);
        String optType = args[0];

        OptTypeEnum optTypeEnum = OptTypeEnum.fromOptType(optType);
        switch(optTypeEnum) {
            case CREATE_SQL:
                new CreateSql().exec(prmArgs);
                break;
            case GET_TABLE_DATA:
                new GetTableData().exec(prmArgs);
                break;
            case GET_TABLE_DATA_BY_PGM_ID:
                new GetTableDataByPgmId().exec(prmArgs);
                break;
            case GET_TARGET_TABLE_CURD:
                new GetTargetTableCURD().exec(prmArgs);
                break;
            case UPDATE_TABLE_DATA:
                new UpdateTableData().exec(prmArgs);
                break;
            case GET_UPDATED_DATA_BY_LOG:
                new GetUpdatedDataByLog().exec(prmArgs);
                break;
            case GET_STATISTICS_INFO_BY_LOG:
                new GetStatisticsInfoByLog().exec(prmArgs);
                break;
            case GET_TEST_DATA_BY_LOG:
                new GetTestDataByLog().exec(prmArgs);
                break;
            default :
                System.out.println("正しい機能名を指定してください。");
                break;

        }
    }

}
