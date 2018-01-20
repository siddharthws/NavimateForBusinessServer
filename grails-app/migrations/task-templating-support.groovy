databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1516380577733-1") {
        addColumn(tableName: "task") {
            column(name: "template_data_id", type: "int8")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1516380577733-2") {
        addForeignKeyConstraint(baseColumnNames: "template_data_id", baseTableName: "task", constraintName: "FKgl9h0ifq13nkkt2d8oiuy94ln", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "data")
    }
}
