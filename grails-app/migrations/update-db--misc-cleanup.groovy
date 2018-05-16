databaseChangeLog = {

    changeSet(author: "Siddharth (generated)", id: "1526466179519-1") {
        dropForeignKeyConstraint(baseTableName: "visit", constraintName: "FK2djr20l2x8xsi22wtth2r0n0l")
    }

    changeSet(author: "Siddharth (generated)", id: "1526466179519-2") {
        dropForeignKeyConstraint(baseTableName: "nvm_user_task", constraintName: "FK4p7el20ecaf3o4hjwbrei83v6")
    }

    changeSet(author: "Siddharth (generated)", id: "1526466179519-3") {
        dropForeignKeyConstraint(baseTableName: "visit", constraintName: "FKb7lgihlmbih9y7xmycok6iy1s")
    }

    changeSet(author: "Siddharth (generated)", id: "1526466179519-4") {
        dropForeignKeyConstraint(baseTableName: "task", constraintName: "FKe9fdr3iipox899d4flfe0cl5d")
    }

    changeSet(author: "Siddharth (generated)", id: "1526466179519-5") {
        dropForeignKeyConstraint(baseTableName: "visit", constraintName: "FKm5e19521cgcvfmg0hbnbcvaya")
    }

    changeSet(author: "Siddharth (generated)", id: "1526466179519-6") {
        dropForeignKeyConstraint(baseTableName: "nvm_user_task", constraintName: "FKq9c2fu6trlgxt2gpn9fhr56yw")
    }

    changeSet(author: "Siddharth (generated)", id: "1526466179519-7") {
        dropTable(tableName: "nvm_user_task")
    }

    changeSet(author: "Siddharth (generated)", id: "1526466179519-8") {
        dropTable(tableName: "visit")
    }

    changeSet(author: "Siddharth (generated)", id: "1526466179519-9") {
        dropColumn(columnName: "app_id", tableName: "acra")
    }

    changeSet(author: "Siddharth (generated)", id: "1526466179519-11") {
        dropColumn(columnName: "phone", tableName: "acra")
    }

    changeSet(author: "Siddharth (generated)", id: "1526466179519-12") {
        dropColumn(columnName: "stacktrace", tableName: "acra")
    }

    changeSet(author: "Siddharth (generated)", id: "1526466179519-13") {
        dropColumn(columnName: "version_name", tableName: "acra")
    }

    changeSet(author: "Siddharth (generated)", id: "1526466179519-14") {
        dropDefaultValue(columnDataType: "boolean", columnName: "is_removed", tableName: "form")
    }

    changeSet(author: "Siddharth (generated)", id: "1526466179519-15") {
        dropDefaultValue(columnDataType: "varchar(255)", columnName: "task_status", tableName: "form")
    }

    changeSet(author: "Siddharth (generated)", id: "1526466179519-16") {
        dropDefaultValue(columnDataType: "varchar(255)", columnName: "visibility", tableName: "lead")
    }
}
