package navimateforbusiness

import grails.gorm.transactions.Transactional
import navimateforbusiness.enums.Role
import navimateforbusiness.enums.TaskStatus
import navimateforbusiness.objects.ObjPager
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

@Transactional
class TaskService {
    // ----------------------- Dependencies ---------------------------//
    def templateService
    def userService
    def leadService
    def formService
    def mongoService
    def fieldService

    // ----------------------- Getter APIs ---------------------------//
    // Method to search leads in mongo database using filter, pager and sorter
    def getAllForUserByFPS(User user, def filters, ObjPager pager, def sorter) {
        // Get mongo filters
        def pipeline = mongoService.getTaskPipeline(user, filters, sorter)

        // Get results
        def dbResult = TaskM.aggregate(pipeline)
        int count = dbResult.size()

        // Apply paging
        def pagedResult = pager.apply(dbResult)

        // Return response
        return [
                rowCount: count,
                tasks: pagedResult.collect { (TaskM) it }
        ]
    }

    // method to get list of leads using filters
    List<TaskM> getAllForUserByFilter(User user, def filters) {
        getAllForUserByFPS(user, filters, new ObjPager(), []).tasks
    }

    // Method to get a single lead using filters
    TaskM getForUserByFilter(User user, def filters) {
        getAllForUserByFilter(user, filters)[0]
    }

    // ----------------------- Public APIs ---------------------------//
    // Methods to convert task objects to / from JSON
    def toJson(TaskM task, User user) {
        // Convert template properties to JSON
        def json = [
                id: task.id,
                publicId: task.publicId,
                lead: [id: task.lead.id, name: task.lead.name, lat: task.lead.latitude, lng: task.lead.longitude],
                manager: [id: task.managerId, name: User.findById(task.managerId).name],
                rep: task.repId ? [id: task.repId, name: User.findById(task.repId).name] : null,
                creator: [id: task.creatorId, name: User.findById(task.creatorId).name],
                status: task.status.value,
                resolutionTime: task.resolutionTimeHrs,
                period: task.period,
                dateCreated: Constants.Formatters.LONG.format(Constants.Date.IST(task.dateCreated)),
                formTemplateId: task.formTemplateId,
                templateId: task.templateId,
                values: []
        ]

        // Add templated values in JSON
        def template = templateService.getForUserById(user, task.templateId)
        def fields = fieldService.getForTemplate(template)
        fields.each {Field field ->
            json.values.push([fieldId: field.id, value: task["$field.id"]])
        }

        json
    }

    TaskM fromJson(def json, User user) {
        TaskM task = null

        // Get existing task or create new
        if (json.id) {
            task = getForUserByFilter(user, [ids: [json.id]])
            if (!task) {
                throw new ApiException("Illegal access to task", Constants.HttpCodes.BAD_REQUEST)
            }
        }

        // Create new lead object if not found
        if (!task) {
            task = new TaskM(   accountId: user.account.id,
                                isRemoved: false)
        }

        // Get manager to be assigned to task
        User rep = json.repId ? userService.getRepForUserById(user, json.repId) : null
        User manager
        if (rep) {
            manager = rep.manager
        } else if (!json.managerId || json.managerId == user.id) {
            manager = user
        } else {
            manager = userService.getManagerForUserById(user, json.managerId)
        }

        if (!manager) {
            throw new ApiException("Invalid manager assigned", Constants.HttpCodes.BAD_REQUEST)
        }

        // Set parameters from JSON
        task.managerId = manager.id
        task.creatorId = user.id
        task.repId = rep.id
        task.lead = leadService.getForUserByFilter(user, [ids: [json.leadId]])
        task.publicId = json.publicId ?: "-"
        task.status = TaskStatus.fromValue(json.status)
        task.period = json.period
        task.formTemplateId = json.formTemplateId

        // Set template ID
        task.templateId = json.templateId

        // Prepare template data
        def template = templateService.getForUserById(user, json.templateId)
        def fields = fieldService.getForTemplate(template)
        fields.each {field ->
            // Set value for this field from JSON received
            task["$field.id"] = fieldService.parseValue(field, json.values.find { it.fieldId == field.id}.value)
        }

        // Add date info
        if (!task.dateCreated) {
            task.dateCreated = new Date()
        }
        task.lastUpdated = new Date()

        task
    }

    // Method to get resolution time of task
    double getResolutionTime(TaskM task) {
        // Get elapsed time in millis
        def elapsedTimeMs = System.currentTimeMillis() - task.dateCreated.time

        // Get elapsed time in hrs
        double elapsedTimeHrs = (double) elapsedTimeMs / (double) (1000 * 60 * 60)

        // Round to 2 places and return
        Constants.round(elapsedTimeHrs, 2)
    }

    // Method to remove a task object
    def remove(User user, TaskM task) {
        // Remove all forms associated with this task
        def forms = formService.getForUserByTask(user, task)
        forms.each {Form form ->
            formService.remove(user, form)
        }

        // Close & Remove task
        task.status = TaskStatus.CLOSED
        task.isRemoved = true
        task.lastUpdated = new Date()
        task.save(failOnError: true, flush: true)
    }

    // Method to convert lead array into exportable data
    def getExportData(User user, List<TaskM> tasks, def params) {
        List objects    = []
        List fields     = []
        Map labels      = [:]

        // Validate data
        if (!tasks) {
            throw new ApiException("No rows to export", Constants.HttpCodes.BAD_REQUEST)
        } else if (!params.columns) {
            throw new ApiException("No columns to export", Constants.HttpCodes.BAD_REQUEST)
        }

        // Create one export object for each selected row
        tasks.each {it -> objects.push([:])}

        // Iterate through each column
        params.columns.each {column ->
            // Add field and label
            fields.push(column.label)
            labels.put(column.label, column.label)

            // Iterate through each lead
            tasks.eachWithIndex {task, i ->
                def value

                if (column.field == "template") {
                    value = templateService.getForUserById(user, task.templateId).name
                } else if (column.field == "formTemplate") {
                    value = templateService.getForUserById(user, task.formTemplateId).name
                } else if (column.field == "location") {
                    value = "https://www.google.com/maps/search/?api=1&query=" + task.lead.latitude + "," + task.lead.longitude
                } else {
                    value = task["$column.field"]
                    if (value) {
                        value = fieldService.formatForExport(column.type, value)
                    }
                }

                if (value == null || value.equals("")) {
                    value = '-'
                }

                // Add data to objects
                objects[i][column.label] = value
            }
        }

        return [
                objects: objects,
                fields: fields,
                labels: labels
        ]
    }

    // ----------------------- Private APIs ---------------------------//
}
