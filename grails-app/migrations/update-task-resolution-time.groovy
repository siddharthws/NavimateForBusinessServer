databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1529827921372-1") {
        addColumn(tableName: "task") {
            column(name: "resolution_time_hrs", type: "float8", defaultValueNumeric: -1) {
                constraints(nullable: "false")
            }
        }
    }
}
