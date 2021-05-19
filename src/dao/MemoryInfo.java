package dao;

public class MemoryInfo {

    private String sqlId;

    private String planHashValue;

    private String parsingSchemaName;

    private String sqlText;

    private String sqlFullText;

    private String module;

    private String executions;

    private String diskReads;

    private String bufferGets;

    private String elapsedTime;

    private String lastActiveTime;

    private String bindData;

    public String getSqlId() {
        return sqlId;
    }

    public void setSqlId(String sqlId) {
        this.sqlId = sqlId;
    }

    public String getPlanHashValue() {
        return planHashValue;
    }

    public void setPlanHashValue(String planHashValue) {
        this.planHashValue = planHashValue;
    }

    public String getParsingSchemaName() {
        return parsingSchemaName;
    }

    public void setParsingSchemaName(String parsingSchemaName) {
        this.parsingSchemaName = parsingSchemaName;
    }

    public String getSqlText() {
        return sqlText;
    }

    public void setSqlText(String sqlText) {
        this.sqlText = sqlText;
    }

    public String getSqlFullText() {
        return sqlFullText;
    }

    public void setSqlFullText(String sqlFullText) {
        this.sqlFullText = sqlFullText;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getExecutions() {
        return executions;
    }

    public void setExecutions(String executions) {
        this.executions = executions;
    }

    public String getDiskReads() {
        return diskReads;
    }

    public void setDiskReads(String diskReads) {
        this.diskReads = diskReads;
    }

    public String getBufferGets() {
        return bufferGets;
    }

    public void setBufferGets(String bufferGets) {
        this.bufferGets = bufferGets;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(String lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public String getBindData() {
        return bindData;
    }

    public void setBindData(String bindData) {
        this.bindData = bindData;
    }

}
