package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import navimateforbusiness.enums.Role
import navimateforbusiness.enums.Visibility
import navimateforbusiness.util.Constants

import java.text.SimpleDateFormat

import static com.mongodb.client.model.Filters.and
import static com.mongodb.client.model.Filters.eq
import static com.mongodb.client.model.Filters.gte
import static com.mongodb.client.model.Filters.lte
import static com.mongodb.client.model.Filters.ne
import static com.mongodb.client.model.Filters.or
import static com.mongodb.client.model.Filters.regex

@Transactional
class MongoService {
    // ----------------------- Dependencies ---------------------------//
    def templateService
    def fieldService

    // ----------------------- Public Methods ---------------------------//
    // methods to get filters for different mongo collections
    def getLeadFilters(User user, def colFilters) {
        def filters = []

        // Add all mandatory filters
        filters.addAll(getMandatoryFilters(user, colFilters))

        // Add role specific filters
        if (user.role == Role.MANAGER) {
            // Objects should either be owned by user or publicly visible for a manager to view it
            filters.push(or(eq("ownerId", user.id),
                            eq("visibility", Visibility.PUBLIC.name())))
        } else if (user.role == Role.REP) {
            // Objects should either be owned by rep's manager or publicly visible for a rep to view it
            filters.push(or(eq("ownerId", user.manager.id),
                            eq("visibility", Visibility.PUBLIC.name())))
        }

        // Apply date filter
        if (colFilters.createTime?.from)  {filters.push(gte("createTime", "$colFilters.createTime.from"))}
        if (colFilters.createTime?.to)    {filters.push(lte("createTime", "$colFilters.createTime.to"))}
        if (colFilters.updateTime?.from)  {filters.push(gte("updateTime", "$colFilters.updateTime.from"))}
        if (colFilters.updateTime?.to)    {filters.push(lte("updateTime", "$colFilters.updateTime.to"))}

        // Apply Ext ID filters if any
        if (colFilters.extId) {filters.push(eq("extId", "$colFilters.extId"))}

        // Apply Name filters if any
        if (colFilters.name?.equal) {filters.push(eq("name", "$colFilters.name.equal"))}
        if (colFilters.name?.value) {filters.push(regex("name", /.*$colFilters.name.value.*/, 'i'))}

        // Apply address / location filter
        if (colFilters.address?.value)         {filters.push(regex("address", /.*$colFilters.address.value.*/, 'i'))}
        if (colFilters.location?.bNoBlanks)    {filters.push(and(ne("latitude", 0), ne("longitude", 0)))}

        // Add all template related filters
        def templates = templateService.getForUserByType(user, Constants.Template.TYPE_LEAD)
        filters.addAll(getTemplateFilters(templates, colFilters))

        return filters
    }

    def getProductFilters(User user, def colFilters) {
        def filters = []

        // Add all mandatory filters
        filters.addAll(getMandatoryFilters(user, colFilters))

        // Apply Name filters if any
        if (colFilters.name?.equal) {filters.push(eq("name", "$colFilters.name.equal"))}
        if (colFilters.name?.value) {filters.push(regex("name", /.*$colFilters.name.value.*/, 'i'))}

        // Apply product ID filters
        if (colFilters.productId?.equal) {filters.push(eq("productId", "$colFilters.productId.equal"))}
        if (colFilters.productId?.value) {filters.push(regex("productId", /.*$colFilters.productId.value.*/, 'i'))}

        // Add all template related filters
        def templates = templateService.getForUserByType(user, Constants.Template.TYPE_PRODUCT)
        filters.addAll(getTemplateFilters(templates, colFilters))

        return filters
    }

    // ----------------------- Private Methods ---------------------------//
    // Method to get mandatory filters for all domains
    private def getMandatoryFilters(User user, def colFilters) {
        def filters = []

        // Add accountId filters
        filters.push(eq("accountId", user.accountId))

        // Add isRemoved filter by default (unless specified explicitly)
        if (!colFilters.includeRemoved) {
            filters.push(ne("isRemoved", true))
        }

        // Apply ID filters if any
        if (colFilters.ids) {
            // Create ID filters
            def idFilters = []
            colFilters.ids.each {id -> idFilters.push(eq("_id", id))}

            // Add OR of ID filters to filters
            filters.push(or(idFilters))
        }

        // Apply date filter
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.Date.FORMAT_LONG)
        if (colFilters.dateCreated?.from)  {filters.push(gte("dateCreated", sdf.parse(colFilters.dateCreated.from).toString()))}
        if (colFilters.dateCreated?.to)    {filters.push(lte("dateCreated", sdf.parse(colFilters.dateCreated.to).toString()))}
        if (colFilters.lastUpdated?.from)  {filters.push(gte("lastUpdated", sdf.parse(colFilters.lastUpdated.from).toString()))}
        if (colFilters.lastUpdated?.to)    {filters.push(lte("lastUpdated", sdf.parse(colFilters.lastUpdated.to).toString()))}

        filters
    }

    // Method to get filters using templates
    private def getTemplateFilters (templates, colFilters) {
        def filters = []

        // Apply Template Name Filter
        if (colFilters.template?.value) {
            // Get filter value
            String filterVal = colFilters.template.value

            // Get IDs of templates which have the filter value in their name
            def templateFilters = []
            templates.each {Template it ->
                if (it.name.toLowerCase().contains(filterVal.toLowerCase())) {
                    templateFilters.push(eq("templateId", it.id))
                }
            }

            // Add Template ID Filter to filters
            if (templateFilters) {
                filters.push(or(templateFilters))
            } else {
                filters.push(eq("templateId", null))
            }
        }

        // Get all fields in templates
        def fields = []
        templates.each {template -> fields.addAll(fieldService.getForTemplate(template))}

        // Find and apply filter for each field
        fields.each {Field field ->
            // Get key and filter value
            def key = "$field.id"
            def colFilter = colFilters[key]

            // Ignore if column filter not found
            if (!colFilter) {
                return
            }

            // Apply blanks filter
            Boolean bNoBlanks = colFilter.bNoBlanks ?: false
            if (bNoBlanks) {
                filters.push(ne("$key", null))
                filters.push(ne("$key", ""))
            }

            // Apply filters specific to field type
            def filterVal = colFilter.value
            if (filterVal) {
                switch (field.type) {
                    case Constants.Template.FIELD_TYPE_TEXT:
                        filters.push(getTextFilter(key, filterVal))
                        break
                    case Constants.Template.FIELD_TYPE_RADIOLIST:
                        filters.push(getRadiolistFilter(key, filterVal, JSON.parse(field.value)))
                        break
                    case Constants.Template.FIELD_TYPE_CHECKLIST:
                        filters.push(getChecklistFilter(key, filterVal, JSON.parse(field.value)))
                        break
                    case Constants.Template.FIELD_TYPE_CHECKBOX:
                        filters.push(getCheckboxFilter(key, filterVal))
                        break
                    case Constants.Template.FIELD_TYPE_NUMBER:
                    case Constants.Template.FIELD_TYPE_DATE:
                        if (filterVal.from || filterVal.to) {filters.push(getNumberFilter(key, filterVal))}
                        break
                }
            }
        }

        filters
    }

    //
    // Field Type specific filters
    //
    private def getTextFilter(String fieldName, String filterVal) {
        // Case insensitive regex filter
        regex("$fieldName", /.*$filterVal.*/, 'i')
    }

    private def getCheckboxFilter(String fieldName, String filterVal) {
        if ("yes".contains(filterVal.toLowerCase())) {
            // Filter to true
            return eq("$fieldName", "true")
        } else if ("no".contains(filterVal.toLowerCase())) {
            // Filter to false
            return eq("$fieldName", "false")
        } else {
            // Filter to invalid
            return and( ne("$fieldName", "true"),
                        ne("$fieldName", "false"))
        }
    }

    private def getRadiolistFilter(String fieldName, String filterVal, def fieldJson) {
        // Create regex filter for each option in radio list that contains the given filter value
        def idxFilters = []
        fieldJson.options.eachWithIndex {String it, int i ->
            if (it.toLowerCase().contains(filterVal.toLowerCase())) {
                idxFilters.push(regex("$fieldName", /.*\"selection\":$i.*/))
            }
        }

        // Prepare and return mongo filters
        if (idxFilters) {
            return or(idxFilters)
        } else {
            return eq("$fieldName", null)
        }
    }

    private def getChecklistFilter(String fieldName, String filterVal, def fieldJson) {
        // Create regex filter for each selected option in check list that contains the given filter value
        def optFilters = []
        fieldJson.eachWithIndex {def it, int i ->
            if (it.name.toLowerCase().contains(filterVal.toLowerCase())) {
                optFilters.push(regex("$fieldName", /.*\"name\":\"$it.name\",\"selection\":true.*/))
            }
        }

        // Prepare and return mongo filters
        if (optFilters) {
            return or(optFilters)
        } else {
            return eq("$fieldName", null)
        }
    }

    private def getNumberFilter(String fieldName, def filterVal) {
        def filters = []

        // Apply greater than filter
        if (filterVal.from) {
            filters.push(gte("$fieldName", "$filterVal.from"))
        }

        // Apply less than filter
        if (filterVal.to) {
            filters.push(lte("$fieldName", "$filterVal.to"))
        }

        return and(filters)
    }
}
