databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1506181545714-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "manager_id", tableName: "lead")
    }
}
