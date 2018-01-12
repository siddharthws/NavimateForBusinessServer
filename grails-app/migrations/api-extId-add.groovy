databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1515777065509-1") {
        addColumn(tableName: "lead") {
            column(name: "ext_id", type: "varchar(255)")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1515777065509-2") {
        addColumn(tableName: "nvm_user") {
            column(name: "ext_id", type: "varchar(255)")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1515777065509-3") {
        addColumn(tableName: "task") {
            column(name: "ext_id", type: "varchar(255)")
        }
    }
}
