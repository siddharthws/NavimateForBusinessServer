package navimateforbusiness

import grails.gorm.transactions.Transactional
import navimateforbusiness.enums.Role
import navimateforbusiness.enums.TaskStatus
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

@Transactional
class FormService {
    // ----------------------- Dependencies ---------------------------//
    def taskService
    def leadService
    def templateService
    def valueService
    def userService

    // ----------------------- Public APIs ---------------------------//
    // Method to get all tasks for a user
    def getForUser(User user) {
        List<Form> forms = []

        switch (user.role) {
            case Role.ADMIN:
            case Role.MANAGER:
                userService.getRepsForUser(user).each {it -> forms.addAll(Form.findAllByAccountAndIsRemovedAndOwner(user.account, false, it)) }
                break
            case Role.CC:
                // No forms returned for customer care
                break
            case Role.REP:
                forms = Form.findAllByAccountAndIsRemovedAndOwner(user.account, false, user)
                break
        }

        // Sort forms as per creation date
        forms.sort{it.dateCreated}
        forms.reverse(true)

        // Return forms
        forms
    }

    // Method to get all tasks for a user
    def getForUserById(User user, long id) {
        // Get all forms for user
        def forms = getForUser(user)

        // Find tasks by template
        def form = forms.find {Form it -> it.id == id}

        form
    }

    // Method to get all tasks for a user
    def getForUserByTemplate(User user, Template template) {
        // Get all forms for user
        def forms = getForUser(user)

        // Find tasks by template
        def form = forms.findAll {Form it -> it.submittedData.template.id == template.id}

        form
    }

    // Method to get all tasks for a user
    def getForUserByTask(User user, Task task) {
        // Get all forms for user
        def forms = getForUser(user)

        // Find tasks by template
        def form = forms.findAll {Form it -> it.task ? it.task.id == task.id : false}

        form
    }

    // Methods to convert form objects to / from JSON
    def toJson(Form form, User user) {
        // Get lead for this form
        LeadM lead = form.task ? leadService.getForUserById(user, form.task.leadid) : null

        // Convert template properties to JSON
        def json = [
                id:             form.id,
                rep:            [id: form.owner.id, name: form.owner.name],
                lat:            form.latitude,
                lng:            form.longitude,
                submitTime:     Constants.Formatters.LONG.format(Constants.Date.IST(form.dateCreated)),
                templateId:     form.submittedData.template.id,
                distance:       getDistance(user, form),
                task:           form.task ? [id: form.task.id, name: String.valueOf(form.task.id)] : null,
                lead:           lead ? [id: lead.id, name: lead.name] : null,
                status:         form.task ? form.taskStatus : null,
                values:         []
        ]

        // Convert template values to JSON
        def values = form.submittedData.values.sort {it -> it.id}
        values.each {value ->
            json.values.push([fieldId: value.field.id, value: value.value])
        }

        json
    }

    Form fromJson(def json, User user) {
        Form form = null

        // Get existing task or create new
        if (json.id) {
            form = getForUserById(user, json.id)
            if (!form) {
                throw new ApiException("Illegal access to form", Constants.HttpCodes.BAD_REQUEST)
            }
        } else {
            form = new Form(
                    account: user.account,
                    owner: user
            )
        }

        // Add task info
        if (json.taskId != -1) {
            // Add task
            form.task = taskService.getForUserById(user, json.taskId)
            if (!form.task) {
                throw new ApiException("Illegal access to task", Constants.HttpCodes.BAD_REQUEST)
            }

            // Add task status
            form.taskStatus = json.closeTask ? TaskStatus.CLOSED : TaskStatus.OPEN
        }

        // Add location info
        form.latitude = json.latitude
        form.longitude = json.longitude

        // Prepare template data
        def template = templateService.getForUserById(user, json.templateId)
        if (!form.submittedData || form.submittedData.template != template) {
            form.submittedData = new Data(account: user.account, owner: user, template: template)
        }

        // Prepare values
        json.values.each {valueJson ->
            Value value = valueService.fromJson(valueJson, form.submittedData)

            if (!value.id) {
                form.submittedData.addToValues(value)
            }
        }

        // Add date info
        if (!form.dateCreated) {
            form.dateCreated = new Date(json.timestamp)
        }
        form.lastUpdated = new Date()

        form
    }

    // Method to remove a form object
    def remove(User user, Form form) {
        // Remove form
        form.isRemoved = true
        form.lastUpdated = new Date()
        form.save(failOnError: true, flush: true)
    }

    def getDistance(User user, Form form) {
        String dis = "-"

        if (form.task) {
            LeadM lead = leadService.getForUserById(user, form.task.leadid)
            if (lead && (lead.latitude || lead.longitude) && (form.latitude || form.longitude)) {
                // Get distance in meters
                long dist = distance(lead.latitude, form.latitude, lead.longitude, form.longitude)
                if (dist > 1000) {
                    dis = ((int) (dist / 1000)) + " km"
                } else {
                    dis = String.valueOf(dist) + " mtr"
                }
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
