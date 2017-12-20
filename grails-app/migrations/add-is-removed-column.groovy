databaseChangeLog = {

    changeSet(author: "Rohan (generated)", id: "1513793996814-1") {
        addColumn(tableName: "field") {
            column(name: "is_removed", type: "boolean", defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Rohan (generated)", id: "1513793996814-2") {
        addColumn(tableName: "task") {
            column(name: "is_removed", type: "boolean", defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Rohan (generated)", id: "1513793996814-3") {
        addColumn(tableName: "template") {
            column(name: "is_removed", type: "boolean", defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }
}
