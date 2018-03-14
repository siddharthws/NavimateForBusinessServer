package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class ValueService {
    // ----------------------- Dependencies ---------------------------//
    def fieldService

    // ----------------------- Getter APIs ---------------------------//
    // ----------------------- Public APIs ---------------------------//
    // Methods to convert fields objects to / from JSON
    def toJson(Value value) {
        return  [
            fieldId:    value.field.id,
            value:      value.value
        ]
    }

    Value fromJson(def valueJson, Data data) {
        Value value

        // Check if value is present in Data
        value = data.values.find {it -> it.field.id == valueJson.fieldId}

        // Create new value if not found
        if (!value) {
            def field = fieldService.getForTemplateById(data.template, valueJson.fieldId)
            value = new Value(account: data.account, field: field, data: data)
        }

        // Update value
        value.value = valueJson.value

        value
    }

    // ----------------------- Private APIs ---------------------------//
}
