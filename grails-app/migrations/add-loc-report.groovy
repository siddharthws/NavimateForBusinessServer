databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1527255850889-1") {
        createTable(tableName: "loc_report") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "loc_reportPK")
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

            column(name: "enc_polyline", type: "longtext") {
                constraints(nullable: "false")
            }

            column(name: "account_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "submit_date", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "distance", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1527255850889-2") {
        createTable(tableName: "loc_submission") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "loc_submissionPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "speed", type: "FLOAT4") {
                constraints(nullable: "false")
            }

            column(name: "latlng_string", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "account_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "report_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "battery", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "roads_idx", type: "INT")

            column(name: "submit_date", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1527255850889-3") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "loc_report", constraintName: "FKa0w1ha4l4haajyyvbp2c19gaa", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "nvm_user")
    }

    changeSet(author: "Siddharth (generated)", id: "1527255850889-5") {
        addForeignKeyConstraint(baseColumnNames: "account_id", baseTableName: "loc_submission", constraintName: "FKerr5hr9e0hlucclb7f7np72tg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "account")
    }

    changeSet(author: "Siddharth (generated)", id: "1527255850889-6") {
        addForeignKeyConstraint(baseColumnNames: "account_id", baseTableName: "loc_report", constraintName: "FKfqtls36hmgmdy1sfxyq60a137", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "account")
    }

    changeSet(author: "Siddharth (generated)", id: "1527255850889-7") {
        addForeignKeyConstraint(baseColumnNames: "report_id", baseTableName: "loc_submission", constraintName: "FKkxgnd6u1ovmy7llyhra8qmq7y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "loc_report")
    }
}
