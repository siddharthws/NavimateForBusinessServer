package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class ReportService {

    def getTeamReport(User manager) {
        def report = []

        // Get reps
        List<User> team = User.findAllByManager(manager)
        for (User member : team){
            // Get all tasks for this rep
            List<Task> tasks = Task.findAllByRep(member)
            for (Task task : tasks){
                // Add an entry in report for each form in this task
                for (Form form : task.forms) {
                    def row = [ rep: task.rep.name,
                                lead: task.lead.company,
                                form: form.data]

                    report.push(row)
                }

                // If no forms in this task, add single entry with empty form
                if (task.forms.size() == 0) {
                    def row = [ rep: task.rep.name,
                                lead: task.lead.company,
                                form: ""]

                    report.push(row)
                }
            }
        }

        report
    }

    def getLeadReport(User manager) {
        def report = []

        // Get all leads for this user
        List<Lead> leads = Lead.findAllByManager(manager)
        for (Lead lead : leads){
            // Get all tasks for this lead
            List<Task> tasks = Task.findAllByLead(lead)
            for (Task task : tasks){
                // Add an entry in report for each form in this task
                for (Form form : task.forms) {
                    def row = [ rep: task.rep.name,
                                lead: task.lead.company,
                                form: form.data]

                    report.push(row)
                }

                // If no forms in this task, add single entry with empty form
                if (task.forms.size() == 0) {
                    def row = [ rep: task.rep.name,
                                lead: task.lead.company,
                                form: "No Forms Added"]

                    report.push(row)
                }
            }
        }

        report
    }
}
