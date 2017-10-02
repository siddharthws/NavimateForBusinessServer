package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONArray

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
                    JSONArray data = JSON.parse(form.data)

                    def row = [ rep: task.rep.name,
                                lead: task.lead.title,
                                date:  form.dateCreated.format("yyyy-MM-dd"),
                                sales: data.get(0).value,
                                notes: data.get(1).value,
                                status: data.get(2).value.selection]

                    report.push(row)
                }

                // If no forms in this task, add single entry with empty form
                if (!task?.forms) {
                    def row = [ rep: task.rep.name,
                                lead: task.lead.title,
                                sales: 0,
                                status: "NA",
                                notes: "NA",
                                date:  task.dateCreated.format("yyyy-MM-dd")]

                    report.push(row)
                }
            }

            // If no tasks for this rep, add single entry with empty data
            if (!tasks) {
                def row = [ rep: member.name,
                            lead: "NA",
                            sales: 0,
                            status: "NA",
                            notes: "NA",
                            date:  member.lastUpdated.format("yyyy-MM-dd")]

                report.push(row)
            }
        }

        report
    }

    def getLeadReport(User manager) {
        def report = []

        // Get leads
        List<Lead> leads = Lead.findAllByManager(manager)
        leads.each {lead ->
            // Get all tasks for this lead
            List<Task> tasks = Task.findAllByLead(lead)
            tasks.each {task ->
                // Add an entry in report for each form in this task
                task.forms.each {form ->
                    JSONArray data = JSON.parse(form.data)

                    def row = [ lead: task.lead.title,
                                rep: task.rep.name,
                                date:  form.dateCreated.format("yyyy-MM-dd"),
                                sales: data.get(0).value,
                                notes: data.get(1).value,
                                status: data.get(2).value.selection]

                    report.push(row)
                }

                // If no forms in this task, add single entry with empty form
                if (!task?.forms) {
                    def row = [ lead: task.lead.title,
                                rep: task.rep.name,
                                sales: 0,
                                status: "NA",
                                notes: "NA",
                                date:  task.dateCreated.format("yyyy-MM-dd")]

                    report.push(row)
                }
            }

            // If no tasks for this rep, add single entry with empty data
            if (!tasks) {
                def row = [ lead: lead.title,
                            rep: "NA",
                            sales: 0,
                            status: "NA",
                            notes: "NA",
                            date:  lead.lastUpdated.format("yyyy-MM-dd")]

                report.push(row)
            }
        }

        report
    }
}
