databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1504272985266-1") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "task_id", tableName: "form")
    }
}
