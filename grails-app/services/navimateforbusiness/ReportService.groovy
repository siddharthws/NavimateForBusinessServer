package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONArray

@Transactional
class ReportService {
    static String FORMAT_TIME = "yyyy-MM-dd HH:mm:ss"
    static TimeZone IST = TimeZone.getTimeZone('Asia/Calcutta')

    // Class to represent an element of the report
    class ReportElement {
        User manager
        User rep
        Lead lead
        Template template
        Form form
        Date date
    }

    // API to get report
    def getReport(User manager) {
        def reportElements = []

        // Iterate though each rep of this manager
        List<User> team = User.findAllByManager(manager)
        team.each {rep ->
            // Get all tasks for this rep
            List<Task> tasks = Task.findAllByRepAndManager(rep, manager)
            if (!tasks) {
                // Push empty report element for this rep
                reportElements.push(new ReportElement(
                        manager: manager,
                        rep: rep,
                        date: rep.dateCreated
                ))
            } else tasks.each {task ->
                // Get list of forms submitted for this task
                List<Form> forms = Form.findAllByOwnerAndTask(rep, task)
                if (!forms) {
                    // Push report element for this task without any data
                    reportElements.push(new ReportElement(
                            manager: manager,
                            rep: rep,
                            lead: task.lead,
                            template: task.formTemplate,
                            date: task.dateCreated
                    ))
                } else forms.each {form ->
                    // Push report element for every form in this task
                    reportElements.push(new ReportElement(
                            manager: manager,
                            rep: rep,
                            lead: task.lead,
                            template: task.formTemplate,
                            form: form,
                            date: form.dateCreated
                    ))
                }
            }
        }

        // Sort elements as per date
        reportElements.sort{it.date}
        reportElements.reverse(true)

        // Get columns and values from report elements
        def columns = getColumnsFromElements(reportElements)
        def values = getValuesFromElements(reportElements, columns)

        // Parse columns and values into report
        def report = [
                columns: columns,
                values: values
        ]
        report
    }

    def getColumnsFromElements(List<ReportElement> elements){
        def columns = []

        // Add mandatory columns
        columns.push([title: 'Manager',
                      type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT,
                      filterType: navimateforbusiness.Constants.Filter.TYPE_SELECTION])
        columns.push([title: 'Representative',
                      type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT,
                      filterType: navimateforbusiness.Constants.Filter.TYPE_SELECTION])
        columns.push([title: 'Lead',
                      type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT,
                      filterType: navimateforbusiness.Constants.Filter.TYPE_SELECTION])
        columns.push([title: 'Form Template',
                      type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT,
                      filterType: navimateforbusiness.Constants.Filter.TYPE_SELECTION])
        columns.push([title: 'Date',
                      type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT,
                      filterType: navimateforbusiness.Constants.Filter.TYPE_DATE])

        // Add columns from elements
        elements.each {element ->
            if (element.form?.submittedData) {
                // Sort values by id
                def values = element.form.submittedData.values.sort(false) {it.id}

                // Iterate through values in a dtaa object
                values.each {value ->
                    // Get column title
                    String columnTitle = value.field.title

                    // Get filter type
                    int filterType = getFilterForField(value.field.type)

                    // Check if a column exists with same params
                    boolean bColumnExists = false
                    columns.each { column ->
                        if ((column.title == columnTitle) && (column.type == value.field.type)) {
                            bColumnExists = true
                        }
                    }

                    // Add new column if column does not exists
                    if (!bColumnExists) {
                        columns.push([title: columnTitle,
                                      type: value.field.type,
                                      filterType: filterType])
                    }
                }
            }
        }

        columns
    }

    def getValuesFromElements(List<ReportElement> elements, def columns){
        def values = []

        elements.each {element ->
            // Create a row
            def row = new ArrayList<String>(Collections.nCopies(columns.size(), "-"))

            // Feed manager, rep, lead details
            row[0] = element.manager.name
            if (element.rep) {
                row[1] = element.rep.name
            }
            if (element.lead) {
                row[2] = element.lead.title
            }

            // Feed template
            if (element.template) {
                row[3] = element.template.name
            }

            // Feed date
            row[4] = element.date.format(FORMAT_TIME, IST)

            // Feed form data
            if (element.form?.submittedData) {
                // Iterate through each value in this dataset
                element.form.submittedData.values.each {value ->
                    // Get column title
                    String columnTitle = value.field.title

                    // Find column index for this value
                    int columnIdx
                    for (columnIdx = 0; columnIdx < columns.size(); columnIdx++) {
                        def column = columns[columnIdx]
                        if ((column.title == columnTitle) && (column.type == value.field.type)) {
                            break
                        }
                    }

                    if (columnIdx < columns.size()) {
                        // Perform value parsing as per field types
                        if ((value.field.type == navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKLIST) && (value.value != '-')) {
                            def valueJson = JSON.parse(value.value)
                            row[columnIdx] = ''
                            valueJson.eachWithIndex {option, i ->
                                if (option.selection) {
                                    if (row[columnIdx].length() == 0) {
                                        row[columnIdx] = option.name
                                    } else {
                                        row[columnIdx] += ', ' + option.name
                                    }
                                }
                            }
                        } else if ((value.field.type == navimateforbusiness.Constants.Template.FIELD_TYPE_RADIOLIST) && (value.value != '-')) {
                            def valueJson = JSON.parse(value.value)
                            def selection = valueJson.options.getString(valueJson.selection)
                            row[columnIdx] = selection
                        } else if (value.value.length()) {
                            row[columnIdx] = value.value
                        }
                    } else {
                        log.error("Could not find column to feed the value in " + value.value)
                    }
                }
            }

            // Push row to values
            values.push(row)
        }

        values
    }

    def getFilterForField(int fieldType) {
        switch (fieldType) {
            case navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT :
            case navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKLIST :
                return navimateforbusiness.Constants.Filter.TYPE_TEXT
            case navimateforbusiness.Constants.Template.FIELD_TYPE_NUMBER :
                return navimateforbusiness.Constants.Filter.TYPE_NUMBER
            case navimateforbusiness.Constants.Template.FIELD_TYPE_RADIOLIST :
                return navimateforbusiness.Constants.Filter.TYPE_SELECTION
        }

        return navimateforbusiness.Constants.Filter.TYPE_NONE
    }
}
