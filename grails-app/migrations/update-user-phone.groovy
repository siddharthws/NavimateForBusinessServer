databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1521122901175-1") {
        addColumn(tableName: "nvm_user") {
            column(name: "country_code", type: "varchar(255)")
        }
    }

    changeSet(author: "Siddharth (generated)", id: "1521122901175-2") {
        addColumn(tableName: "nvm_user") {
            column(name: "phone", type: "varchar(255)")
        }
    }
}
