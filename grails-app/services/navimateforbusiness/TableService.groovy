package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class TableService {
    // ----------------------- Dependencies ---------------------------//
    // ----------------------- Public APIs ---------------------------//
    // ----------------------- Private APIs ---------------------------//
    // Method to get list of columns from template list
    private def getTemplatedColumns(List<Template> templates, int startId) {
        def columns = []

        // Iterate through templates
        templates.each {template ->
            // Sort fields as per IDs
            def fields = template.fields.sort {it -> it.id}

            // Iterate through fields
            fields.each {field ->
                // Skip if column exists for this field
                if (!getColumnForField(columns, field)) {
                    // Add new column to list
                    columns.push(createColumn(startId + columns.size(), field.type, field.title))
                }
            }
        }

        columns
    }

    // Method to check if column for a given field exists in a list of columns
    private def getColumnForField(def columns, Field field) {
        def fieldColumn = null

        // Iterate through given columns
        for (int i = 0; i < columns.size(); i++) {
            def column = columns.getAt(i)

            // Compare column's title and type
            if (column.type == field.type && column.name == field.title) {
                fieldColumn = column
                break
            }
        }

        fieldColumn
    }

    // Method to get string value for tabular format from Value Object
    private def getStringFromValue(Value value) {
        def valueString = ""

        // Parse to string as per field type
        switch (value.field.type) {
            case navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT:
            case navimateforbusiness.Constants.Template.FIELD_TYPE_PHOTO:
            case navimateforbusiness.Constants.Template.FIELD_TYPE_SIGN:
            case navimateforbusiness.Constants.Template.FIELD_TYPE_LOCATION:
                valueString = value.value ? value.value : '-'
                break
            case navimateforbusiness.Constants.Template.FIELD_TYPE_NUMBER:
                valueString = value.value
                break
            case navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKBOX:
                valueString = Boolean.valueOf(value.value) ? "Yes" : "No"
                break
            case navimateforbusiness.Constants.Template.FIELD_TYPE_RADIOLIST:
                // Parse to JSON Object
                def valueJson = JSON.parse(value.value)

                // Get Option at selection index
                valueString = valueJson.options[valueJson.selection]
                break
            case navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKLIST:
                // Parse to JSON Object
                def valueJson = JSON.parse(value.value)

                // Add selected options as comma separated list
                valueJson.each{option ->
                    if (option.selection) {
                        if (valueString) {
                            valueString += ', ' + option.name
                        } else {
                            valueString = option.name
                        }
                    }
                }

                // Add Blank character if nothing was selected
                if (!valueString) {
                    valueString = '-'
                }
                break
        }

        valueString
    }

    // Method to create a column using type and name
    private def createColumn(int id, int type, String name) {
        // Set sort status of column
        boolean bSortable = !(  type == navimateforbusiness.Constants.Template.FIELD_TYPE_PHOTO ||
                                type == navimateforbusiness.Constants.Template.FIELD_TYPE_SIGN ||
                                type == navimateforbusiness.Constants.Template.FIELD_TYPE_LOCATION)

        return [
                id:     id,
                type:   type,
                name:   name,
                bSortable: bSortable
        ]
    }
}
