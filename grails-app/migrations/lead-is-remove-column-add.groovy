databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1513680424301-1") {
        addColumn(tableName: "lead") {
            column(name: "is_removed", type: "boolean", defaultValueBoolean: "false") {
                constraints(nullable: "false")
            }
        }
    }
}
