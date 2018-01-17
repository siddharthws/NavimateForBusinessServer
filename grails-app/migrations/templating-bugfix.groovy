databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1516180737218-1") {
        addColumn(tableName: "data") {
            column(name: "template_id", type: "int8")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1516180737218-2") {
        addForeignKeyConstraint(baseColumnNames: "template_id", baseTableName: "data", constraintName: "FKo6s7ne6pqwa3p4p6v1rptvv8y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "template")
    }
}
