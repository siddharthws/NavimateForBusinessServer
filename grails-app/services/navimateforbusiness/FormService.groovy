package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class FormService {
    // ----------------------- Dependencies ---------------------------//
    def taskService
    def templateService
    def valueService
    def userService

    // ----------------------- Public APIs ---------------------------//
    // Method to get all tasks for a user
    def getForUser(User user) {
        List<Form> forms = []

        switch (user.role) {
            case navimateforbusiness.Role.ADMIN:
            case navimateforbusiness.Role.MANAGER:
                userService.getRepsForUser(user).each {it -> forms.addAll(Form.findAllByAccountAndIsRemovedAndOwner(user.account, false, it)) }
                break
            case navimateforbusiness.Role.CC:
                // No forms returned for customer care
                break
            case navimateforbusiness.Role.REP:
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
        return null
    }

    Form fromJson(def json, User user) {
        Form form = null

        // Get existing task or create new
        if (json.id) {
            form = getForUserById(user, json.id)
            if (!form) {
                throw new navimateforbusiness.ApiException("Illegal access to form", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }
        } else {
            form = new Form(
                    account: user.account,
                    owner: user
            )
        }

        // Add task info
        if (json.taskId) {
            // Add task
            form.task = taskService.getForUserById(user, json.taskId)
            if (!form.task) {
                throw new navimateforbusiness.ApiException("Illegal access to task", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }

            // Add task status
            form.taskStatus = json.closeTask ? navimateforbusiness.TaskStatus.CLOSED : navimateforbusiness.TaskStatus.OPEN
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
        form.lastUpdated = new Date(json.timestamp)

        form
    }

    // Method to remove a form object
    def remove(User user, Form form) {
        // Remove form
        form.isRemoved = true
        form.lastUpdated = new Date()
        form.save(failOnError: true, flush: true)
    }

    // ----------------------- Private APIs ---------------------------//
}
