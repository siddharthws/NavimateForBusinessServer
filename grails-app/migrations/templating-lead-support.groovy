databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1513336739266-1") {
        addColumn(tableName: "lead") {
            column(name: "template_data_id", type: "int8")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1513336739266-3") {
        addForeignKeyConstraint(baseColumnNames: "template_data_id", baseTableName: "lead", constraintName: "FKefdswhv1rl7y2u7ryfof2uu9q", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "data")
    }
}
