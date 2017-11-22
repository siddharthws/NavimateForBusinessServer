databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1511329081918-1") {
        addColumn(tableName: "form") {
            column(name: "address", type: "longtext", defaultValue: "") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1511329081918-2") {
        addColumn(tableName: "form") {
            column(name: "latitude", type: "float8", defaultValueNumeric: 0) {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1511329081918-3") {
        addColumn(tableName: "form") {
            column(name: "longitude", type: "float8", defaultValueNumeric: 0) {
                constraints(nullable: "false")
            }
        }
    }
}
