package tool.data.constant;

public enum OptTypeEnum {
    CREATE_SQL("CreateSql"),
    GET_TABLE_DATA("GetTableData"),
    GET_TABLE_DATA_BY_PGM_ID("GetTableDataByPgmId"),
    GET_TARGET_TABLE_CURD("GetTargetTableCURD"),
    UPDATE_TABLE_DATA("UpdateTableData"),
    GET_UPDATED_DATA_BY_LOG("GetUpdatedDataByLog"),
    GET_STATISTICS_INFO_BY_LOG("GetStatisticsInfoByLog"),
    GET_TEST_DATA_BY_LOG("GetTestDataByLog"),
    INIT_TEST_DATA("InitTestData"),
    POWER_SHELL_TEST("PowerShellTest"),
    CREATE_EVIDENCE("CreateEvidence");

    private String optType;

    OptTypeEnum(String optType) {
        this.optType = optType;
    }

    /**
     * 根据类型的名称，返回类型的枚举实例。
     *
     * @param optType 类型名称
     */
    public static OptTypeEnum fromOptType(String optType) {
        for (OptTypeEnum type : OptTypeEnum.values()) {
            if (type.getOptType().equals(optType)) {
                return type;
            }
        }
        return null;
    }

    public String getOptType() {
        return this.optType;
    }
}
