package navimateforbusiness

import grails.gorm.transactions.Transactional
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

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
    // ----------------------- Private APIs ---------------------------//
}
