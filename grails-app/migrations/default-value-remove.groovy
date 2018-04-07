databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1523118593837-3") {
        dropDefaultValue(columnDataType: "boolean", columnName: "is_removed", tableName: "field")
    }

    changeSet(author: "Siddharth (generated)", id: "1523118593837-4") {
        dropDefaultValue(columnDataType: "boolean", columnName: "is_removed", tableName: "lead")
    }

    changeSet(author: "Siddharth (generated)", id: "1523118593837-5") {
        dropDefaultValue(columnDataType: "boolean", columnName: "is_removed", tableName: "task")
    }

    changeSet(author: "Siddharth (generated)", id: "1523118593837-6") {
        dropDefaultValue(columnDataType: "boolean", columnName: "is_removed", tableName: "template")
    }

    changeSet(author: "Siddharth (generated)", id: "1523118593837-7") {
        dropDefaultValue(columnDataType: "float8", columnName: "latitude", tableName: "form")
    }

    changeSet(author: "Siddharth (generated)", id: "1523118593837-8") {
        dropDefaultValue(columnDataType: "float8", columnName: "longitude", tableName: "form")
    }

    changeSet(author: "Siddharth (generated)", id: "1523118593837-9") {
        dropDefaultValue(columnDataType: "varchar(255)", columnName: "task_status", tableName: "form")
    }

    changeSet(author: "Siddharth (generated)", id: "1523118593837-10") {
        dropDefaultValue(columnDataType: "varchar(255)", columnName: "visibility", tableName: "lead")
    }
}
