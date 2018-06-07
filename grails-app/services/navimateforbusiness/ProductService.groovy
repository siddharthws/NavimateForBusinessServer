package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import com.mongodb.client.FindIterable
import navimateforbusiness.enums.Role
import navimateforbusiness.enums.TaskStatus
import navimateforbusiness.enums.Visibility
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

import static com.mongodb.client.model.Filters.*

@Transactional
class ProductService {
    // ----------------------- Dependencies ---------------------------//
    def templateService
    def fieldService

    // ----------------------- Getter APIs ---------------------------//
    // Method to search products in mongo database using filter, pager and sorter
    def getAllForUserByFPS(User user, def filters, def pager, def sorter) {
        // Get mongo filters
        def mongoFilters = mongoService.getProductFilters(user, filters)

        // Get results
        FindIterable fi = productM.find(and(mongoFilters))
        int rowCount = fi.size()

        // Apply Sorting with atleast name
        if (!sorter) {sorter = [[name: Constants.Filter.SORT_ASC]]}
        def sortBson = [:]
        sorter.each {sortObj ->
            def key = sortObj.keySet()[0]
            sortBson[key] = sortObj[key]
        }
        fi = fi.sort(sortBson)

        // Apply paging
        if (pager.startIdx) {fi = fi.skip(pager.startIdx)}
        if (pager.count) {fi = fi.limit(pager.count)}

        // Prepare products array to return
        def products = []
        fi.each {ProductM product -> products.push(product)}

        // Return response
        return [
                rowCount: rowCount,
                products: products
        ]
    }

    // method to get list of products using filters
    List<ProductM> getAllForUserByFilter(User user, def filters) {
        getAllForUserByFPS(user, filters, [:], []).products
    }

    // Method to get a single product using filters
    ProductM getForUserByFilter(User user, def filters) {
        getAllForUserByFilter(user, filters)[0]
    }

    // ----------------------- Public APIs ---------------------------//
    // Methods to convert product objects to / from JSON
    def toJson(ProductM product, User user) {
        // Convert template properties to JSON
        def json = [
                id:         product.id,
                productId:  product.productId,
                name:       product.name,
                templateId: product.templateId,
                values:     []
        ]

        // Add templated values in JSON
        def template = templateService.getForUserById(user, product.templateId)
        def fields = fieldService.getForTemplate(template)
        fields.each {Field field ->
            json.values.push([fieldId: field.id, value: product["$field.id"]])
        }

        json
    }

    ProductM fromJson(def json, User user) {
        ProductM product = null

        // Get existing template or create new
        if (json.id) {
            product = getForUserById(user, json.id)
            if (!product) {
                throw new ApiException("Illegal access to product", Constants.HttpCodes.BAD_REQUEST)
            }
        }

        // Create new product object if not found
        if (!product) {
            product = new ProductM(
                    accountId: user.account.id,
                    ownerId: user.id,
            )
        }

        // Set name
        product.name = json.name

        // Set product ID if present
        product.productId = json.productId

        // Set template ID
        product.templateId = json.templateId

        // Prepare template data
        def template = templateService.getForUserById(user, json.templateId)
        def fields = fieldService.getForTemplate(template)
        fields.each {field ->
            // Set value for this field from JSON received
            product["$field.id"] = json.values.find {it -> it.fieldId == field.id}.value
        }

        // Add date info
        if (!product.dateCreated) {
            product.dateCreated = new Date()
        }
        product.lastUpdated = new Date()

        product
    }
    // ----------------------- Private APIs ---------------------------//
}
