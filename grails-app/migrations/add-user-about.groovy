databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1525248364715-1") {
        addColumn(tableName: "nvm_user") {
            column(name: "about", type: "longtext")
        }
    }
}
