package navimateforbusiness.api

import grails.converters.JSON
import grails.core.GrailsApplication
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.LeadM
import navimateforbusiness.Template
import navimateforbusiness.Visibility

// APIs exposed to users with manager access or higher
class ManagerApiController {
    // ----------------------- Dependencies ---------------------------//
    def authService
    def userService
    def leadService
    def taskService
    def formService
    def fcmService
    def templateService
    def tableService
    def filtrService
    def sortingService
    def pagingService
    def searchService
    def exportService
    def importService

    GrailsApplication grailsApplication

    // ----------------------- Team APIs ----------------------- //
    def getTeamById () {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // get team members for this user
        def team = userService.getRepsForUser(user)

        // Find users with given IDs
        def selectedTeam = []
        request.JSON.ids.each {id -> selectedTeam.push(team.find {it -> it.id == id}) }

        // Throw exception if all users not found
        if (selectedTeam.size() != request.JSON.ids.size()) {
            throw new ApiException("Invalid team IDs requested", Constants.HttpCodes.BAD_REQUEST)
        }

        // Prepare JSON response
        def resp = []
        selectedTeam.each {rep -> resp.push(userService.toJson(rep))}

        render resp as JSON
    }

    def searchTeam() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get input params
        def text = request.JSON.text
        def pager = request.JSON.pager

        // get reps for this user
        def reps = userService.getRepsForUser(user)

        // get search results
        reps = searchService.searchUsers(reps, text)
        int totalCount = reps.size()

        // Get pages results
        reps = pagingService.apply(reps, pager)

        // Create response array with IDs and names
        def resp = [
                items: [],
                totalCount: totalCount
        ]
        reps.each {it -> resp.items.push([id: it.id, title: it.name])}

        // Send response
        render resp as JSON
    }

    // ----------------------- LEAD APIs ----------------------- //
    def getLeadsById () {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Find leads with given IDs
        def leads = leadService.getForUserByFilter(user, [_ids: request.JSON.ids], [:], []).leads

        // Throw exception if all tasks not found
        if (leads.size() != request.JSON.ids.size()) {
            throw new ApiException("Invalid lead IDs requested", Constants.HttpCodes.BAD_REQUEST)
        }

        // Prepare JSON response
        def resp = []
        leads.each {lead -> resp.push(leadService.toJson(lead, user))}

        render resp as JSON
    }

    def getLeadTable() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter
        def pager = request.JSON.pager
        def sorter = request.JSON.sorter

        // get leads for this user
        def filteredLeads = leadService.getForUserByFilter(user, filter, pager, sorter)

        // Send JSON Response
        def resp = [
                rowCount: filteredLeads.rowCount,
                leads: []
        ]
        filteredLeads.leads.each {LeadM lead -> resp.leads.push(leadService.toJson(lead, user))}
        render resp as JSON
    }

    def searchLeads() {
        /*// Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get input params
        def text = request.JSON.text
        def pager = request.JSON.pager

        // get leads for this user
        def leads = leadService.getForUser(user)

        // get search results
        leads = searchService.searchLeads(leads, text)
        int totalCount = leads.size()

        // Get pages results
        leads = pagingService.apply(leads, pager)

        // Create response array with IDs and names
        def resp = [
                items: [],
                totalCount: totalCount
        ]
        leads.each {it -> resp.items.push([id: it.id, name: it.name])}

        // Send response
        render resp as JSON*/
        throw new ApiException("API unavailable")
    }

    def getLeadIds() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter
        def sorter = request.JSON.sorter

        // get leads for this user
        def filteredLeads = leadService.getForUserByFilter(user, filter, [:], sorter)

        // Ensure number of rows are less than max limit
        if (filteredLeads.leads.size() > Constants.Table.MAX_SELECTION_COUNT) {
            throw new ApiException("Too many rows. Maximum " + Constants.Table.MAX_SELECTION_COUNT + " rows can be selected at once.")
        }

        // Prepare response as list of IDs & names
        def resp = []
        filteredLeads.leads.each {LeadM lead ->
            resp.push(id: lead.id, name: lead.name)
        }
        // Send response
        render resp as JSON
    }

    def exportLeads() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter
        def sorter = request.JSON.sorter
        def exportParams = request.JSON.exportParams

        // get leads for this user
        def leads = leadService.getForUserByFilter(user, filter, [:], sorter).leads

        // Extract selected leads if applicable
        if (params.selection) {
            // Find all leads with IDs contained in selection array
            leads = leads.findAll {it -> params.selection.contains(it.id)}
        }

        // Export table
        def exportData = leadService.getExportData(user, leads, exportParams)

        // Set response parameters
        response.setHeader("Content-disposition", "attachment; filename=exportfile.xls")
        response.contentType = grailsApplication.config.getProperty("grails.mime.types.excel")

        // Export data
        exportService.export('excel', response.outputStream, exportData.objects, exportData.fields, exportData.labels, [:], [:])
    }

    def importLeads() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get file
        def file = request.getFile('importFile')

        // Get table from file
        def table = tableService.parseExcel(file)

        // Validate all columns
        importService.checkLeadColumns(table.columns)

        // Ensure all IDs are unique
        importService.checkIds(table.columns, table.rows)

        // Get lead JSON for each row
        def leadsJson = []
        table.rows.eachWithIndex {row, i -> leadsJson.push(importService.parseLeadRow(table.columns, row, i, user))}

        // Parse each JSON object into Lead object
        def leads = []
        leadsJson.each {leadJson ->
            // Parse to lead object and assign update & create time
            LeadM lead = leadService.fromJson(leadJson, user)

            leads.push(lead)
        }

        // Save each lead
        leads.each {lead ->
            // Make lead public
            lead.visibility = Visibility.PUBLIC

            // Save lead
            lead.save(flush: true, failOnError: true)
        }

        def resp = [success: true]
        render resp as JSON
    }

    // ----------------------- TASK APIs ----------------------- //
    def getTasksById () {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // get tasks for this user
        def tasks = taskService.getForUser(user)

        // Find tasks with given IDs
        def selectedTasks = []
        request.JSON.ids.each {id -> selectedTasks.push(tasks.find {it -> it.id == id}) }

        // Throw exception if all tasks not found
        if (selectedTasks.size() != request.JSON.ids.size()) {
            throw new ApiException("Invalid task IDs requested", Constants.HttpCodes.BAD_REQUEST)
        }

        // Prepare JSON response
        def resp = []
        selectedTasks.each {task -> resp.push(taskService.toJson(task, user))}

        render resp as JSON
    }

    def getTaskTable() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter

        // get tasks for this user
        def tasks = taskService.getForUser(user)

        // Convert tasks to tabular format
        def table = tableService.parseTasks(user, tasks)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)
        int totalRows = table.rows.size()

        // Apply sorting to table
        table.rows = sortingService.sortRows(table.columns, table.rows, filter.sortList)

        // Apply paging to table
        table.rows = pagingService.apply(table.rows, filter.pager)

        // Send response
        def resp = [
                rows: table.rows,
                columns: request.JSON.bColumns ? table.columns : null,
                totalRows: totalRows
        ]
        render resp as JSON
    }

    def getTaskIds() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter

        // get tasks for this user
        def tasks = taskService.getForUser(user)

        // Convert tasks to tabular format
        def table = tableService.parseTasks(user, tasks)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)

        // Ensure number of rows are less than max limit
        if (table.rows.size() > Constants.Table.MAX_SELECTION_COUNT) {
            throw new ApiException("Too many rows. Maximum " + Constants.Table.MAX_SELECTION_COUNT + " rows can be selected at once.")
        }

        // Prepare response as list of IDs & names
        def resp = []
        table.rows.each {row ->
            resp.push(id: row.id, name: row.name)
        }
        // Send response
        render resp as JSON
    }

    def editTasks() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Parse task JSON to task objects
        def tasks = []
        def reps = []
        request.JSON.tasks.each {taskJson ->
            def task = taskService.fromJson(taskJson, user)
            tasks.push(task)

            // Push FCM
            if (task.rep) {if (!reps.contains(task.rep)) {reps.push(task.rep)}}
        }

        // Save tasks
        tasks.each {it -> it.save(flush: true, failOnError: true)}

        // Send notifications
        reps.each {it -> fcmService.notifyApp(it)}

        // Return response
        def resp = [success: true]
        render resp as JSON
    }

    def exportTasks() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter
        def exportParams = request.JSON.exportParams

        // get tasks for this user
        def tasks = taskService.getForUser(user)

        // Convert tasks to tabular format
        def table = tableService.parseTasks(user, tasks)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)

        // Apply sorting to table
        table.rows = sortingService.sortRows(table.columns, table.rows, filter.sortList)

        // Export table
        def exportData = tableService.getExportData(table, exportParams)

        // Set response parameters
        response.setHeader("Content-disposition", "attachment; filename=exportfile.xls")
        response.contentType = grailsApplication.config.getProperty("grails.mime.types.excel")

        // Export data
        exportService.export('excel', response.outputStream, exportData.objects, exportData.fields, exportData.labels, [:], [:])
    }

    def importTasks() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get file
        def file = request.getFile('importFile')

        // Get table from file
        def table = tableService.parseExcel(file)

        // Validate all columns
        importService.checkTaskColumns(table.columns)

        // Get task JSON for each row
        def tasksJson = []
        table.rows.eachWithIndex {row, i -> tasksJson.push(importService.parseTaskRow(table.columns, row, i, user))}

        // Parse each JSON object into Task object
        def tasks = []
        tasksJson.each {taskJson -> tasks.push(taskService.fromJson(taskJson, user))}

        // Save each task
        tasks.each {task -> task.save(flush: true, failOnError: true) }

        def resp = [success: true]
        render resp as JSON
    }

    // ----------------------- FORM APIs ----------------------- //
    def getFormTable() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter

        // Get forms for this user
        def forms = formService.getForUser(user)

        // Convert forms to tabular format
        def table = tableService.parseForms(user, forms)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)
        int totalRows = table.rows.size()

        // Apply sorting to table
        table.rows = sortingService.sortRows(table.columns, table.rows, filter.sortList)

        // Apply paging to table
        table.rows = pagingService.apply(table.rows, filter.pager)

        // Send response
        def resp = [
                rows: table.rows,
                columns: request.JSON.bColumns ? table.columns : null,
                totalRows: totalRows
        ]
        render resp as JSON
    }

    def getFormIds() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter

        // get forms for this user
        def forms = formService.getForUser(user)

        // Convert forms to tabular format
        def table = tableService.parseForms(user, forms)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)

        // Ensure number of rows are less than max limit
        if (table.rows.size() > Constants.Table.MAX_SELECTION_COUNT) {
            throw new ApiException("Too many rows. Maximum " + Constants.Table.MAX_SELECTION_COUNT + " rows can be selected at once.")
        }

        // Prepare response as list of IDs & names
        def resp = []
        table.rows.each {row ->
            resp.push(id: row.id, name: row.name)
        }
        // Send response
        render resp as JSON
    }

    def exportForms() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter
        def exportParams = request.JSON.exportParams

        // get forms for this user
        def forms = formService.getForUser(user)

        // Convert forms to tabular format
        def table = tableService.parseForms(user, forms)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)

        // Apply sorting to table
        table.rows = sortingService.sortRows(table.columns, table.rows, filter.sortList)

        // Export table
        def exportData = tableService.getExportData(table, exportParams)

        // Set response parameters
        response.setHeader("Content-disposition", "attachment; filename=exportfile.xls")
        response.contentType = grailsApplication.config.getProperty("grails.mime.types.excel")

        // Export data
        exportService.export('excel', response.outputStream, exportData.objects, exportData.fields, exportData.labels, [:], [:])
    }

    // ----------------------- Template APIs ----------------------- //
    def getTemplates() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get templates for this user
        List<Template> templates = templateService.getForUser(user)

        // Prepare JSON response
        def resp = []
        templates.each {template ->
            resp.push(templateService.toJson(template))
        }

        render resp as JSON
    }

    // ----------------------- Team APIs ----------------------- //
    def getTeamTable() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter

        // Get team for this user
        def team = userService.getRepsForUser(user)

        // Convert team to tabular format
        def table = tableService.parseTeam(user, team)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)
        int totalRows = table.rows.size()

        // Apply sorting to table
        table.rows = sortingService.sortRows(table.columns, table.rows, filter.sortList)

        // Apply paging to table
        table.rows = pagingService.apply(table.rows, filter.pager)

        // Send response
        def resp = [
                rows: table.rows,
                columns: request.JSON.bColumns ? table.columns : null,
                totalRows: totalRows
        ]
        render resp as JSON
    }

    /*
     * Remove reps function is used to remove representatives.
     * Admin or manager select representatives to be removed.
     * Remove reps function receives a JSON object of representatives selected by admin or their manager.
     */
    def removeReps() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get reps under this user
        def reps = userService.getRepsForUser(user)

        // Get Reps IDs to remove
        def ids = request.JSON.ids

        // Get reps to remove
        def removeReps = []
        ids.each {id -> removeReps.push(reps.find {it -> it.id == id})}

        // Remove selected reps
        removeReps.each {rep ->
            // Remove Rep's Manager & account
            rep.manager = null
            rep.account = null

            // Save rep
            rep.save(flush: true, failOnError: true)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def getRepIds() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter

        // get team for this user
        def team = userService.getRepsForUser(user)

        // Convert team to tabular format
        def table = tableService.parseTeam(user, team)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)

        // Prepare response as list of IDs & names
        def resp = []
        table.rows.each {row ->
            resp.push(id: row.id, name: row.name)
        }
        render resp as JSON
    }

    def exportTeam() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter
        def exportParams = request.JSON.exportParams

        // get team for this user
        def team = userService.getRepsForUser(user)

        // Convert team to tabular format
        def table = tableService.parseTeam(user, team)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)

        // Apply sorting to table
        table.rows = sortingService.sortRows(table.columns, table.rows, filter.sortList)

        // Export table
        def exportData = tableService.getExportData(table, exportParams)

        // Set response parameters
        response.setHeader("Content-disposition", "attachment; filename=exportfile.xls")
        response.contentType = grailsApplication.config.getProperty("grails.mime.types.xls")

        // Export data
        exportService.export('csv', response.outputStream, exportData.objects, exportData.fields, exportData.labels, [:], [:])
    }
    // ----------------------- Private methods ----------------------- //
}
