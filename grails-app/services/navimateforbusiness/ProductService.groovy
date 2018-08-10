package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import com.mongodb.client.FindIterable
import navimateforbusiness.objects.ObjPager
import navimateforbusiness.objects.ObjSorter
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants
import navimateforbusiness.ProductM


import static com.mongodb.client.model.Filters.*

@Transactional
class ProductService {
    // ----------------------- Dependencies ---------------------------//
    def templateService
    def fieldService
    def mongoService

    // ----------------------- Getter APIs ---------------------------//
    // Method to filter using input JSON
    def filter (User user, def requestJson, boolean bPaging) {
        // Get filters, sorter and pager
        def filter = requestJson.filter.find {it.id == Constants.Template.TYPE_PRODUCT}.filter
        ObjSorter sorter = new ObjSorter(requestJson.sorter.find {it.id == Constants.Template.TYPE_PRODUCT}.sorter)
        ObjPager pager = bPaging ? new ObjPager(requestJson.pager) : new ObjPager()

        // Filter and return
        getAllForUserByFPS(user, filter, pager, sorter)
    }

    // Method to search products in mongo database using filter, pager and sorter
    def getAllForUserByFPS(User user, def filters, ObjPager pager, ObjSorter sorter) {
        // Get mongo filters
        def pipeline = mongoService.getProductPipeline(user, filters, sorter)

        // Get results
        def dbResult = ProductM.aggregate(pipeline)
        int count = dbResult.size()

        // Apply paging
        def pagedResult = pager.apply(dbResult)

        // Return response
        return [
                rowCount: count,
                products: pagedResult.collect { (ProductM) it }
        ]
    }

    // method to get list of products using filters
    List<ProductM> getAllForUserByFilter(User user, def filters) {
        getAllForUserByFPS(user, filters, new ObjPager(), new ObjSorter()).products
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
                owner:      [id: product.ownerId, name: User.findById(product.ownerId).name],
                productId:  product.productId,
                name:       product.name,
                templateId: product.templateId,
                values:     []
        ]

        // Add templated values in JSON
        def template = templateService.getForUserById(user, product.templateId)
        def fields = fieldService.getForTemplate(template)
        fields.each {Field field ->
            json.values.push([fieldId: field.id, value: fieldService.toFrontendValue(user, field, product["$field.id"])])
        }

        json
    }

    def toExcelJson(User user, ProductM product, def params) {
        def json = [:]

        params.columns.each { def column ->
            if (column.objectId == Constants.Template.TYPE_PRODUCT) {
                json[column.name] = getColumnValue(user, column, product)
            }
        }

        json
    }

    String getColumnValue(User user, def column, ProductM product) {
        String value

        if (column.fieldName == "template") {
            value = templateService.getForUserById(user, product.templateId).name
        } else if (column.fieldName == "name") {
            value = product.name
        } else {
            value = fieldService.formatForExport(user, column.type, product[column.fieldName])
        }

        if (value == null || value.equals("")) {
            value = '-'
        }

        value
    }

    ProductM fromJson(def json, User user) {
        ProductM product = null

        // Get existing template or create new
        if (json.id) {
            product = getForUserByFilter(user, [ids: [json.id]])
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
            product["$field.id"] = fieldService.parseValue(field, json.values.find {it -> it.fieldId == field.id}.value)
        }

        // Add date info
        if (!product.dateCreated) {
            product.dateCreated = new Date()
        }
        product.lastUpdated = new Date()

        product
    }

    ProductM fromExcelJson(User user, def json) {
        ProductM product = null

        // Validate Mandatory Columns
        if (!json.ID)         {throw new ApiException("'ID' Column Not Found")}
        if (!json.Name)       {throw new ApiException("'Name' Column Not Found")}
        if (!json.Template)   {throw new ApiException("'Template' Column Not Found")}

        // Validate mandatory parameters
        if (!json.ID.value)         {throw new ApiException("Cell " + json.ID.cell + ": ID is missing") }
        if (!json.Name.value)       {throw new ApiException("Cell " + json.Name.cell + ": Name is missing")}
        if (!json.Template.value)   {throw new ApiException("Cell " + json.Template.cell + ": Template is missing")}

        // Ensure template exists
        def templates = templateService.getForUserByType(user, Constants.Template.TYPE_PRODUCT)
        def template = templates.find {it.name.equals(json.Template.value)}
        if (!template) {throw new ApiException("Cell " + json.Template.cell + ": Template not found")}

        // Get existing object from id
        product = getForUserByFilter(user, [productId: [equal: json.ID.value]])

        // Create new lead object if not found
        if (!product) {
            product = new ProductM(
                    accountId: user.account.id,
                    ownerId: user.id,
                    productId: json.ID.value
            )
        }

        // Set name from json
        product.name = json.Name.value

        // Set templated data
        product.templateId = template.id
        def fields = fieldService.getForTemplate(template)
        fields.each {field ->
            // Set value for this field from JSON received
            product["$field.id"] = fieldService.parseExcelValue(user, field, json[field.title])
        }

        // Add date info
        Date currentDate = new Date()
        if (!product.dateCreated) {product.dateCreated = currentDate}
        product.lastUpdated = currentDate

        product
    }

    // Method to remove a product object
    def remove(User user, ProductM product) {
        // Remove product
        product.isRemoved = true
        product.lastUpdated = new Date()
        product.save(failOnError: true, flush: true)
    }

    // Method to get FCMs associated with the product
    def getAffectedReps (User user, ProductM product) {
        return []
    }

    // ----------------------- Private APIs ---------------------------//
}
