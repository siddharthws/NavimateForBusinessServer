databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1523247248454-1") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "creator_id", tableName: "task")
    }
}
