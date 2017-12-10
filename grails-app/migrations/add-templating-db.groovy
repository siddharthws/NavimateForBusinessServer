databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1512908746428-1") {
        createTable(tableName: "data") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "dataPK")
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

            column(name: "account_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-2") {
        createTable(tableName: "field") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "fieldPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "title", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "template_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "b_mandatory", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "account_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-3") {
        createTable(tableName: "template") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "templatePK")
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

            column(name: "default_data_id", type: "BIGINT")

            column(name: "type", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "account_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "owner_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-4") {
        createTable(tableName: "value") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "valuePK")
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

            column(name: "value", type: "longtext") {
                constraints(nullable: "false")
            }

            column(name: "account_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "data_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "field_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-5") {
        addColumn(tableName: "task") {
            column(name: "form_template_id", type: "int8")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-6") {
        addColumn(tableName: "form") {
            column(name: "submitted_data_id", type: "int8")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-7") {
        addForeignKeyConstraint(baseColumnNames: "field_id", baseTableName: "value", constraintName: "FK17fqpqgyppww4t5hbfa6e31gw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "field")
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-8") {
        addForeignKeyConstraint(baseColumnNames: "account_id", baseTableName: "template", constraintName: "FK2t3w5v1eswbrafxtc12nyl2c4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "account")
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-9") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "data", constraintName: "FK35y0o4k3l48fbm1ahio0e9u1s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "nvm_user")
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-10") {
        addForeignKeyConstraint(baseColumnNames: "form_template_id", baseTableName: "task", constraintName: "FK6crjq98n0k3kv1ts4le8sspo7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "template")
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-11") {
        addForeignKeyConstraint(baseColumnNames: "account_id", baseTableName: "data", constraintName: "FK76sujgiqmogv5oaitretmb6cf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "account")
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-12") {
        addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "template", constraintName: "FKat2uubdm5vg1roosvbgkedox0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "nvm_user")
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-13") {
        addForeignKeyConstraint(baseColumnNames: "account_id", baseTableName: "field", constraintName: "FKbvx6lf3i2411uqoksvfj13wv6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "account")
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-14") {
        addForeignKeyConstraint(baseColumnNames: "account_id", baseTableName: "value", constraintName: "FKh6vxd1cd2qmkj4smk0eibrv8w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "account")
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-15") {
        addForeignKeyConstraint(baseColumnNames: "data_id", baseTableName: "value", constraintName: "FKim0jsv1m7ao9bpqhc3d6yd319", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "data")
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-16") {
        addForeignKeyConstraint(baseColumnNames: "default_data_id", baseTableName: "template", constraintName: "FKnhvq65ai099wckko7ktxp9a95", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "data")
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-17") {
        addForeignKeyConstraint(baseColumnNames: "template_id", baseTableName: "field", constraintName: "FKnjv4ikp4djrothsmynsha2y0s", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "template")
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-18") {
        addForeignKeyConstraint(baseColumnNames: "submitted_data_id", baseTableName: "form", constraintName: "FKouk76x6vdbfloda133008oouk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "data")
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-19") {
        dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "data", tableName: "form")
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-22") {
        dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "name", tableName: "form")
    }

    changeSet(author: "Siddharth (generated)", id: "1512908746428-23") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "template_id", tableName: "task")
    }
}
