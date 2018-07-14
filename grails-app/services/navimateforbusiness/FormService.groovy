package navimateforbusiness

import grails.gorm.transactions.Transactional
import navimateforbusiness.enums.TaskStatus
import navimateforbusiness.objects.ObjPager
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

@Transactional
class FormService {
    // ----------------------- Dependencies ---------------------------//
    def userService
    def taskService
    def leadService
    def mongoService
    def templateService
    def fieldService

    // ----------------------- Public APIs ---------------------------//
    // Method to search leads in mongo database using filter, pager and sorter
    def getAllForUserByFPS(User user, def filters, ObjPager pager, def sorter) {
        // Get mongo filters
        def pipeline = mongoService.getFormPipeline(user, filters, sorter)

        // Get results
        def dbResult = FormM.aggregate(pipeline)
        int count = dbResult.size()

        // Apply paging
        def pagedResult = pager.apply(dbResult)

        // Return response
        return [
                rowCount: count,
                forms: pagedResult.collect { (FormM) it }
        ]
    }

    // method to get list of leads using filters
    List<FormM> getAllForUserByFilter(User user, def filters) {
        getAllForUserByFPS(user, filters, new ObjPager(), []).forms
    }

    // Method to get a single lead using filters
    FormM getForUserByFilter(User user, def filters) {
        getAllForUserByFilter(user, filters)[0]
    }

    // Methods to convert form objects to / from JSON
    def toJson(FormM form, User user) {
        // Convert template properties to JSON
        def json = [
                id:             form.id,
                rep:            [id: form.ownerId, name: User.findById(form.ownerId).name],
                lat:            form.latitude,
                lng:            form.longitude,
                submitTime:     Constants.Formatters.LONG.format(Constants.Date.IST(form.dateCreated)),
                templateId:     form.templateId,
                distance:       form.distanceKm,
                task:           form.task ? [id: form.task.id, name: form.task.publicId] : null,
                lead:           form.task ? [id: form.task.lead.id, name: form.task.lead.name] : null,
                status:         form.task ? form.taskStatus.name() : null,
                values:         []
        ]

        // Add templated values in JSON
        def template = templateService.getForUserById(user, form.templateId)
        def fields = fieldService.getForTemplate(template)
        fields.each {Field field ->
            json.values.push([fieldId: field.id, value: form["$field.id"]])
        }

        json
    }

    FormM fromJson(def json, User user) {
        FormM form = null

        // Get existing task or create new
        if (json.id) {
            form = getForUserByFilter(user, [ids: [json.id]])
            if (!form) {
                throw new ApiException("Illegal access to form", Constants.HttpCodes.BAD_REQUEST)
            }
        }

        if (!form) {
            form = new FormM(   accountId: user.account.id,
                                ownerId: user.id)
        }

        // Add task info
        if (json.taskId != -1) {
            // Add task
            form.task = taskService.getForUserByFilter(user, [ids: [json.taskId]])
            if (!form.task) {
                throw new ApiException("Illegal access to task", Constants.HttpCodes.BAD_REQUEST)
            }

            // Add task status
            form.taskStatus = json.closeTask ? TaskStatus.CLOSED : TaskStatus.OPEN
        }

        // Add location info
        form.latitude = json.latitude
        form.longitude = json.longitude
        form.distanceKm = getDistance(user, form)

        // Set template ID
        form.templateId = json.templateId

        // Prepare template data
        def template = templateService.getForUserById(user, json.templateId)
        def fields = fieldService.getForTemplate(template)
        fields.each {field ->
            // Set value for this field from JSON received
            form["$field.id"] = fieldService.parseValue(field, json.values.find {it.fieldId == field.id}.value)
        }

        // Add date info
        if (!form.dateCreated) {
            form.dateCreated = new Date(json.timestamp)
        }
        form.lastUpdated = new Date()

        form
    }

    // Method to convert lead array into exportable data
    def getExportData(User user, List<FormM> forms, def params) {
        List objects    = []
        List fields     = []
        Map labels      = [:]

        // Validate data
        if (!forms) {
            throw new ApiException("No rows to export", Constants.HttpCodes.BAD_REQUEST)
        } else if (!params.columns) {
            throw new ApiException("No columns to export", Constants.HttpCodes.BAD_REQUEST)
        }

        // Create one export object for each selected row
        forms.each {it -> objects.push([:])}

        // Iterate through each column
        params.columns.each {column ->
            // Add field and label
            fields.push(column.label)
            labels.put(column.label, column.label)

            // Iterate through each lead
            forms.eachWithIndex {form, i ->
                def value

                if (column.field == "template") {
                    value = templateService.getForUserById(user, form.templateId).name
                } else if (column.field == "location") {
                    value = "https://www.google.com/maps/search/?api=1&query=" + form.latitude + "," + form.longitude
                } else {
                    value = form["$column.field"]
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

    // Method to remove a form object
    def remove(User user, FormM form) {
        // Remove form
        form.isRemoved = true
        form.lastUpdated = new Date()
        form.save(failOnError: true, flush: true)
    }

    def getDistance(User user, FormM form) {
        double dis = -1

        if (form.task) {
            LeadM lead = form.task.lead
            if (lead && (lead.latitude || lead.longitude) && (form.latitude || form.longitude)) {
                // Get distance in meters
                long dist = distance(lead.latitude, form.latitude, lead.longitude, form.longitude)
                dis = (double) dist / (double) 1000
                dis = Constants.round(dis, 2)
            }
        }

        dis
    }

    // ----------------------- Private APIs ---------------------------//
    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    private long distance(double lat1, double lat2, double lon1,
                                  double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        return (long) distance
    }
}
