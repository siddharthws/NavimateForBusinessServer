package navimateforbusiness.api

import navimateforbusiness.Field

class PortingApiController {
    // Method to port default values of all fields
    def fieldValues () {
        // Iterate through all templates
        def fields = Field.findAll()
        fields.each {field ->
            // Get default value of the field
            def value = field.template.defaultData.values.find {it -> it.fieldId == field.id}

            // Update field value
            field.value = value.value

            // save field
            field.save(failOnError: true, flush: true)
        }
    }
}
