databaseChangeLog = {
    include file: 'add-account-and-user.groovy'
    include file: 'alter-last-modified-in-account-and-user.groovy'
    include file: 'add-nullable-to-account-for-user.groovy'
    include file: 'add-biz-db.groovy'
    include file: 'update-account-admin-relationship.groovy'
    include file: 'update-form-task-nullable.groovy'
    include file: 'update-lead-manager-nullable.groovy'
    include file: 'update-lead-title-desc.groovy'
}
