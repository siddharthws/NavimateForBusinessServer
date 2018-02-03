databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1517657029075-1") {
        createTable(tableName: "location_report") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "location_reportPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "longitude", type: "FLOAT8") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "timestamp", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "account_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "latitude", type: "FLOAT8") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1517657029075-2") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "location_report", constraintName: "FKdfy310hlwxfhwqoh4dwxkxvey", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "nvm_user")
    }

    changeSet(author: "Siddharth (generated)", id: "1517657029075-3") {
        addForeignKeyConstraint(baseColumnNames: "account_id", baseTableName: "location_report", constraintName: "FKhc7pqlspb63udn7nw93amiuqc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "account")
    }
}
