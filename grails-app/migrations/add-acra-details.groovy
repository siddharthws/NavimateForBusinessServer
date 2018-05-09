databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1525886682633-1") {
        addColumn(tableName: "acra") {
            column(name: "app_id", type: "varchar(255)")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1525886682633-2") {
        addColumn(tableName: "acra") {
            column(name: "stacktrace", type: "longtext")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1525886682633-3") {
        addColumn(tableName: "acra") {
            column(name: "version_name", type: "varchar(255)")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1525886682633-4") {
        addColumn(tableName: "acra") {
            column(name: "phone", type: "varchar(255)")
        }
    }
}
