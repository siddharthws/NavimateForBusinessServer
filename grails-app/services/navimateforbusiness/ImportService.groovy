package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import navimateforbusiness.enums.TaskStatus
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

import java.text.SimpleDateFormat

@Transactional
class ImportService {
    // ----------------------- Dependencies ---------------------------//
    def leadService
    def userService
    def templateService
    def fieldService
    // ----------------------- Public APIs ---------------------------//
    def checkLeadColumns(def columns) {
        // Ensure all mandatory columns are present
        if (!columns.contains("id")) {
            throw new ApiException("id column not found", Constants.HttpCodes.BAD_REQUEST)
        }
        if (!columns.contains("Name")) {
            throw new ApiException("Name column not found", Constants.HttpCodes.BAD_REQUEST)
        }
        if (!columns.contains("Address")) {
            throw new ApiException("Address column not found", Constants.HttpCodes.BAD_REQUEST)
        }
        if (!columns.contains("Template")) {
            throw new ApiException("Template column not found", Constants.HttpCodes.BAD_REQUEST)
        }
    }

    def validateTeam(def columns, def rows) {
        // Ensure all mandatory columns are present
        if (!columns.contains("Name")) {
            throw new ApiException("Name column not found", Constants.HttpCodes.BAD_REQUEST)
        }
        if (!columns.contains("Phone")) {
            throw new ApiException("Phone column not found", Constants.HttpCodes.BAD_REQUEST)
        }

        // Get phone number Column Index
        int phoneIdx = columns.indexOf("Phone")

        // Ensure all phone numbers are unique
        rows.eachWithIndex {row, i ->
            // Get all rows with this ID
            def duplicates = rows.findAll {it -> it[phoneIdx] == row[phoneIdx]}

            // Ensure phone number is unique
            if (duplicates.size() > 1) {
                // Get first dupe row index
                def dupeRowIdx = rows.indexOf(duplicates[1])

                // Throw exception
                throw new ApiException("Duplicate phone number found in cells " + getCellAddress(phoneIdx, i) + " & " + getCellAddress(phoneIdx, dupeRowIdx))
            }
        }
    }

    def checkIds(def columns, def rows) {
        // Get Id Column Index
        int idIdx = columns.indexOf("id")

        // Ensure all IDs are unique
        rows.eachWithIndex {row, i ->
            // Get all rows with this ID
            def duplicates = rows.findAll {it -> it[idIdx] == row[idIdx]}

            // Ensure ID is unique
            if (duplicates.size() > 1) {
                // Get first dupe row index
                def dupeRowIdx = rows.indexOf(duplicates[1])

                // Throw exception
                throw new ApiException("Duplicate id found in cells " + getCellAddress(idIdx, i) + " & " + getCellAddress(idIdx, dupeRowIdx))
            }
        }
    }

    def parseLeadRow(def columns, def row, int rowIdx, User user) {
        // Get and validate mandatory columns
        int idIdx = columns.indexOf("id")
        def extId = row[idIdx]
        if (!extId) {
            throw new ApiException("Invalid id at " + getCellAddress(idIdx, rowIdx), Constants.HttpCodes.BAD_REQUEST)
        }

        int nameIdx = columns.indexOf("Name")
        def name = row[nameIdx]
        if (!name) {
            throw new ApiException("Invalid Name at " + getCellAddress(nameIdx, rowIdx), Constants.HttpCodes.BAD_REQUEST)
        }

        int addressIdx = columns.indexOf("Address")
        def address = row[addressIdx]
        if (!address) {
            throw new ApiException("Invalid Address at " + getCellAddress(addressIdx, rowIdx), Constants.HttpCodes.BAD_REQUEST)
        }

        int templateIdx = columns.indexOf("Template")
        def templateName = row[templateIdx]
        def template = templateService.getForUserByName(user, templateName, Constants.Template.TYPE_LEAD)
        if (!template) {
            throw new ApiException("Invalid Template name '" + templateName + "' at " + getCellAddress(templateIdx, rowIdx), Constants.HttpCodes.BAD_REQUEST)
        }

        // Get optional columns
        int cityIdx = columns.indexOf("City")
        def city = (cityIdx != -1) ? row[cityIdx] : ""
        int stateIdx = columns.indexOf("State")
        def state = (stateIdx != -1) ? row[stateIdx] : ""
        int countryIdx = columns.indexOf("Country")
        def country = (countryIdx != -1) ? row[countryIdx] : ""
        int pinIdx = columns.indexOf("Pin code")
        def pincode = (pinIdx != -1) ? row[pinIdx] : ""

        // Update address as per extra columns
        if (city) {address += ', ' + city}
        if (state) {address += ', ' + state}
        if (country) {address += ', ' + country}
        if (pincode) {address += ', ' + pincode}

        // Prepare Lead JSON
        def leadJson = [
            extId: extId,
            name: name,
            address: address,
            templateId: template.id,
            values: parseTemplateData(columns, row, template, rowIdx)
        ]

        leadJson
    }

    // Task related import methods
    def checkTaskColumns(def columns) {
        // Ensure all mandatory columns are present
        if (!columns.contains("Lead")) {
            throw new ApiException("Lead column not found", Constants.HttpCodes.BAD_REQUEST)
        }
        if (!columns.contains("Form")) {
            throw new ApiException("Form column not found", Constants.HttpCodes.BAD_REQUEST)
        }
        if (!columns.contains("Template")) {
            throw new ApiException("Template column not found", Constants.HttpCodes.BAD_REQUEST)
        }
    }

    def parseTaskRow(def columns, def row, int rowIdx, User user) {
        int leadIdx = columns.indexOf("Lead")
        def leadId = row[leadIdx]
        def lead = leadService.getForUserByExtId(user, leadId)
        if (!lead) {
            lead = leadService.getForUserByName(user, leadId)
        }
        if (!lead) {
            throw new ApiException("Lead not found at " + getCellAddress(leadIdx, rowIdx), Constants.HttpCodes.BAD_REQUEST)
        }

        int formIdx = columns.indexOf("Form")
        def formName = row[formIdx]
        def form = templateService.getForUserByName(user, formName, Constants.Template.TYPE_FORM)
        if (!form) {
            throw new ApiException("Invalid Form name '" + formName + "' at " + getCellAddress(formIdx, rowIdx), Constants.HttpCodes.BAD_REQUEST)
        }

        int templateIdx = columns.indexOf("Template")
        def templateName = row[templateIdx]
        def template = templateService.getForUserByName(user, templateName, Constants.Template.TYPE_TASK)
        if (!template) {
            throw new ApiException("Invalid Template name '" + templateName + "' at " + getCellAddress(templateIdx, rowIdx), Constants.HttpCodes.BAD_REQUEST)
        }

        // Get optional columns
        // Validate manager
        int managerIdx = columns.indexOf("Manager")
        def manager = (managerIdx != -1) ? userService.getManagerForUserByName(user, row[managerIdx]) : null
        manager = manager ?: user

        // Validate rep
        int repIdx = columns.indexOf("Representative")
        def rep = (repIdx != -1) ? userService.getRepForUserByName(user, row[repIdx]) : null

        // Get task period
        int periodIdx = columns.indexOf("Period")
        def period = (periodIdx != -1) ? row[periodIdx] ?: 0 : 0

        // Prepare Lead JSON
        def taskJson = [
                managerId: manager.id,
                repId: rep ? rep.id : 0,
                leadId: lead.id,
                status: TaskStatus.OPEN.value,
                period: period,
                formTemplateId: form.id,
                templateId: template.id,
                values: parseTemplateData(columns, row, template, rowIdx)
        ]

        taskJson
    }

    def parseTeamRow(def columns, def row, int rowIdx, User user) {
        // Get and validate mandatory columns
        int nameIdx = columns.indexOf("Name")
        def name = row[nameIdx]
        if (!name) {
            throw new ApiException("Invalid Name at " + getCellAddress(nameIdx, rowIdx), Constants.HttpCodes.BAD_REQUEST)
        }

        int phoneIdx = columns.indexOf("Phone")
        def phone = row[phoneIdx]
        if (!phone) {
            throw new ApiException("Invalid Phone at " + getCellAddress(phoneIdx, rowIdx), Constants.HttpCodes.BAD_REQUEST)
        }

        //Check for valid manager name
        int managerIdx = columns.indexOf("Manager")
        def manager = (managerIdx != -1) ? userService.getManagerForUserByName(user, row[managerIdx]) : null
        manager = manager ?: user

        // Prepare Team JSON
        def teamJson = [
                name: name,
                phone: phone,
                managerId : manager.id
        ]

        teamJson
    }

    // ----------------------- Private APIs ---------------------------//
    private def parseTemplateData(def columns, def row, Template template, int rowIdx) {

        // Iterate through template fields
        def templateData = []
        def fields = fieldService.getForTemplate(template)
        fields.each {field ->
            int colIdx = columns.indexOf(field.title)
            if (colIdx == -1) {
                throw new ApiException("Missing column '" + field.title + "' for template '" + template.name + "'", Constants.HttpCodes.BAD_REQUEST)
            }

            // Get value for this field in row
            String value = row[colIdx]

            // Get default field value
            String defaultValue = field.value

            // Validate value
            switch (field.type) {
                case Constants.Template.FIELD_TYPE_TEXT:
                    // Assign from default if required
                    if (value == null) {
                        value = defaultValue
                    }
                    break
                case Constants.Template.FIELD_TYPE_NUMBER:
                    // Assign from default if required
                    if (value == null) {
                        value = defaultValue
                    }

                    // Validate value type as double
                    try {
                        Double val = Double.parseDouble(value)
                    } catch (Exception e) {
                        throw new ApiException("Value in cell " + getCellAddress(colIdx, rowIdx) + " must be a number", Constants.HttpCodes.BAD_REQUEST)
                    }
                    break

                case Constants.Template.FIELD_TYPE_DATE:
                    if (value) {
                        try {
                            // Parse date form importing format to long format
                            SimpleDateFormat df = new SimpleDateFormat(Constants.Date.FORMAT_LONG)
                            Date date = df.parse(value)
                        } catch (Exception e) {
                            // Bad date formatting
                            throw new ApiException("Could not parse date in cell " + getCellAddress(colIdx, rowIdx), Constants.HttpCodes.BAD_REQUEST)
                        }
                    }
                    break
                case Constants.Template.FIELD_TYPE_CHECKBOX:
                    // Assign from default if required
                    if (value == null) {
                        value = defaultValue
                    } else if ((value != 'Yes') && (value != 'No')) {
                        throw new ApiException("Value in cell " + getCellAddress(colIdx, rowIdx) + " must be either 'Yes' or 'No'", Constants.HttpCodes.BAD_REQUEST)
                    } else {
                        value = (value == "Yes") ? "true" : "false"
                    }
                    break
                case Constants.Template.FIELD_TYPE_RADIOLIST:
                    // Assign from default if required
                    if (value == null) {
                        value = defaultValue
                    } else {
                        // Get default value in JSON format
                        def defValueJson = JSON.parse(defaultValue)

                        // Check if value is present in option
                        int selection = defValueJson.options.indexOf(value)
                        if (selection == -1) {
                            throw new ApiException("Value in cell " + getCellAddress(colIdx, rowIdx) + " must be a valid option in the radiolist", Constants.HttpCodes.BAD_REQUEST)
                        }

                        defValueJson.selection = selection
                        value = defValueJson.toString()
                    }
                    break
                case Constants.Template.FIELD_TYPE_CHECKLIST:
                    // Assign from default if required
                    if (value == null) {
                        value = defaultValue
                    } else {
                        // Get selecion from value
                        String[] selections = value.split(',')

                        // Get default value in JSON format
                        def defValueJson = JSON.parse(defaultValue)

                        // Check if number of options are equal
                        if (defValueJson.size() != selection.size()) {
                            throw new ApiException("Cell " + getCellAddress(colIdx, rowIdx) + " must contain a list of " + defValueJson.size() + " comma separated values", Constants.HttpCodes.BAD_REQUEST)
                        }

                        // Add correct values to checklist
                        selections.eachWithIndex {selection, i ->
                            if (selection == 'Yes') {
                                defValueJson[i].selection = true
                            } else if (selection == 'No') {
                                defValueJson[i].selection = false
                            } else {
                                throw new ApiException("Cell " + getCellAddress(colIdx, rowIdx) + " must contain a comma separated list of 'Yes' or 'No' only", Constants.HttpCodes.BAD_REQUEST)
                            }
                        }

                        value = defValueJson.toString()
                    }
                    break
            }

            // Add key value pair to map
            templateData.push([fieldId: field.id, value: value])
        }

        return templateData
    }

    private def getCellAddress(int colIdx, int rowIdx) {
        String address = ""

        // Get character for colIdx
        if (colIdx > 25) {
            address += (char)(64 + (colIdx / 26))
        }
        address += (char)(65 + (colIdx % 26))

        // Prepare address and return
        address += String.valueOf(rowIdx + 2)
        return address
    }
}
