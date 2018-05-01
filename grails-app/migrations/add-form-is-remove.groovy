databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1525186558260-1") {
        addColumn(tableName: "form") {
            column(name: "is_removed", type: "boolean", defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }
}
