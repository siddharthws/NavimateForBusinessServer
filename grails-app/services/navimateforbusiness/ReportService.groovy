package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONArray

@Transactional
class ReportService {
    static String FORMAT_TIME = "yyyy-MM-dd HH:mm:ss"

    def getTeamReport(User manager) {
        // Prepare list of columns
        def columns = [
                [type: "selection", title: "Manager"],
                [type: "selection", title: "Rep"],
                [type: "selection", title: "Lead"],
                [type: "selection", title: "Template"]
        ]
        def formColumns = getFormColumns(manager)
        columns += formColumns
        columns.push([type: "date", title: "Date"])

        // Iterate though each rep to get list of values
        def values = []
        List<User> team = User.findAllByManager(manager)
        team.each {rep ->
            // Get all tasks & forms for this rep
            List<Task> tasks = Task.findAllByRep(rep)

            if (!tasks) {
                // If no tasks, create empty entry
                def row = new ArrayList<String>(Collections.nCopies(columns.size(), "-"))
                row[0] = manager.name
                row[1] = rep.name
                row[row.size() - 1] = rep.lastUpdated.format(FORMAT_TIME)
                values.push(row)
            } else tasks.each {task ->
                List<Form> forms = Form.findAllByTask(task)
                if (!forms) {
                    // Create single entry for each task
                    def row = new ArrayList<String>(Collections.nCopies(columns.size(), "-"))
                    row[0] = manager.name
                    row[1] = rep.name
                    row[2] = task.lead.title
                    row[3] = task.template.name
                    row[row.size() - 1] = task.lastUpdated.format(FORMAT_TIME)
                    values.push(row)
                } else forms.each {form ->
                    def formRow = getFormRow(form, formColumns)
                    def row = [
                            manager.name,
                            rep.name,
                            task.lead.title,
                            form.name
                    ]
                    row += formRow
                    row.push(form.lastUpdated.format(FORMAT_TIME))
                    values.push(row)
                }
            }
        }

        // Parse columns and values into report
        def report = [
                columns: columns,
                values: values
        ]
        report
    }

    def getLeadReport(User manager) {
        // Prepare list of columns
        def columns = [
                [type: "selection", title: "Manager"],
                [type: "selection", title: "Lead"],
                [type: "selection", title: "Rep"],
                [type: "selection", title: "Template"]
        ]
        def formColumns = getFormColumns(manager)
        columns += formColumns
        columns.push([type: "date", title: "Date"])

        // Iterate though each rep to get list of values
        def values = []
        List<Lead> leads = Lead.findAllByManager(manager)
        leads.each {lead ->
            // Get all tasks & forms for this rep
            List<Task> tasks = Task.findAllByLead(lead)

            if (!tasks) {
                // If no tasks, create empty entry
                def row = new ArrayList<String>(Collections.nCopies(columns.size(), "-"))
                row[0] = manager.name
                row[1] = lead.title
                row[row.size() - 1] = lead.lastUpdated.format(FORMAT_TIME)
                values.push(row)
            } else tasks.each {task ->
                List<Form> forms = Form.findAllByTask(task)
                if (!forms) {
                    // Create single entry for each task
                    def row = new ArrayList<String>(Collections.nCopies(columns.size(), "-"))
                    row[0] = manager.name
                    row[1] = lead.title
                    row[2] = task.rep.name
                    row[3] = task.template.name
                    row[row.size() - 1] = task.lastUpdated.format(FORMAT_TIME)
                    values.push(row)
                } else forms.each {form ->
                    def formRow = getFormRow(form, formColumns)
                    def row = [
                            manager.name,
                            lead.title,
                            task.rep.name,
                            form.name
                    ]
                    row += formRow
                    row.push(form.lastUpdated.format(FORMAT_TIME))
                    values.push(row)
                }
            }
        }

        // Parse columns and values into report
        def report = [
                columns: columns,
                values: values
        ]
        report
    }

    def getFormColumns(User manager) {
        def columns = []

        // Iterate through all templates
        manager.forms.each { template ->
            JSONArray fields = JSON.parse(template.data)
            // Iterate through all fields in template
            fields.each {field ->
                // Check if entry is already present in columns
                boolean bDuplicate = false
                columns.each {column ->
                    if ((column.title == field.title) &&
                        (column.type == field.type)) {
                            bDuplicate = true
                        }
                }

                // Add to columns
                if (!bDuplicate) {
                    columns.push([
                            title: field.title,
                            type:  field.type
                    ])
                }
            }
        }

        columns
    }

    def getFormRow(Form form, def columns) {
        def row = new ArrayList<String>(Collections.nCopies(columns.size(), "-"))

        // Iterate through form fields
        JSONArray fields = JSON.parse(form.data)
        fields.each {field ->
            // Add entry to appropriate index in row
            columns.eachWithIndex {column, index ->
                if ((column.title == field.title) &&
                    (column.type == field.type)) {
                    if (field.type == "radioList") {
                        // Value.Seleciton for radio list
                        row[index] = field.value.selection
                    } else if (field.type == 'checkList') {
                        // Reset row data
                        row[index] = ""

                        // Create array of selected values
                        def selection = []
                        field.value.selection.eachWithIndex {bChecked, optionIdx ->
                            if (bChecked) {
                                selection.push(field.value.options[optionIdx])
                            }
                        }

                        // Push array into row
                        selection.eachWithIndex{ option, i ->
                            row[index] += option
                            if (i < (selection.size() - 1)) {
                                row[index] += ', '
                            }
                        }
                    } else {
                        // Value for everything else
                        if (field.value) {
                            row[index] = field.value
                        }
                    }
                }
            }
        }

        row
    }
}
