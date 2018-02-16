databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1518809996917-1") {
        createTable(tableName: "acra") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "acraPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "acra_data", type: "longtext") {
                constraints(nullable: "false")
            }
        }
    }
}
