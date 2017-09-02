databaseChangeLog = {

    changeSet(author: "vishesh (generated)", id: "1502781888653-1") {
        createSequence(sequenceName: "hibernate_sequence")
    }

    changeSet(author: "vishesh (generated)", id: "1502781888653-2") {
        createTable(tableName: "account") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "accountPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "last_modified", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "admin_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "vishesh (generated)", id: "1502781888653-3") {
        createTable(tableName: "nvm_user") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "userPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "fcm_id", type: "VARCHAR(255)")

            column(name: "date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "last_modified", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "role", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "password", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "account_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR(255)")

            column(name: "phone_number", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "vishesh (generated)", id: "1502781888653-4") {
        addForeignKeyConstraint(baseColumnNames: "account_id", baseTableName: "nvm_user", constraintName: "FKc3b4xfbq6rbkkrddsdum8t5f0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "account")
    }

    changeSet(author: "vishesh (generated)", id: "1502781888653-5") {
        addForeignKeyConstraint(baseColumnNames: "admin_id", baseTableName: "account", constraintName: "FKjsok2lt6hykiw2lo5ks2hhvm3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "nvm_user")
    }
}
