package navimateforbusiness

import grails.gorm.transactions.Transactional
import navimateforbusiness.enums.Role
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

@Transactional
class TemplateDataService {
    // ----------------------- Dependencies ---------------------------//
    def fieldService

    // ----------------------- Getters ---------------------------//
    // Method to get Template Data for user by id
    def getForUserById(User user, long id) {
        // Get lead by this id
        Data data = Data.findByAccountAndId(user.account, id)

        // Check access
        if (!checkAccess(user, data)) {
            data = null
        }

        data
    }

    // ----------------------- Public APIs ---------------------------//
    // method to convert JSOn into Template Data Domain Object
    def fromJson(def dataJson, Template template, User user) {
        Data data = null

        // Get data object by id
        if (dataJson.id) {
            data = getForUserById(user, dataJson.id)
            if (!data) {
                throw new ApiException("Template Data is unavailable", Constants.HttpCodes.BAD_REQUEST)
            }
        }

        // Create Data Object if not found
        if (!data) {
            data = new Data(account: user.account, owner: user)

            // TODO : Hack since assigning template in constructor changes default data of template
            data.template = template
        }

        // Assign value for each field in template
        def values = []
        def fields = fieldService.getForTemplate(template)
        fields.each {field ->
            // Find an existing value object with this field
            Value value = data.values.find {it -> it.field.id == field.id}

            // Create new value object if required
            if (!value) {
                value = new Value(field: field, account: data.account, data: data)
            }

            // Assign value from JSON
            value.value = dataJson[field.title]

            // Push to values array
            values.push(value)
        }
        data.values = values

        data
    }
    // ----------------------- Private APIs ---------------------------//
    private def checkAccess(User user, Data data) {
        // Validate data
        if (data == null) {
            return false
        }

        // Check for valid account
        if (user.account != data.account) {
            return false
        }

        // Admin has all access
        if (user.role == Role.ADMIN) {
            return true
        }

        // Rep's manager has all access
        if (data.owner == user || data.owner.manager == user) {
            return true
        }

        return false
    }

}
