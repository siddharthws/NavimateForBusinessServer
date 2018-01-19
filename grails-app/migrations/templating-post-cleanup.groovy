databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1516092135855-1") {
        dropColumn(columnName: "description", tableName: "lead")
    }

    changeSet(author: "Siddharth (generated)", id: "1516092135855-2") {
        dropColumn(columnName: "email", tableName: "lead")
    }

    changeSet(author: "Siddharth (generated)", id: "1516092135855-3") {
        dropColumn(columnName: "phone", tableName: "lead")
    }

    changeSet(author: "Siddharth (generated)", id: "1516092135855-9") {
        addNotNullConstraint(columnDataType: "float8", columnName: "latitude", tableName: "lead")
    }

    changeSet(author: "Siddharth (generated)", id: "1516092135855-11") {
        addNotNullConstraint(columnDataType: "float8", columnName: "longitude", tableName: "lead")
    }

    changeSet(author: "Siddharth (generated)", id: "1516092135855-12") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "manager_id", tableName: "lead")
    }

    changeSet(author: "Siddharth (generated)", id: "1516092135855-14") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "template_data_id", tableName: "lead")
    }

    changeSet(author: "Siddharth (generated)", id: "1516092135855-15") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "title", tableName: "lead")
    }

    changeSet(author: "Siddharth (generated)", id: "1516092135855-16") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "template_data_id", tableName: "task")
    }

    changeSet(author: "Siddharth (generated)", id: "1516092135855-17") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "lead_id", tableName: "task")
    }

    changeSet(author: "Siddharth (generated)", id: "1516092135855-18") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "rep_id", tableName: "task")
    }

    changeSet(author: "Siddharth (generated)", id: "1516092135855-19") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "manager_id", tableName: "task")
    }
}
