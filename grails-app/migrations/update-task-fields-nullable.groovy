databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1511010426173-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "lead_id", tableName: "task")
    }

    changeSet(author: "Siddharth (generated)", id: "1511010426173-2") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "manager_id", tableName: "task")
    }

    changeSet(author: "Siddharth (generated)", id: "1511010426173-3") {
        dropDefaultValue(columnDataType: "int", columnName: "period", tableName: "task")
    }

    changeSet(author: "Siddharth (generated)", id: "1511010426173-4") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "rep_id", tableName: "task")
    }
}
