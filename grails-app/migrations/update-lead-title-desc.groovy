databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1506448847093-1") {
        addColumn(tableName: "lead") {
            column(name: "description", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1506448847093-2") {
        addColumn(tableName: "lead") {
            column(name: "title", type: "varchar(255)")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1506448847093-3") {
        dropColumn(columnName: "company", tableName: "lead")
    }

    changeSet(author: "Siddharth (generated)", id: "1506448847093-4") {
        dropColumn(columnName: "name", tableName: "lead")
    }
}
