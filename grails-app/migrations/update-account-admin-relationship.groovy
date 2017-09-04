databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1504512322233-1") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "account_id", tableName: "nvm_user")
    }

    changeSet(author: "Siddharth (generated)", id: "1504512322233-2") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "admin_id", tableName: "account")
    }
}
