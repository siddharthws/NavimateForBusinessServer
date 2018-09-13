package navimateforbusiness

import com.mongodb.BasicDBObject
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import navimateforbusiness.enums.Role
import navimateforbusiness.objects.ObjSorter
import navimateforbusiness.util.Constants

import java.text.SimpleDateFormat

@Transactional
class MongoService {
    // ----------------------- Dependencies ---------------------------//
    def userService
    def leadService
    def taskService
    def templateService
    def fieldService

    // ----------------------- Public Methods ---------------------------//
    // Pipeline Generation methods
    def getLeadPipeline(User user, def filters, ObjSorter sorter) {
        def pipeline = []

        // Add match stage
        pipeline.push(new BasicDBObject('$match', ['$and': getLeadFilters(user, filters)]))

        // Add atleast basic sorting
        if (!sorter.list) {sorter.list = [[name: Constants.Filter.SORT_ASC]]}

        // Add template order column and replace in sorter if required
        pipeline.push(getTemplateOrderStage(user, '$templateId', "template_order"))

        // Replace Template Sorting Field
        sorter.replace("template", "template_order")

        // Add sorting stage
        pipeline.push(new BasicDBObject('$sort', sorter.getBson()))

        pipeline
    }

    def getTaskPipeline(User user, def filters, ObjSorter sorter) {
        def pipeline = []

        // Add match stage
        pipeline.push(new BasicDBObject('$match', ['$and': getTaskFilters(user, filters)]))

        // Add atleast basic sorting
        if (!sorter.list) {sorter.list =   [[status: Constants.Filter.SORT_DESC],
                                            [dateCreated: Constants.Filter.SORT_DESC]]}

        // Add template order column and replace in sorter if required
        pipeline.push(getTemplateOrderStage(user, '$templateId', "template_order"))
        sorter.replace("template", "template_order")

        // Add rep order column and replace in sorter if required
        pipeline.push(getUserOrderStage(user, '$repId', "rep_order"))
        sorter.replace("rep", "rep_order")

        // Add manager order column and replace in sorter if required
        pipeline.push(getUserOrderStage(user, '$managerId', "manager_order"))
        sorter.replace("manager", "manager_order")

        // Add creator order column and replace in sorter if required
        pipeline.push(getUserOrderStage(user, '$creatorId', "creator_order"))
        sorter.replace("creator", "creator_order")

        // Add lead order column and replace in sorter if required
        pipeline.push(getLeadOrderStage(user, '$lead', "lead_order", filters.lead?.ids))
        sorter.replace("lead", "lead_order")

        // Add sorting stage
        pipeline.push(new BasicDBObject('$sort', sorter.getBson()))

        pipeline
    }

    def getFormPipeline(User user, def filters, ObjSorter sorter) {
        def pipeline = []

        // Add match stage
        pipeline.push(new BasicDBObject('$match', ['$and': getFormFilters(user, filters)]))

        // Add atleast basic sorting
        if (!sorter.list) {sorter.list = [[dateCreated: Constants.Filter.SORT_DESC]]}

        // Add template order column and replace in sorter
        pipeline.push(getTemplateOrderStage(user, '$templateId', "template_order"))
        sorter.replace("template", "template_order")

        // Add rep order column and replace in sorter if required
        pipeline.push(getUserOrderStage(user, '$repId', "rep_order"))
        sorter.replace("rep", "rep_order")

        // Add lead order column and replace in sorter if required
        pipeline.push(getTaskOrderStage(user, '$task', "task_order", filters.task?.ids))
        sorter.replace("task", "task_order")

        // Add sorting stage
        pipeline.push(new BasicDBObject('$sort', sorter.getBson()))

        pipeline
    }

    def getProductPipeline(User user, def filters, ObjSorter sorter) {
        def pipeline = []

        // Add match stage
        pipeline.push(new BasicDBObject('$match', ['$and': getProductFilters(user, filters)]))

        // Add atleast basic sorting
        if (!sorter.list) {sorter.list = [[name: Constants.Filter.SORT_ASC]]}

        // Add template order column and replace in sorter if required
        pipeline.push(getTemplateOrderStage(user, '$templateId', "template_order"))

        // Replace Template Sorting Field
        sorter.replace("template", "template_order")

        // Add sorting stage
        pipeline.push(new BasicDBObject('$sort', sorter.getBson()))

        pipeline
    }

    // methods to get filters for different mongo collections
    def getLeadFilters(User user, def colFilters) {
        def filters = []

        // Add all mandatory filters
        filters.addAll(getMandatoryFilters(user, colFilters))

        // Add role specific filters
        if (user.role == Role.MANAGER) {
            def repIds = userService.getRepsForUser(user).collect {it.id}
            // Objects should either be owned by user or by account admin
            filters.push(['$or': [['ownerId': ['$eq': user.id]],
                                  ['ownerId': ['$in': repIds]],
                                  ['ownerId': ['$eq': user.account.admin.id]]]])
        } else if (user.role == Role.CC) {
            // Objects should either be owned by account admin
            filters.push(['$eq': ['ownerId': user.account.admin.id]])
        } else if (user.role == Role.REP) {
            // Objects should either be owned by rep's manager or admin
            filters.push(['$or': [['ownerId': ['$eq': user.manager.id]],
                                  ['ownerId': ['$eq': user.id]],
                                  ['ownerId': ['$eq': user.account.admin.id]]]])
        }

        // Apply date filter
        if (colFilters.createTime?.from)  {filters.push(['createTime': ['$gte': "$colFilters.createTime.from"]])}
        if (colFilters.createTime?.to)    {filters.push(['createTime': ['$lte': "$colFilters.createTime.to"]])}
        if (colFilters.updateTime?.from)  {filters.push(['updateTime': ['$gte': "$colFilters.updateTime.from"]])}
        if (colFilters.updateTime?.to)    {filters.push(['updateTime': ['$lte': "$colFilters.updateTime.to"]])}

        // Apply Ext ID filters if any
        if (colFilters.extId) {filters.push(['extId': ['$eq': "$colFilters.extId"]])}

        // Apply Name filters if any
        if (colFilters.name?.equal) {filters.push(['name': ['$eq': "$colFilters.name.equal"]])}
        if (colFilters.name?.regex) {filters.push(['name': ['$regex': /.*$colFilters.name.regex.*/, '$options': 'i']])}
        if (colFilters.name?.value) {filters.push(getMultiselectFilter("_id", colFilters.name.value))}

        // Apply address / location filter
        if (colFilters.address?.value)         {filters.push(['address': ['$regex': /.*$colFilters.address.value.*/, '$options': 'i']])}
        if (colFilters.location?.bNoBlanks)    {filters.push(['$and': [['latitude': ['$ne': 0]],
                                                                       ['longitude': ['$ne': 0]]]])}

        // Add template filter
        if (colFilters.template?.value) {filters.push(getMultiselectFilter("templateId", colFilters.template.value))}
        if (colFilters.template?.ids) {filters.push(["templateId": ['$in': colFilters.template.ids]])}

        // Add filters for templated data
        def templates = templateService.getForUserByType(user, Constants.Template.TYPE_LEAD)
        filters.addAll(getFieldFilters(templates, colFilters))

        return filters
    }

    def getTaskFilters(User user, def colFilters) {
        def filters = []

        // Add all mandatory filters
        filters.addAll(getMandatoryFilters(user, colFilters))

        // Add role specific filters
        switch (user.role) {
            case Role.MANAGER:
                // Get tasks where this user is manager
                filters.push(['managerId': ['$eq': user.id]])
                break
            case Role.CC:
                // Get tasks where this user is creator
                filters.push(['creatorId': ['$eq': user.id]])
                break
            case Role.REP:
                // Get tasks where this user is rep
                filters.push(['repId': ['$eq': user.id]])
                break
        }

        // Apply ID filter
        if (colFilters.publicId?.equal)     {filters.push(['publicId': ['$eq': "$colFilters.publicId.equal"]])}
        if (colFilters.publicId?.value)     {filters.push(getMultiselectFilter("_id", colFilters.publicId.value))}
        if (colFilters.publicId?.regex)     {filters.push(getTextFilter("publicId", colFilters.publicId.regex))}
        if (colFilters.publicId?.bNoBlanks) {filters.push(['publicId': ['$ne': "-"]])}

        // Apply manager, rep and creator filters
        if (colFilters.manager?.value)  {filters.push(getMultiselectFilter("managerId", colFilters.manager.value))}
        if (colFilters.rep?.value)      {filters.push(getMultiselectFilter("repId", colFilters.rep.value))}
        if (colFilters.rep?.bNoBlanks)  {filters.push(['repId': ['$ne': null]])}
        if (colFilters.rep?.ids)        {filters.push(['repId': ['$in': colFilters.rep.ids]])}
        if (colFilters.creator?.value)  {filters.push(getMultiselectFilter("creatorId", colFilters.creator.value))}

        // Apply resolution time filter
        if (colFilters.resolutionTimeHrs?.value?.from)  {filters.push(['resolutionTimeHrs': ['$gte': colFilters.resolutionTimeHrs.value.from]])}
        if (colFilters.resolutionTimeHrs?.value?.to)    {filters.push(['resolutionTimeHrs': ['$lte': colFilters.resolutionTimeHrs.value.to]])}
        if (colFilters.resolutionTimeHrs?.bNoBlanks)    {filters.push(['resolutionTimeHrs': ['$ne': -1]])}

        // Apply Status filter
        if (colFilters.status?.value)  {filters.push(['status': ['$regex': /.*$colFilters.status.value.*/, '$options': 'i']])}

        // Apply Lead Filter
        if (colFilters.lead?.ids)           {filters.push(['lead': ['$in': colFilters.lead.ids]])}

        // Add template filter
        if (colFilters.formTemplate?.value) {filters.push(getMultiselectFilter("formTemplateId", colFilters.formTemplate.value))}
        if (colFilters.formTemplate?.ids) {filters.push(["formTemplateId": ['$in': colFilters.formTemplate.ids]])}

        // Add template filter
        if (colFilters.template?.value) {filters.push(getMultiselectFilter("templateId", colFilters.template.value))}
        if (colFilters.template?.ids) {filters.push(["templateId": ['$in': colFilters.template.ids]])}

        // Add filters for templated data
        def templates = templateService.getForUserByType(user, Constants.Template.TYPE_TASK)
        filters.addAll(getFieldFilters(templates, colFilters))

        return filters
    }

    def getFormFilters(User user, def colFilters) {
        def filters = []

        // Add all mandatory filters
        filters.addAll(getMandatoryFilters(user, colFilters))

        // Add role specific filters
        switch (user.role) {
            case Role.MANAGER:
                // Get forms submitted by this manager's reps
                def reps = userService.getRepsForUser(user)
                def repIds = reps.collect {it.id}
                filters.push(['ownerId': ['$in': repIds]])
                break
            case Role.CC:
                // Get forms submitted in tasks created by this CC
                def tasks = taskService.getAllForUserByFilter(user, [creator: [ids: [user.id]]])
                def taskIds = tasks.each {it.id}
                filters.push(['task': ['$in': taskIds]])
                break
            case Role.REP:
                // Get forms submitted by this user only
                filters.push(['ownerId': ['$eq': user.id]])
                break
        }

        // Apply rep filters
        if (colFilters.rep?.ids)                {filters.push(['repId': ['$in': colFilters.rep.ids]])}
        if (colFilters.rep?.value)              {filters.push(getMultiselectFilter("ownerId", colFilters.rep.value))}

        // Apply location filters
        if (colFilters.location?.bNoBlanks)    {filters.push(['$and': [['latitude': ['$ne': 0]],
                                                                       ['longitude': ['$ne': 0]]]])}

        // Apply distance filter
        if (colFilters.distanceKm?.value?.from)   {filters.push(['distanceKm': ['$gte': colFilters.distanceKm.value.from]])}
        if (colFilters.distanceKm?.value?.to)     {filters.push(['distanceKm': ['$lte': colFilters.distanceKm.value.to]])}
        if (colFilters.distanceKm?.bNoBlanks)     {filters.push(['distanceKm': ['$ne': -1]])}

        // Apply Status filter
        if (colFilters.taskStatus?.value)       {filters.push(getTextFilter("taskStatus", colFilters.taskStatus.value))}
        if (colFilters.taskStatus?.bNoBlanks)   {filters.push(['taskStatus': ['$ne': null]])}

        // Apply Task Filter
        if (colFilters.task) {
            def taskFilters = []
            if (colFilters.task?.ids)           {taskFilters.push(['task': ['$in': colFilters.task.ids]])}
            if (!colFilters.task?.bNoBlanks)    {taskFilters.push(['task': ['$eq': null]])}
            if (taskFilters)                    {filters.push(['$or': taskFilters])}
        }

        // Add template filter
        if (colFilters.template?.value) {filters.push(getMultiselectFilter("templateId", colFilters.template.value))}
        if (colFilters.template?.ids) {filters.push(["templateId": ['$in': colFilters.template.ids]])}

        // Add filters for templated data
        def templates = templateService.getForUserByType(user, Constants.Template.TYPE_FORM)
        filters.addAll(getFieldFilters(templates, colFilters))

        return filters
    }

    def getProductFilters(User user, def colFilters) {
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

        // Apply Name filters if any
        if (colFilters.name?.equal) {filters.push(['name': ['$eq': "$colFilters.name.equal"]])}
        if (colFilters.name?.value) {filters.push(getMultiselectFilter("_id", colFilters.name.value))}
        if (colFilters.name?.regex) {filters.push(['name': ['$regex': /.*$colFilters.name.regex.*/, '$options': 'i']])}

        // Apply product ID filters
        if (colFilters.productId?.equal) {filters.push(['productId': ['$eq': "$colFilters.productId.equal"]])}
        if (colFilters.productId?.value) {filters.push(['productId': ['$regex': /.*$colFilters.productId.value.*/, '$options': 'i']])}

        // Add template filter
        if (colFilters.template?.value) {filters.push(getMultiselectFilter("templateId", colFilters.template.value))}
        if (colFilters.template?.ids)   {filters.push(["templateId": ['$in': colFilters.template.ids]])}

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
                    case Constants.Template.FIELD_TYPE_PRODUCT:
                        filters.push(getMultiselectFilter(key, filterVal))
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

    def getUserOrderStage(User user, String inputFieldName, String outputFieldName) {
        // Get all Templates
        def users = userService.getAllForAccount(user.account)

        // Sort using name
        users = users.sort {it.name.toLowerCase()}

        // Collect Template IDs
        def userIds = users.collect {it.id}

        // Create pipeline stage for adding field
        def stage = new BasicDBObject('$addFields', [(outputFieldName): ['$indexOfArray': [userIds, inputFieldName]]])

        stage
    }

    def getLeadOrderStage(User user, String inputFieldName, String outputFieldName, def leadIds) {
        if (!leadIds) {
            // Get all Templates
            def leads = leadService.getAllForUserByFilter(user, [:])

            // Sort using name
            leads = leads.sort {it.name.toLowerCase()}

            // Collect Template IDs
            leadIds = leads.collect {it.id}
        }

        // Create pipeline stage for adding field
        def stage = new BasicDBObject('$addFields', [(outputFieldName): ['$indexOfArray': [leadIds, inputFieldName]]])

        stage
    }

    def getTaskOrderStage(User user, String inputFieldName, String outputFieldName, def taskIds) {
        if (!taskIds) {
            // Get all tasks
            def tasks = taskService.getAllForUserByFilter(user, [:])

            // Sort using name
            tasks = tasks.sort {it.publicId.toLowerCase()}

            // Collect IDs
            taskIds = tasks.collect {it.id}
        }

        // Create pipeline stage for adding field
        def stage = new BasicDBObject('$addFields', [(outputFieldName): ['$indexOfArray': [taskIds, inputFieldName]]])

        stage
    }
}
