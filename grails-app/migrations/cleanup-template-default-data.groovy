databaseChangeLog = {
    changeSet(author: "Siddharth (generated)", id: "1520696817994-1") {
        dropForeignKeyConstraint(baseTableName: "template", constraintName: "FKnhvq65ai099wckko7ktxp9a95")
    }

    changeSet(author: "Siddharth (generated)", id: "1520696817994-2") {
        dropColumn(columnName: "default_data_id", tableName: "template")
    }

    changeSet(author: "Siddharth (generated)", id: "1520696817994-10") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "value", tableName: "field")
    }
}
