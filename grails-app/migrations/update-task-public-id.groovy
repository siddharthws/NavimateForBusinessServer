databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1528374280478-1") {
        addColumn(tableName: "task") {
            column(name: "public_id", type: "varchar(255)")
        }
    }
}
