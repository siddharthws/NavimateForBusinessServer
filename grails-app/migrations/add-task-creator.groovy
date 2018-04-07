databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1523114389312-1") {
        addColumn(tableName: "task") {
            column(name: "creator_id", type: "int8")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1523114389312-2") {
        addForeignKeyConstraint(baseColumnNames: "creator_id", baseTableName: "task", constraintName: "FKs2oehdl4fdmkt9drnhja347bb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "nvm_user")
    }
}
