databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1503402674123-1") {
        createTable(tableName: "form") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "formPK")
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

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "account_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "data", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "task_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-2") {
        createTable(tableName: "lead") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "leadPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "phone", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "latitude", type: "FLOAT8")

            column(name: "address", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "manager_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "company", type: "VARCHAR(255)")

            column(name: "longitude", type: "FLOAT8")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "account_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-3") {
        createTable(tableName: "nvm_user_task") {
            column(name: "user_created_tasks_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "task_id", type: "BIGINT")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-4") {
        createTable(tableName: "task") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "taskPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "template_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "lead_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "rep_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "manager_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "account_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-5") {
        createTable(tableName: "visit") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "visitPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "rep_id", type: "BIGINT") {
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

            column(name: "task_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-6") {
        addColumn(tableName: "nvm_user") {
            column(name: "manager_id", type: "int8")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-7") {
        addForeignKeyConstraint(baseColumnNames: "rep_id", baseTableName: "visit", constraintName: "FK2djr20l2x8xsi22wtth2r0n0l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "nvm_user")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-8") {
        addForeignKeyConstraint(baseColumnNames: "template_id", baseTableName: "task", constraintName: "FK3d7tyg5bpqr8192821g9tja8s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "form")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-9") {
        addForeignKeyConstraint(baseColumnNames: "user_created_tasks_id", baseTableName: "nvm_user_task", constraintName: "FK4p7el20ecaf3o4hjwbrei83v6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "nvm_user")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-10") {
        addForeignKeyConstraint(baseColumnNames: "account_id", baseTableName: "lead", constraintName: "FK64eaeh91o5wg9qf2fslh6q9jp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "account")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-11") {
        addForeignKeyConstraint(baseColumnNames: "task_id", baseTableName: "form", constraintName: "FKabx5t91rab1tnyix6bbpinfkg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "task")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-12") {
        addForeignKeyConstraint(baseColumnNames: "account_id", baseTableName: "visit", constraintName: "FKb7lgihlmbih9y7xmycok6iy1s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "account")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-13") {
        addForeignKeyConstraint(baseColumnNames: "lead_id", baseTableName: "task", constraintName: "FKe9fdr3iipox899d4flfe0cl5d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "lead")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-14") {
        addForeignKeyConstraint(baseColumnNames: "account_id", baseTableName: "form", constraintName: "FKkpjm5a8gkr51w3ysecac320kx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "account")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-15") {
        addForeignKeyConstraint(baseColumnNames: "account_id", baseTableName: "task", constraintName: "FKl4vy7dauagn3cra1gkdb67r8p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "account")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-16") {
        addForeignKeyConstraint(baseColumnNames: "task_id", baseTableName: "visit", constraintName: "FKm5e19521cgcvfmg0hbnbcvaya", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "task")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-17") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "form", constraintName: "FKmljtvc5y35t35axpbm5hmhpvw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "nvm_user")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-18") {
        addForeignKeyConstraint(baseColumnNames: "manager_id", baseTableName: "task", constraintName: "FKn4ldr2kfy8ryiirhj1mj1hmjb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "nvm_user")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-19") {
        addForeignKeyConstraint(baseColumnNames: "rep_id", baseTableName: "task", constraintName: "FKng63mc2pk9hi7jg66fqcgwvfb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "nvm_user")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-20") {
        addForeignKeyConstraint(baseColumnNames: "task_id", baseTableName: "nvm_user_task", constraintName: "FKq9c2fu6trlgxt2gpn9fhr56yw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "task")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-21") {
        addForeignKeyConstraint(baseColumnNames: "manager_id", baseTableName: "nvm_user", constraintName: "FKqgiwyujqqywi8pbu8qqnykgfm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "nvm_user")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-22") {
        addForeignKeyConstraint(baseColumnNames: "manager_id", baseTableName: "lead", constraintName: "FKqoxl7rlinqahcgsx2ksroifxy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "nvm_user")
    }

    changeSet(author: "Siddharth (generated)", id: "1503402674123-23") {
        dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "password", tableName: "nvm_user")
    }
}
