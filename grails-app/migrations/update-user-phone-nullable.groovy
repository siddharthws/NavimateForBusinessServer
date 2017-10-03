databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1507035380993-1") {
        dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "phone_number", tableName: "nvm_user")
    }
}
