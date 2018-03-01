databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1519882831395-1") {
        addColumn(tableName: "location_report") {
            column(name: "date_submitted", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1519882831395-2") {
        dropColumn(columnName: "timestamp", tableName: "location_report")
    }
}
