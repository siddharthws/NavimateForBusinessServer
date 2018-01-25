databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1516891365288-1") {
        addColumn(tableName: "lead") {
            column(name: "visibility", type: "varchar(255)", defaultValue: "PRIVATE") {
                constraints(nullable: "false")
            }
        }
    }
}
