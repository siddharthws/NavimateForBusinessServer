databaseChangeLog = {

    changeSet(author: "vishesh (generated)", id: "1502789008231-1") {
        addColumn(tableName: "account") {
            column(name: "last_updated", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "vishesh (generated)", id: "1502789008231-2") {
        addColumn(tableName: "nvm_user") {
            column(name: "last_updated", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "vishesh (generated)", id: "1502789008231-3") {
        dropColumn(columnName: "last_modified", tableName: "account")
    }

    changeSet(author: "vishesh (generated)", id: "1502789008231-4") {
        dropColumn(columnName: "last_modified", tableName: "nvm_user")
    }
}
