databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1521966953612-1") {
        dropColumn(columnName: "phone_number", tableName: "nvm_user")
    }
}
