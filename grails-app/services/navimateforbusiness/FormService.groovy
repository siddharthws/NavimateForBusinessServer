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

        // Get all reps under this user
        def reps = userService.getRepsForUser(user)

        // Iterate through each rep and get all his form submissions
        reps.each {User rep ->
            forms.addAll(Form.findAllByAccountAndOwner(user.account, rep))
        }

        // Remove forms for which templates have been removed
        forms = forms.findAll {it -> it.submittedData.template && !it.submittedData.template.isRemoved}

        // Sort forms as per creation date
        forms.sort{it.dateCreated}
        forms.reverse(true)

        // Return forms
        forms
    }

    // ----------------------- Private APIs ---------------------------//
}
