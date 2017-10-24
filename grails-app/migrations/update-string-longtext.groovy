databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1508843377301-1") {
        modifyDataType(tableName: "form", columnName: "data", newDataType: "longtext")
        modifyDataType(tableName: "lead", columnName: "description", newDataType: "longtext")
        modifyDataType(tableName: "lead", columnName: "address", newDataType: "longtext")
    }
}
