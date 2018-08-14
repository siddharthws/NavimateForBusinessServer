databaseChangeLog = {

    changeSet(author: "amit (generated)", id: "1533702989095-2") {
        addColumn(tableName: "account") {
            column(name: "photo_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }
}