databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1508139937841-1") {
        addColumn(tableName: "task") {
            column(name: "period", type: "int4", defaultValueNumeric: 0) {
                constraints(nullable: "false")
            }
        }
    }
}
