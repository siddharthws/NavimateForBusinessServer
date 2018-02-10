databaseChangeLog = {
    changeSet(author: "Siddharth (generated)", id: "1518035980638-7") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "task_id", tableName: "form")
    }

    changeSet(author: "Siddharth (generated)", id: "1518035980638-8") {
        dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "task_status", tableName: "form")
    }
}
