databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1515919596508-1") {
        addColumn(tableName: "form") {
            column(name: "task_status", type: "varchar(255)", defaultValue: "OPEN") {
                constraints(nullable: "false")
            }
        }
    }
}
