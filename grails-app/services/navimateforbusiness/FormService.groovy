package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class FormService {
    // ----------------------- Dependencies ---------------------------//
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

    // Method to remove a form object
    def remove(User user, Form form) {
        // Remove form
        form.isRemoved = true
        form.lastUpdated = new Date()
        form.save(failOnError: true, flush: true)
    }

    // ----------------------- Private APIs ---------------------------//
}
