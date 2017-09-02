databaseChangeLog = {

    changeSet(author: "vishesh (generated)", id: "1502789731982-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "account_id", tableName: "nvm_user")
    }
}
