package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONArray

@Transactional
class ReportService {
    static String FORMAT_DATE = "yyyy-MM-dd"
    static String FORMAT_TIME = "HH:mm:ss"
    static TimeZone IST = TimeZone.getTimeZone('Asia/Calcutta')

    // API to get report
    def getReport(User user) {
        List<User> team

        if (user.role==navimateforbusiness.Role.ADMIN){
          // Iterate though each rep of this company
           team = User.findAllByAccountAndRole(user.account, navimateforbusiness.Role.REP)
        }
        else {
            // Iterate though each rep of this manager
            team = User.findAllByManager(user)
        }

        List<Form> forms = []
        team.each {rep ->
            // Get all forms submitted by this rep
            forms.addAll(Form.findAllByAccountAndOwner(rep.account, rep))
        }

        // Remove forms with removed templates
        forms = forms.findAll {it -> it.submittedData.template && !it.submittedData.template.isRemoved}

        // Sort elements in descending order of date
        forms.sort{it.dateCreated}
        forms.reverse(true)

        // Get columns and values from report elements
        def columns = getColumnsFromElements(forms)
        def values = getValuesFromElements(forms, columns)

        // Parse columns and values into report
        def report = [
                columns: columns,
                values: values
        ]
        report
    }

    def getColumnsFromElements(List<Form> forms){
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
        columns.push([title: 'Date',
                      type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT,
                      filterType: navimateforbusiness.Constants.Filter.TYPE_DATE])
        columns.push([title: 'Time',
                      type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT,
                      filterType: navimateforbusiness.Constants.Filter.TYPE_NONE])
        columns.push([title: 'Task ID',
                      type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT,
                      filterType: navimateforbusiness.Constants.Filter.TYPE_TEXT])
        columns.push([title: 'Task Status',
                      type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT,
                      filterType: navimateforbusiness.Constants.Filter.TYPE_SELECTION])
        columns.push([title: 'Form Template',
                      type: navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT,
                      filterType: navimateforbusiness.Constants.Filter.TYPE_SELECTION])
        columns.push([title: 'Location',
                      type: navimateforbusiness.Constants.Template.FIELD_TYPE_LOCATION,
                      filterType: navimateforbusiness.Constants.Filter.TYPE_NONE])

        // Add columns from forms
        forms.each {form ->
            // Sort values by id
            def values = form.submittedData.values.sort(false) {it.id}

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

        columns
    }

    def getValuesFromElements(List<Form> forms, def columns){
        def values = []

        forms.each {form ->
            // Create a row
            def row = new ArrayList<String>(Collections.nCopies(columns.size(), "-"))

            // Add manager
            row[0] = form.task ? form.task.manager.name : form.owner.manager.name

            // Add Rep
            row[1] = form.owner.name

            // Add lead
            row[2] = form.task ? form.task.lead.title : "-"

            // Add date
            row[3] = form.dateCreated.format(FORMAT_DATE, IST)

            // Add date
            row[4] = form.dateCreated.format(FORMAT_TIME, IST)

            // Add Task ID
            row[5] = form.task ? "T" + String.format("%08d", form.task.id) : "-"

            // Add status
            row[6] = form.taskStatus ? form.taskStatus.name() : "-"

            // Add form template name
            row[7] = form.submittedData.template ? form.submittedData.template.name : "-"

            // Feed location if valid
            row[8] = (form.latitude || form.longitude) ? form.latitude + ',' + form.longitude : "-"

            // Iterate through each value in this dataset
            form.submittedData.values.each {value ->
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
                    } else if (value.field.type == navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKBOX) {
                        row[columnIdx] = Boolean.valueOf(value.value)
                    } else if (value.value.length()) {
                        row[columnIdx] = value.value
                    }
                } else {
                    log.error("Could not find column to feed the value in " + value.value)
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
            case navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKBOX :
                return navimateforbusiness.Constants.Filter.TYPE_SELECTION
        }

        return navimateforbusiness.Constants.Filter.TYPE_NONE
    }

    def getLocationReport(User rep) {
        def report = []

        // Get all report data submitted by this rep
        def locReport = LocationReport.findByAccountAndOwner(rep.account, rep)

        // Iterate through each report item
        locReport.each {row ->
            // Prepare JSON for row
            def rowJson = [
                    timestamp: row.timestamp,
                    latitude: row.latitude,
                    longitude: row.longitude,
                    status: row.status
            ]

            // Add to report
            report.push(rowJson)
        }

        // Send report
        report
    }

    def saveLocationReport(User rep, def reportJson) {
        // Iterate through report
        reportJson.each {rowJson ->
            // Create report object
            LocationReport locReport = new LocationReport(
                    account: rep.account,
                    owner: rep,
                    latitude: rowJson.latitude ? rowJson.latitude : 0,
                    longitude: rowJson.longitude ? rowJson.longitude : 0,
                    timestamp: rowJson.timestamp,
                    status: rowJson.status
            )

            // Save to database
            locReport.save(flush: true, failOnError: true)
        }
    }
}
