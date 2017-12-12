databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1513095609963-1") {
        dropForeignKeyConstraint(baseTableName: "task", constraintName: "FK3d7tyg5bpqr8192821g9tja8s")
    }

    changeSet(author: "Siddharth (generated)", id: "1513095609963-2") {
        dropColumn(columnName: "data", tableName: "form")
    }

    changeSet(author: "Siddharth (generated)", id: "1513095609963-3") {
        dropColumn(columnName: "name", tableName: "form")
    }

    changeSet(author: "Siddharth (generated)", id: "1513095609963-4") {
        dropColumn(columnName: "template_id", tableName: "task")
    }

    changeSet(author: "Siddharth (generated)", id: "1513095609963-5") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "form_template_id", tableName: "task")
    }

    changeSet(author: "Siddharth (generated)", id: "1513095609963-8") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "submitted_data_id", tableName: "form")
    }

    changeSet(author: "Siddharth (generated)", id: "1513095609963-9") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "task_id", tableName: "form")
    }

    changeSet(author: "Siddharth (generated)", id: "1513095609963-10") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "owner_id", tableName: "form")
    }
}
