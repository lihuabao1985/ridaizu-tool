package test;

public class Diff {

    private String tableName;

    private String columnName;

    private String newValue;

    private String oldValue;

    private int newDiffRowIndex;

    private int oldDiffRowIndex;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public int getNewDiffRowIndex() {
        return newDiffRowIndex;
    }

    public void setNewDiffRowIndex(int newDiffRowIndex) {
        this.newDiffRowIndex = newDiffRowIndex;
    }

    public int getOldDiffRowIndex() {
        return oldDiffRowIndex;
    }

    public void setOldDiffRowIndex(int oldDiffRowIndex) {
        this.oldDiffRowIndex = oldDiffRowIndex;
    }

}
