databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1517399601681-1") {
        createTable(tableName: "account_settings") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "account_settingsPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "account_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "start_hr", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "end_hr", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1517399601681-2") {
        addForeignKeyConstraint(baseColumnNames: "account_id", baseTableName: "account_settings", constraintName: "FKnqmsxgh6d3lfl5vy0yymwjunv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "account")
    }
}
