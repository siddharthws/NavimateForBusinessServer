databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1509891964711-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "account_id", tableName: "nvm_user")
    }
}
