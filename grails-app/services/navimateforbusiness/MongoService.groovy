package navimateforbusiness

import com.mongodb.BasicDBObject
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import navimateforbusiness.enums.Role
import navimateforbusiness.util.Constants

import java.text.SimpleDateFormat

@Transactional
class MongoService {
    // ----------------------- Dependencies ---------------------------//
    def userService
    def leadService
    def templateService
    def fieldService

    // ----------------------- Public Methods ---------------------------//
    // Pipeline Generation methods
    def getLeadPipeline(User user, def filters, def sorter) {
        def pipeline = []

        // Add match stage
        pipeline.push(new BasicDBObject('$match', ['$and': getLeadFilters(user, filters)]))

        // Add atleast basic sorting
        if (!sorter) {sorter = [[name: Constants.Filter.SORT_ASC]]}

        // Add template order column and replace in sorter if required
        pipeline.push(getTemplateOrderStage(user, '$templateId', "template_order"))

        // Replace Template Sorting Field
        replaceSorter(sorter, "template", "template_order")

        // Add sorting stage
        pipeline.push(new BasicDBObject('$sort', getSortBson(sorter)))

        pipeline
    }

    def getProductPipeline(User user, def filters, def sorter) {
        def pipeline = []

        // Add match stage
        pipeline.push(new BasicDBObject('$match', ['$and': getProductFilters(user, filters)]))

        // Add atleast basic sorting
        if (!sorter) {sorter = [[name: Constants.Filter.SORT_ASC]]}

        // Add template order column and replace in sorter if required
        pipeline.push(getTemplateOrderStage(user, '$templateId', "template_order"))

        // Replace Template Sorting Field
        replaceSorter(sorter, "template", "template_order")

        // Add sorting stage
        pipeline.push(new BasicDBObject('$sort', getSortBson(sorter)))

        pipeline
    }

    // methods to get filters for different mongo collections
    def getLeadFilters(User user, def colFilters) {
        def filters = []

        // Add all mandatory filters
        filters.addAll(getMandatoryFilters(user, colFilters))

        // Add role specific filters
        if (user.role == Role.MANAGER) {
            // Objects should either be owned by user or by account admin
            filters.push(['$or': [['ownerId': ['$eq': user.id]],
                                  ['ownerId': ['$eq': user.account.admin.id]]]])
        } else if (user.role == Role.CC) {
            // Objects should either be owned by account admin
            filters.push(['$eq': ['ownerId': user.account.admin.id]])
        } else if (user.role == Role.REP) {
            // Objects should either be owned by rep's manager or admin
            filters.push(['$or': [['ownerId': ['$eq': user.manager.id]],
                                  ['ownerId': ['$eq': user.account.admin.id]]]])
        }

        // Apply date filter
        if (colFilters.createTime?.from)  {filters.push(['createTime': ['$gte': "$colFilters.createTime.from"]])}
        if (colFilters.createTime?.to)    {filters.push(['createTime': ['$lte': "$colFilters.createTime.to"]])}
        if (colFilters.updateTime?.from)  {filters.push(['updateTime': ['$gte': "$colFilters.updateTime.from"]])}
        if (colFilters.updateTime?.to)    {filters.push(['updateTime': ['$lte': "$colFilters.updateTime.to"]])}

        // Apply Ext ID filters if any
        if (colFilters.extId) {filters.push(['extId': ['$eq': "$colFilters.extId"]])}

        // Apply multi select filter
        if (colFilters.lead?.value) {filters.push(getMultiselectFilter("_id", colFilters.lead.value))}

        // Apply Name filters if any
        if (colFilters.name?.equal) {filters.push(['name': ['$eq': "$colFilters.name.equal"]])}
        if (colFilters.name?.value) {filters.push(['name': ['$regex': /.*$colFilters.name.value.*/, '$options': 'i']])}

        // Apply address / location filter
        if (colFilters.address?.value)         {filters.push(['address': ['$regex': /.*$colFilters.address.value.*/, '$options': 'i']])}
        if (colFilters.location?.bNoBlanks)    {filters.push(['$and': [['latitude': ['$ne': "0"]],
                                                                       ['longitude': ['$ne': "0"]]]])}

        // Add template filter
        if (colFilters.template.value) {filters.push(getMultiselectFilter("templateId", colFilters.template.value))}
        if (colFilters.template.ids) {filters.push(["templateId": ['$in': colFilters.template.ids]])}

        // Add filters for templated data
        def templates = templateService.getForUserByType(user, Constants.Template.TYPE_LEAD)
        filters.addAll(getFieldFilters(templates, colFilters))

        return filters
    }

    def getProductFilters(User user, def colFilters) {
        def filters = []

        // Add all mandatory filters
        filters.addAll(getMandatoryFilters(user, colFilters))

        // Apply Name filters if any
        if (colFilters.name?.equal) {filters.push(['name': ['$eq': "$colFilters.name.equal"]])}
        if (colFilters.name?.value) {filters.push(['name': ['$regex': /.*$colFilters.name.value.*/, '$options': 'i']])}

        // Apply product ID filters
        if (colFilters.productId?.equal) {filters.push(['productId': ['$eq': "$colFilters.productId.equal"]])}
        if (colFilters.productId?.value) {filters.push(['productId': ['$regex': /.*$colFilters.productId.value.*/, '$options': 'i']])}

        // Add template filter
        if (colFilters.template.value) {filters.push(getMultiselectFilter("templateId", colFilters.template.value))}
        if (colFilters.template.ids) {filters.push(["templateId": ['$in': colFilters.template.ids]])}

        // Add filters for templated data
        def templates = templateService.getForUserByType(user, Constants.Template.TYPE_PRODUCT)
        filters.addAll(getFieldFilters(templates, colFilters))

        return filters
    }

    // ----------------------- Private Methods ---------------------------//
    // Method to get mandatory filters for all domains
    private def getMandatoryFilters(User user, def colFilters) {
        def filters = []

        // Add accountId filters
        filters.push(['accountId': ['$eq': user.accountId]])

        // Add isRemoved filter by default (unless specified explicitly)
        if (!colFilters.includeRemoved) {
            filters.push(['isRemoved': ['$ne': true]])
        }

        // Apply ID filters if any
        if (colFilters.ids) {
            filters.push(['_id': ['$in': colFilters.ids]])
        }

        // Apply date filter
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.Date.FORMAT_LONG)
        if (colFilters.dateCreated?.value?.from)  {filters.push(["dateCreated": ['$gte': Constants.getISODate(sdf.parse(colFilters.dateCreated.value.from))]])}
        if (colFilters.dateCreated?.value?.to)    {filters.push(["dateCreated": ['$lte': Constants.getISODate(sdf.parse(colFilters.dateCreated.value.to))]])}
        if (colFilters.lastUpdated?.value?.from)  {filters.push(["lastUpdated": ['$gte': Constants.getISODate(sdf.parse(colFilters.lastUpdated.value.from))]])}
        if (colFilters.lastUpdated?.value?.to)    {filters.push(["lastUpdated": ['$lte': Constants.getISODate(sdf.parse(colFilters.lastUpdated.value.to))]])}

        filters
    }

    // Method to get filters using field values
    private def getFieldFilters(templates, colFilters) {
        def filters = []

        // Get all fields in templates
        def fields = []
        templates.each {template -> fields.addAll(fieldService.getForTemplate(template))}

        // Find and apply filter for each field
        fields.each {Field field ->
            // Get key and filter value
            String key = "$field.id"
            def colFilter = colFilters[key]

            // Ignore if column filter not found
            if (!colFilter) {
                return
            }

            // Apply blanks filter
            Boolean bNoBlanks = colFilter.bNoBlanks ?: false
            if (bNoBlanks) {
                filters.push([(key): ['$ne': null]])
                filters.push([(key): ['$ne': ""]])
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
                        if (filterVal.from || filterVal.to) {filters.push(getNumberFilter(key, filterVal))}
                        break
                    case Constants.Template.FIELD_TYPE_DATE:
                        if (filterVal.from || filterVal.to) {filters.push(getDateFilter(key, filterVal))}
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
        [(fieldName): ['$regex': /.*$filterVal.*/, '$options': 'i']]
    }

    private def getCheckboxFilter(String fieldName, String filterVal) {
        if ("yes".contains(filterVal.toLowerCase())) {
            // Filter to true
            return [(fieldName): ['$eq': "true"]]
        } else if ("no".contains(filterVal.toLowerCase())) {
            // Filter to false
            return [(fieldName): ['$eq': "false"]]
        } else {
            // Filter to invalid
            return ['$and': [[(fieldName): ['$ne': "true"]],
                             [(fieldName): ['$ne': "true"]]]]
        }
    }

    private def getRadiolistFilter(String fieldName, String filterVal, def fieldJson) {
        // Create regex filter for each option in radio list that contains the given filter value
        def idxFilters = []
        fieldJson.options.eachWithIndex {String it, int i ->
            if (it.toLowerCase().contains(filterVal.toLowerCase())) {
                idxFilters.push([(fieldName): ['$regex': /.*\"selection\":$i.*/]])
            }
        }

        // Prepare and return mongo filters
        if (idxFilters) {
            return ['$or': idxFilters]
        } else {
            return [(fieldName): ['$eq': null]]
        }
    }

    private def getChecklistFilter(String fieldName, String filterVal, def fieldJson) {
        // Create regex filter for each selected option in check list that contains the given filter value
        def optFilters = []
        fieldJson.eachWithIndex {def it, int i ->
            if (it.name.toLowerCase().contains(filterVal.toLowerCase())) {
                optFilters.push([(fieldName): ['$regex': /.*\"name\":\"$it.name\",\"selection\":true.*/]])
            }
        }

        // Prepare and return mongo filters
        if (optFilters) {
            return ['$or': optFilters]
        } else {
            return [(fieldName): ['$eq': null]]
        }
    }

    private def getNumberFilter(String fieldName, def filterVal) {
        def filters = []

        // Apply greater than filter
        if (filterVal.from) {
            filters.push([(fieldName): ['$gte': filterVal.from]])
        }

        // Apply less than filter
        if (filterVal.to) {
            filters.push([(fieldName): ['$lte': filterVal.to]])
        }

        return ['$and': filters]
    }

    private def getDateFilter(String fieldName, def filterVal) {
        def filters = []

        // Apply greater than filter
        if (filterVal.from) {
            filters.push([(fieldName): ['$gte': "$filterVal.from"]])
        }

        // Apply less than filter
        if (filterVal.to) {
            filters.push([(fieldName): ['$lte': "$filterVal.to"]])
        }

        return ['$and': filters]
    }

    private def getMultiselectFilter(String fieldName, def filterVal) {
        def filter

        switch (filterVal.type) {
            case Constants.Table.MS_INCLUDE:
                filter = [(fieldName): ['$in': filterVal.list]]
                break
            case Constants.Table.MS_EXCLUDE:
                filter = [(fieldName): ['$nin': filterVal.list]]
                break
        }

        filter
    }

    def getTemplateOrderStage(User user, String inputFieldName, String outputFieldName) {
        // Get all Templates
        def templates = templateService.getForUser(user)

        // Sort using name
        templates = templates.sort {it.name.toLowerCase()}

        // Collect Template IDs
        def templateIds = templates.collect {it.id}

        // Create pipeline stage for adding field
        def stage = new BasicDBObject('$addFields', [(outputFieldName): ['$indexOfArray': [templateIds, inputFieldName]]])

        stage
    }

    def replaceSorter(def sorter, String from, String to) {
        def sortObj = sorter.find {it.keySet()[0].equals(from)}
        if (sortObj) {
            def replaceIdx = sorter.indexOf(sortObj)
            sorter[replaceIdx] = [(to): sorter[replaceIdx][from]]
        }
    }

    def getSortBson(def sorter) {
        def sorts = [:]

        sorter.each {sortObj ->
            String key = sortObj.keySet()[0]
            int value = sortObj[key]
            sorts[key] = value
        }

        sorts
    }
}
