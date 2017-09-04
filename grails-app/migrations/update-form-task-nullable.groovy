databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1504537909329-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "task_id", tableName: "form")
    }
}
