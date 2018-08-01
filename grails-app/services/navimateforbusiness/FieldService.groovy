package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

import java.text.SimpleDateFormat

@Transactional
class FieldService {
    // ----------------------- Dependencies ---------------------------//

    // ----------------------- Getter Methods ---------------------------//
    List<Field> getForTemplate(Template template) {
        // Sort fields by ID
        def fields = template.fields.sort {it -> it.id}

        fields
    }

    // Field getter method to return field by id in template
    Field getForTemplateById(Template template, long id) {
        // Get all template fields
        def fields = getForTemplate(template)

        // Find field by id
        def field = fields.find {it -> it.id == id}

        field
    }

    // ----------------------- Public APIs ---------------------------//
    // Methods to convert fields objects to / from JSON
    def toJson(Field field) {
        def fieldJson = [
                id:     field.id,
                title:  field.title,
                type:   field.type,
                value:  field.value,
                settings: [bMandatory: field.bMandatory]
        ]

        fieldJson
    }

    Field fromJson(def fieldJson, Template template) {
        Field field

        // Check for existing fields
        if (fieldJson.id) {
            // Find this field in the template
            field = getForTemplateById(template, fieldJson.id)
            if (!field) {
                throw new ApiException("Illegal access to field", Constants.HttpCodes.BAD_REQUEST)
            }
        } else {
            field = new Field(account: template.account)
        }

        // Set parameters from JSON
        field.title = fieldJson.title
        field.type = fieldJson.type
        field.value = fieldJson.value
        field.bMandatory = fieldJson.settings ? fieldJson.settings.bMandatory : false

        field
    }

    def parseValue(navimateforbusiness.Field field, def value) {
        def parsedValue

        switch (field.type) {
            case Constants.Template.FIELD_TYPE_NUMBER:
                parsedValue = Double.parseDouble(value)
                break
            default:
                parsedValue = value
        }

        parsedValue
    }

    String parseExcelValue (User user, Field field, def excelValue) {
        String defValue = field.value

        // Assign Default if column not present in sheet
        if (!excelValue) {
            return defValue
        }

        String value = excelValue.value

        // Validate value
        switch (field.type) {
            case Constants.Template.FIELD_TYPE_TEXT:
                // Assign from default if required
                if (value == null) {
                    value = defValue
                }
                break
            case Constants.Template.FIELD_TYPE_NUMBER:
                // Assign from default if required
                if (value == null) {
                    value = defValue
                }

                // Validate value type as double
                try {
                    Double val = Double.parseDouble(value)
                } catch (Exception e) {
                    throw new ApiException("Cell " + excelValue.cell + ": Not a valid number", Constants.HttpCodes.BAD_REQUEST)
                }
                break

            case Constants.Template.FIELD_TYPE_DATE:
                if (value) {
                    try {
                        // Parse date form importing format to long format
                        SimpleDateFormat df = new SimpleDateFormat(Constants.Date.FORMAT_LONG)
                        Date date = df.parse(value)
                    } catch (Exception e) {
                        throw new ApiException("Cell " + excelValue.cell + ": Not a valid date", Constants.HttpCodes.BAD_REQUEST)
                    }
                }
                break
            case Constants.Template.FIELD_TYPE_CHECKBOX:
                // Assign from default if required
                if (value == null) {
                    value = defValue
                } else if ((value != 'Yes') && (value != 'No')) {
                    throw new ApiException("Cell " + excelValue.cell + ": Value should be either 'Yes' or 'No'", Constants.HttpCodes.BAD_REQUEST)
                } else {
                    value = (value == "Yes") ? "true" : "false"
                }
                break
            case Constants.Template.FIELD_TYPE_RADIOLIST:
                // Assign from default if required
                if (value == null) {
                    value = defValue
                } else {
                    // Get default value in JSON format
                    def defValueJson = JSON.parse(defValue)

                    // Check if value is present in option
                    int selection = defValueJson.options.indexOf(value)
                    if (selection == -1) {
                        throw new ApiException("Cell " + excelValue.cell + ": Value should be an available option in radiolist", Constants.HttpCodes.BAD_REQUEST)
                    }

                    defValueJson.selection = selection
                    value = defValueJson.toString()
                }
                break
            case Constants.Template.FIELD_TYPE_CHECKLIST:
                // Assign from default if required
                if (value == null) {
                    value = defValue
                } else {
                    // Get selection from value
                    String[] selections = value.split(',')

                    // Get default value in JSON format
                    def defValueJson = JSON.parse(defValue)

                    // Check if number of options are equal
                    if (defValueJson.size() != selection.size()) {
                        throw new ApiException("Cell " + excelValue.cell + ": Value must contain a comma separated list of " + defValueJson.size() + " values", Constants.HttpCodes.BAD_REQUEST)
                    }

                    // Add correct values to checklist
                    selections.eachWithIndex {selection, i ->
                        if (selection == 'Yes') {
                            defValueJson[i].selection = true
                        } else if (selection == 'No') {
                            defValueJson[i].selection = false
                        } else {
                            throw new ApiException("Cell " + excelValue.cell + ": Value must contain a comma separated list of 'Yes' or 'No' only", Constants.HttpCodes.BAD_REQUEST)
                        }
                    }

                    value = defValueJson.toString()
                }
                break
        }

        value
    }

    String formatForExport(int type, def value) {
        String returnValue = ""

        // Parse special values as per column type
        switch (type) {
            case Constants.Template.FIELD_TYPE_PHOTO:
            case Constants.Template.FIELD_TYPE_SIGN:
                returnValue = "https://biz.navimateapp.com/#/photos?name=" + value
                break
            case Constants.Template.FIELD_TYPE_CHECKBOX:
                returnValue = value ? "yes" : "no"
                break
            case Constants.Template.FIELD_TYPE_RADIOLIST:
                def valueJson = JSON.parse(value)
                returnValue = valueJson.options[valueJson.selection]
                break
            case Constants.Template.FIELD_TYPE_PRODUCT:
                returnValue = value.name
                break
            case Constants.Template.FIELD_TYPE_CHECKLIST:
                def valueJson = JSON.parse(value)
                valueJson.each {option ->
                    if (option.selection) {
                        if (returnValue) {
                            returnValue += ", " + option.name
                        } else {
                            returnValue = option.name
                        }
                    }
                }
                break
            default:
                returnValue = value
        }

        returnValue
    }
    // ----------------------- Private APIs ---------------------------//
}
