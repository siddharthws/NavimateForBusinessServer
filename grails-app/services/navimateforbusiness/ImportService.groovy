package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class ImportService {
    // ----------------------- Dependencies ---------------------------//
    def leadService
    def templateService
    def fieldService

    // ----------------------- Public APIs ---------------------------//
    def checkLeadColumns(def columns) {
        // Ensure all mandatory columns are present
        if (!columns.contains("id")) {
            throw new navimateforbusiness.ApiException("id column not found", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }
        if (!columns.contains("Name")) {
            throw new navimateforbusiness.ApiException("Name column not found", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }
        if (!columns.contains("Address")) {
            throw new navimateforbusiness.ApiException("Address column not found", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }
        if (!columns.contains("Template")) {
            throw new navimateforbusiness.ApiException("Template column not found", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
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
                throw new navimateforbusiness.ApiException("Duplicate id found in cells " + getCellAddress(idIdx, i) + " & " + getCellAddress(idIdx, dupeRowIdx))
            }
        }
    }

    def parseLeadRow(def columns, def row, int rowIdx, User user) {
        // Get and validate mandatory columns
        int idIdx = columns.indexOf("id")
        def extId = row[idIdx]
        if (!extId) {
            throw new navimateforbusiness.ApiException("Invalid id at " + getCellAddress(idIdx, rowIdx), navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        int nameIdx = columns.indexOf("Name")
        def name = row[nameIdx]
        if (!name) {
            throw new navimateforbusiness.ApiException("Invalid Name at " + getCellAddress(nameIdx, rowIdx), navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        int addressIdx = columns.indexOf("Address")
        def address = row[addressIdx]
        if (!address) {
            throw new navimateforbusiness.ApiException("Invalid Address at " + getCellAddress(addressIdx, rowIdx), navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        int templateIdx = columns.indexOf("Template")
        def templateName = row[templateIdx]
        def template = templateService.getForUserByName(user, templateName, navimateforbusiness.Constants.Template.TYPE_LEAD)
        if (!template) {
            throw new navimateforbusiness.ApiException("Invalid Template name '" + templateName + "' at " + getCellAddress(templateIdx, rowIdx), navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
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

    // ----------------------- Private APIs ---------------------------//
    private def parseTemplateData(def columns, def row, Template template, int rowIdx) {

        // Iterate through template fields
        def templateData = []
        def fields = fieldService.getForTemplate(template)
        fields.each {field ->
            int colIdx = columns.indexOf(field.title)
            if (colIdx == -1) {
                throw new navimateforbusiness.ApiException("Missing column '" + field.title + "' for template '" + template.name + "'", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }

            // Get value for this field in row
            String value = row[colIdx]

            // Get default field value
            String defaultValue = field.value

            // Validate value
            switch (field.type) {
                case navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT:
                    // Assign from default if required
                    if (value == null) {
                        value = defaultValue
                    }
                    break
                case navimateforbusiness.Constants.Template.FIELD_TYPE_NUMBER:
                    // Assign from default if required
                    if (value == null) {
                        value = defaultValue
                    }

                    // Validate value type as double
                    try {
                        Double val = Double.parseDouble(value)
                    } catch (Exception e) {
                        throw new navimateforbusiness.ApiException("Value in cell " + getCellAddress(colIdx, rowIdx) + " must be a number", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
                    }
                    break
                case navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKBOX:
                    // Assign from default if required
                    if (value == null) {
                        value = defaultValue
                    } else if ((value != 'Yes') && (value != 'No')) {
                        throw new navimateforbusiness.ApiException("Value in cell " + getCellAddress(colIdx, rowIdx) + " must be either 'Yes' or 'No'", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
                    } else {
                        value = (value == "Yes") ? "true" : "false"
                    }
                    break
                case navimateforbusiness.Constants.Template.FIELD_TYPE_RADIOLIST:
                    // Assign from default if required
                    if (value == null) {
                        value = defaultValue
                    } else {
                        // Get default value in JSON format
                        def defValueJson = JSON.parse(defaultValue)

                        // Check if value is present in option
                        int selection = defValueJson.options.indexOf(value)
                        if (selection == -1) {
                            throw new navimateforbusiness.ApiException("Value in cell " + getCellAddress(colIdx, rowIdx) + " must be a valid option in the radiolist", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
                        }

                        defValueJson.selection = selection
                        value = defValueJson.toString()
                    }
                    break
                case navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKLIST:
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
                            throw new navimateforbusiness.ApiException("Cell " + getCellAddress(colIdx, rowIdx) + " must contain a list of " + defValueJson.size() + " comma separated values", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
                        }

                        // Add correct values to checklist
                        selections.eachWithIndex {selection, i ->
                            if (selection == 'Yes') {
                                defValueJson[i].selection = true
                            } else if (selection == 'No') {
                                defValueJson[i].selection = false
                            } else {
                                throw new navimateforbusiness.ApiException("Cell " + getCellAddress(colIdx, rowIdx) + " must contain a comma separated list of 'Yes' or 'No' only", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
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
            address += (char)(65 + (colIdx / 26))
        }
        address += (char)(65 + (colIdx % 26))

        // Prepare address and return
        address += String.valueOf(rowIdx + 2)
        return address
    }
}
