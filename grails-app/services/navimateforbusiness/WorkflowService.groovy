package navimateforbusiness

import grails.gorm.transactions.Transactional
import navimateforbusiness.objects.LatLng
import navimateforbusiness.objects.ObjWorkflow

@Transactional
class WorkflowService {

    def leadService

    def exec(ObjWorkflow objWf) {
        switch (objWf.type) {
            case ObjWorkflow.TYPE_SUBMIT_FORM:
                execFormSubmissionWorkflow(objWf)
                break
        }
    }

    private def execFormSubmissionWorkflow (ObjWorkflow objWorkflow) {
        // Parse Form
        def form = (FormM) objWorkflow.input

        switch (objWorkflow.user.accountId) {
            case 131927:
                execZbcFormSubmissionWf(objWorkflow.user, form)
                break
        }
    }

    def execZbcFormSubmissionWf (User user, FormM form) {
        // Check form type
        switch (form.templateId) {
            case 147238:
                execZbcRentAgreementFormWf(user, form)
                break
        }
    }

    private def execZbcRentAgreementFormWf (User user, FormM form) {
        // Create list of templated values by transforming form values
        def values = [:]
        values["149507"] = form["147246"]
        values["149508"] = form["147247"]
        values["149509"] = form["147249"]
        values["149510"] = form["147250"]
        values["149511"] = form["147251"]
        values["149512"] = form["147252"]
        values["149513"] = form["147245"]
        values["149514"] = form["147248"]
        values["149515"] = form["147243"]

        // Create lead object
        def lead = leadService.newInstance( user,
                                            form["147245"],
                                            new LatLng(form.latitude, form.longitude),
                                            149506,
                                            values)

        // Save lead
        lead.save(flush: true, failOnError: true)
    }
}
