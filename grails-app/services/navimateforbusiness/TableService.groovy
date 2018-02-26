package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class TableService {
    // ----------------------- Dependencies ---------------------------//
    def templateService

    // ----------------------- Public APIs ---------------------------//
    // API parse lead objects to table format
    def parseLeads(User user, List<Lead> leads) {
        def rows = []

        // Get all lead columns for this user
        def columns = getLeadColumns(user)

        // Iterate through leads
        leads.each {lead ->
            def values = []

            // Push blank identifier for each column in values
            columns.each {it -> values.push('-')}

            // Add row data for mandatory columns
            values[0] = lead.title
            values[1] = lead.address
            values[2] = lead.latitude + "," + lead.longitude
            values[3] = lead.templateData.template.name

            // Iterate through template values
            lead.templateData.values.each {value ->
                // Get column for the field
                def column = getColumnForField(columns, value.field)

                if (column) {
                    // Get column index
                    int colIdx = columns.indexOf(column)

                    // Update value
                    values[colIdx] = getStringFromValue(value)
                }
            }

            // Add row to table
            rows.push([
                    id:         lead.id,
                    values:     values
            ])
        }

        return [
                columns: columns,
                rows: rows
        ]
    }

    // APi to get export data
    def getExportData(def table, def params) {
        List objects    = []
        List fields     = []
        Map labels      = [:]

        // Validate data
        if (!table.rows.size()) {
            throw new navimateforbusiness.ApiException("No rows to export", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        } else if (!params.order) {
            throw new navimateforbusiness.ApiException("No columns to export", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        // Create array of selected rows that need to be exported
        def selectedRows = []
        if (params.selection) {
            // HACK since findAll is not working
            table.rows.each {row ->
                // Check if row id is present in selection array
                def selectedId = params.selection.find {it -> it == row.id}
                if (selectedId == row.id) {
                    selectedRows.push(row)
                }
            }
        } else {
            selectedRows = table.rows
        }

        // Create one export object for each selected row
        selectedRows.each {it -> objects.push([:])}

        // Add key value pairs to objects as per column order
        params.order.each {colId ->
            // Get column
            def column = table.columns.find {it -> it.id == colId}

            // Add field and label
            fields.push(column.name)
            labels.put(column.name, column.name)

            // Iterate through each selected row
            selectedRows.eachWithIndex {row, i ->
                // Add row to objects
                objects[i][column.name] =  row.values[column.id]
            }
        }

        return [
                objects: objects,
                fields: fields,
                labels: labels
        ]
    }

    // ----------------------- Private APIs ---------------------------//
    // Method to get lead columns for a given user
    private def getLeadColumns(User user) {
        def columns = []

        // Add mandatory columns for leads
        columns.push(createColumn(0, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Title"))
        columns.push(createColumn(1, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Address"))
        columns.push(createColumn(2, navimateforbusiness.Constants.Template.FIELD_TYPE_LOCATION, "Location"))
        columns.push(createColumn(3, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Template"))

        // Add templated columns through lead templates
        List<Template> templates = templateService.getForUser(user, navimateforbusiness.Constants.Template.TYPE_LEAD)
        columns += getTemplatedColumns(templates, 4)

        columns
    }

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
