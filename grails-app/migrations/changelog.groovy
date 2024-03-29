databaseChangeLog = {
    include file: 'add-account-and-user.groovy'
    include file: 'alter-last-modified-in-account-and-user.groovy'
    include file: 'add-nullable-to-account-for-user.groovy'
    include file: 'add-biz-db.groovy'
    include file: 'update-account-admin-relationship.groovy'
    include file: 'update-form-task-nullable.groovy'
    include file: 'update-lead-manager-nullable.groovy'
    include file: 'update-lead-title-desc.groovy'
    include file: 'update-user-phone-nullable.groovy'
    include file: 'add-column-task-period.groovy'
    include file: 'update-string-longtext.groovy'
    include file: 'update-user-account-nullable.groovy'
    include file: 'update-task-fields-nullable.groovy'
    include file: 'add-column-form-latlng.groovy'
    include file: 'add-templating-db.groovy'
    include file: 'templating-porting-cleanup.groovy'
    include file: 'add-is-removed-column.groovy'
    include file: 'api-key-add.groovy'
    include file: 'api-extId-add.groovy'
    include file: 'form-add-taskstatus.groovy'
    include file: 'templating-lead-support.groovy'
    include file: 'lead-is-remove-column-add.groovy'
    include file: 'templating-bugfix.groovy'
    include file: 'task-templating-support.groovy'
    include file: 'templating-post-cleanup.groovy'
    include file: 'task-rep-nullable.groovy'
    include file: 'lead-visibility-add.groovy'
    include file: 'location-report-add.groovy'
    include file: 'account-settings-add.groovy'
    include file: 'form-task-nullable.groovy'
    include file: 'acra-add.groovy'
    include file: 'loc-report-update-date.groovy'
    include file: 'vto-field-value-add.groovy'
    include file: 'cleanup-template-default-data.groovy'
    include file: 'remove-user-status.groovy'
    include file: 'update-user-phone.groovy'
    include file: 'update-lead-name.groovy'
    include file: 'remove-user-phone-number.groovy'
    include file: 'add-task-creator.groovy'
    include file: 'cleanup-porting-task-creator.groovy'
    include file: 'default-value-remove.groovy'
    include file: 'update-task-lead-id.groovy'
    include file: 'add-user-about.groovy'
    include file: 'add-form-is-remove.groovy'
    include file: 'add-acra-details.groovy'
    include file: 'update-db--misc-cleanup.groovy'
    include file: 'add-loc-report.groovy'
    include file: 'update-task-resolution-time.groovy'
    include file: 'update-task-public-id.groovy'
    include file: 'add-account-photo.groovy'
}
