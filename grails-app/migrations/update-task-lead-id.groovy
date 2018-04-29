databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1524911244409-1") {
        addColumn(tableName: "task") {
            column(name: "leadid", type: "varchar(255)")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1524911244409-3") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "lead_id", tableName: "task")
    }
}
