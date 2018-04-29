package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.apache.poi.hssf.usermodel.HSSFDateUtil
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.text.SimpleDateFormat

@Transactional
class TableService {

    // ----------------------- Constants ---------------------------//
    // ----------------------- Dependencies ---------------------------//
    def templateService
    def fieldService

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
            values[0] = [id:lead.id, name:lead.name]
            values[1] = lead.address
            values[2] = (lead.latitude || lead.longitude) ? lead.latitude + "," + lead.longitude : '-'
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
                    name:       lead.name,
                    values:     values
            ])
        }

        return [
                columns: columns,
                rows: rows
        ]
    }

    // API to parse task objects to table format
    def parseTasks(User user, List<Task> tasks) {
        def rows = []
        // Get all task columns for this user
        def columns = getTaskColumns(user)

        // Iterate through tasks
        tasks.each {task ->
            def values = []
            // Push blank identifier for each column in values
            columns.each {it -> values.push('-')}

            // Add row data for mandatory columns
            values[0] = String.valueOf(task.id)
            values[1] = [id:task.lead.id, name:task.lead.name]
            values[2] = (task.lead.latitude || task.lead.longitude) ? task.lead.latitude + "," + task.lead.longitude : '-'
            values[3] = task.manager.name
            values[4] = task.rep ? task.rep.name : 'Unassigned'
            values[5] = task.creator.name
            values[6] = task.dateCreated.format(navimateforbusiness.Constants.Date.FORMAT_FRONTEND,
                                                navimateforbusiness.Constants.Date.TIMEZONE_IST)
            values[7] = task.dateCreated.format(navimateforbusiness.Constants.Date.FORMAT_TIME,
                                                navimateforbusiness.Constants.Date.TIMEZONE_IST)
            values[8] = task.period
            values[9] = task.formTemplate.name
            values[10] = task.templateData.template.name
            values[11] = task.status.name()

            // Iterate through template values
            task.templateData.values.each {value ->
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
                    id:         task.id,
                    name:       task.lead.name,
                    values:     values
            ])
        }

        return [
                columns: columns,
                rows: rows
        ]
    }

    // API to parse form objects to table format
    def parseForms(User user, List<Form> forms) {
        def rows = []

        // Get all form columns for this user
        def columns = getFormColumns(user)

        // Iterate through leads
        forms.each { form ->
            def values = []

            // Push blank identifier for each column in values
            columns.each { it -> values.push('-') }

            // Add row data for mandatory columns
            values[0] = form.owner.manager.name
            values[1] = form.owner.name
            values[2] = form.submittedData.template.name
            values[3] = form.dateCreated.format(navimateforbusiness.Constants.Date.FORMAT_FRONTEND,
                                                navimateforbusiness.Constants.Date.TIMEZONE_IST)
            values[4] = form.dateCreated.format(navimateforbusiness.Constants.Date.FORMAT_TIME,
                                                navimateforbusiness.Constants.Date.TIMEZONE_IST)
            values[5] = (form.latitude || form.longitude) ? form.latitude + "," + form.longitude : '-'
            values[6] = form.task ? [id:form.task.lead.id , name:form.task.lead.name] : "-"
            values[7] = form.task ? String.valueOf(form.task.id) : "-"
            values[8] = form.taskStatus ? form.taskStatus.name() : "-"

            // Iterate through template values
            form.submittedData.values.each {value ->
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
                    id:         form.id,
                    name:       form.owner.name,
                    values:     values
            ])
        }

        return [
                columns: columns,
                rows: rows
        ]
    }

    // API to parse team objects to table format
    def parseTeam(User user, List<User> team) {
        def rows = []

        // Get all team columns for this user
        def columns = getTeamColumns(user)

        // Iterate through reps
        team.each { rep ->
            def values = []

            // Push blank identifier for each column in values
            columns.each { it -> values.push('-') }

            // Add row data for mandatory columns
            values[0] = rep.name
            values[1] = '+' + rep.countryCode + " " + rep.phone
            values[2] = rep.role.name()
            values[3] = rep.manager.name

            // Add row to table
            rows.push([
                    id:         rep.id,
                    name:       rep.name,
                    values:     values
            ])
        }

        return [
                columns: columns,
                rows: rows
        ]
    }

    // API to parse excel file into columns and rows
    def parseExcel(def file) {
        // Validate File
        if (file.empty) {
            throw new navimateforbusiness.ApiException("Invalid File uploaded", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        // Convert file to workbook
        def workbook
        try {
            workbook = new XSSFWorkbook(file.getInputStream())
        } catch (Exception e) {
            throw new navimateforbusiness.ApiException("Invalid File uploaded", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        // Get sheet from workbook
        def sheet = workbook.getSheetAt(0)
        DataFormatter df = new DataFormatter()

        // Get columns from sheet
        def columns = []
        def headerRow = sheet.getRow(0)
        for (cell in headerRow.cellIterator()) {
            // Validate cell
            if (!cell) {
                throw new navimateforbusiness.ApiException("Column names cannot be empty", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }

            // Get and validate cell value
            String cellValue = df.formatCellValue(cell).trim()

            // Check for null value
            if (!cellValue) {
                throw new navimateforbusiness.ApiException("Column names cannot be empty", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }

            // Check for duplicate columns
            if (columns.contains(cellValue)) {
                throw new navimateforbusiness.ApiException("Duplicate column names are not allowed", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }

            columns.push(cellValue)
        }

        // Get rows
        def rows = []
        boolean bHeaderFlag = true
        for (sheetRow in sheet.rowIterator()) {
            // Skip first row
            if (bHeaderFlag) {
                bHeaderFlag = false
                continue
            }

            // Iterate through all cells in this row
            def row = []
            for (int i = 0; i < columns.size(); i++) {
                def cell = sheetRow.getCell(i)
                String value = ""

                if (cell) {
                    // Parse value based on cell type
                    if(cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                        switch(cell.getCachedFormulaResultType()) {
                            case Cell.CELL_TYPE_NUMERIC:
                                value = String.valueOf(cell.getNumericCellValue())
                                break
                            case Cell.CELL_TYPE_STRING:
                                value = cell.getRichStringCellValue().toString()
                                break
                        }
                    } else if ( cell.getCellType() == Cell.CELL_TYPE_NUMERIC &&
                                HSSFDateUtil.isCellDateFormatted(cell)) {
                        SimpleDateFormat sdf = new SimpleDateFormat(navimateforbusiness.Constants.Date.FORMAT_FRONTEND)
                        Date date = cell.getDateCellValue()
                        value = sdf.format(date)
                    } else {
                        value = df.formatCellValue(cell)
                    }
                }

                // Trim value
                value = value.trim()

                // Push to row
                row.push(value)
            }

            // Ignore row if all values are blank
            def nonBlankVals = row.findAll {String it -> !it}
            if (nonBlankVals.size() != row.size()) {
                // Add row to rows
                rows.push(row)
            }
        }

        // Ensure atleast 1 row is present
        if (!rows.size()) {
            throw new navimateforbusiness.ApiException("No Rows Found", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
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
                def value = row.values[column.id]

                // Parse special values as per column type
                switch (column.type) {
                    case navimateforbusiness.Constants.Template.FIELD_TYPE_LOCATION:
                        if (value != '-') {
                            value = "https://www.google.com/maps/search/?api=1&query=" + value
                        }
                        break
                    case navimateforbusiness.Constants.Template.FIELD_TYPE_LEAD:
                        if (value != "-") {
                            value = value.name
                        }
                        break
                    case navimateforbusiness.Constants.Template.FIELD_TYPE_PHOTO:
                    case navimateforbusiness.Constants.Template.FIELD_TYPE_SIGN:
                        if (value != '-') {
                            value = "https://biz.navimateapp.com/#/photos?name=" + value
                        }
                        break
                }

                // Add value to objects
                objects[i][column.name] = value
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
        columns.push(createColumn(0, navimateforbusiness.Constants.Template.FIELD_TYPE_LEAD, "Name"))
        columns.push(createColumn(1, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Address"))
        columns.push(createColumn(2, navimateforbusiness.Constants.Template.FIELD_TYPE_LOCATION, "Location"))
        columns.push(createColumn(3, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Template"))

        // Add templated columns through lead templates
        List<Template> templates = templateService.getForUserByType(user, navimateforbusiness.Constants.Template.TYPE_LEAD)
        columns += getTemplatedColumns(templates, 4)

        columns
    }

    // Method to get task columns for a given user
    private def getTaskColumns(User user) {
        def columns = []

        // Add mandatory columns for tasks
        columns.push(createColumn(0, navimateforbusiness.Constants.Template.FIELD_TYPE_TASK, "ID"))
        columns.push(createColumn(1, navimateforbusiness.Constants.Template.FIELD_TYPE_LEAD, "Lead"))
        columns.push(createColumn(2, navimateforbusiness.Constants.Template.FIELD_TYPE_LOCATION, "Location"))
        columns.push(createColumn(3, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Manager"))
        columns.push(createColumn(4, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Rep"))
        columns.push(createColumn(5, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Created By"))
        columns.push(createColumn(6, navimateforbusiness.Constants.Template.FIELD_TYPE_DATE, "Create Date"))
        columns.push(createColumn(7, navimateforbusiness.Constants.Template.FIELD_TYPE_NONE, "Create Time"))
        columns.push(createColumn(8, navimateforbusiness.Constants.Template.FIELD_TYPE_NUMBER, "Period"))
        columns.push(createColumn(9, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Form"))
        columns.push(createColumn(10, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Template"))
        columns.push(createColumn(11, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Status"))

        // Add templated columns through task templates
        List<Template> templates = templateService.getForUserByType(user, navimateforbusiness.Constants.Template.TYPE_TASK)
        columns += getTemplatedColumns(templates, 12)

        columns
    }

    private def getFormColumns(User user) {
        def columns = []

        // Add mandatory columns for forms
        columns.push(createColumn(0, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Manager"))
        columns.push(createColumn(1, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Representative"))
        columns.push(createColumn(2, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Template"))
        columns.push(createColumn(3, navimateforbusiness.Constants.Template.FIELD_TYPE_DATE, "Date"))
        columns.push(createColumn(4, navimateforbusiness.Constants.Template.FIELD_TYPE_NONE, "Time"))
        columns.push(createColumn(5, navimateforbusiness.Constants.Template.FIELD_TYPE_LOCATION, "Location"))
        columns.push(createColumn(6, navimateforbusiness.Constants.Template.FIELD_TYPE_LEAD, "Lead"))
        columns.push(createColumn(7, navimateforbusiness.Constants.Template.FIELD_TYPE_TASK, "Task ID"))
        columns.push(createColumn(8, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Task Status"))

        // Add templated columns through form templates
        List<Template> templates = templateService.getForUserByType(user, navimateforbusiness.Constants.Template.TYPE_FORM)
        columns += getTemplatedColumns(templates, 9)

        columns
    }

    private def getTeamColumns(User user) {
        def columns = []

        // Add mandatory columns for team
        columns.push(createColumn(0, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Name"))
        columns.push(createColumn(1, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Phone"))
        columns.push(createColumn(2, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Role"))
        columns.push(createColumn(3, navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT, "Manager"))

        columns
    }

    // Method to get list of columns from template list
    private def getTemplatedColumns(List<Template> templates, int startId) {
        def columns = []

        // Iterate through templates
        templates.each {template ->
            // Sort fields as per IDs
            def fields = fieldService.getForTemplate(template)

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
            case navimateforbusiness.Constants.Template.FIELD_TYPE_DATE:
                SimpleDateFormat sdf = new SimpleDateFormat(navimateforbusiness.Constants.Date.FORMAT_BACKEND)
                valueString = value.value ? sdf.parse(value.value).format(navimateforbusiness.Constants.Date.FORMAT_FRONTEND) : '-'
                break
            case navimateforbusiness.Constants.Template.FIELD_TYPE_NUMBER:
                valueString = String.valueOf(value.value)
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
