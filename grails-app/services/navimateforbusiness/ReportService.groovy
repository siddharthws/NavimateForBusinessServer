package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class ReportService {

    def getTeamReport(User manager) {
        def report = []

        // Get reps
        List<User> team = User.findAllByManager(manager)
        team.each {member ->
            // Get all tasks for this rep
            List<Task> tasks = Task.findAllByRep(member)
            tasks.each {task ->
                // Add an entry in report for each form in this task
                task.forms.each {form ->
                    def row = [ rep: task.rep.name,
                                lead: task.lead.company,
                                form: form.data]

                    report.push(row)
                }

                // If no forms in this task, add single entry with empty form
                if (task?.forms) {
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
        leads.each {lead ->
            // Get all tasks for this lead
            List<Task> tasks = Task.findAllByLead(lead)
            tasks.each {task ->
                // Add an entry in report for each form in this task
                task.forms.each {form ->
                    def row = [ rep: task.rep.name,
                                lead: task.lead.company,
                                form: form.data]

                    report.push(row)
                }

                // If no forms in this task, add single entry with empty form
                if (task?.forms) {
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
