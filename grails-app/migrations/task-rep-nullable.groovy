databaseChangeLog = {
    changeSet(author: "Siddharth (generated)", id: "1516871026005-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "rep_id", tableName: "task")
    }
}
