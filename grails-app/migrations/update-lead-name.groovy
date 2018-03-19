databaseChangeLog = {
    changeSet(author: "Siddharth (generated)", id: "1521452143069-1") {
        renameColumn(tableName: "lead", oldColumnName: "title", newColumnName: "name")
    }
}
