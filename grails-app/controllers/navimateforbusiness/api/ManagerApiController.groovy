package navimateforbusiness.api

import grails.converters.JSON
import grails.core.GrailsApplication
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.Template

// APIs exposed to users with manager access or higher
class ManagerApiController {
    // ----------------------- Dependencies ---------------------------//
    def authService
    def userService
    def leadService
    def taskService
    def formService
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
    def getLeadTable() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter

        // get leads for this user
        def leads = leadService.getForUser(user)

        // Convert leads to tabular format
        def table = tableService.parseLeads(user, leads)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)
        int totalRows = table.rows.size()

        // Apply sorting to table
        table.rows = sortingService.sortRows(table.rows, filter.sortList)

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

    def searchLeads() {
        // Get user object
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
        leads.each {it -> resp.items.push([id: it.id, title: it.title])}

        // Send response
        render resp as JSON
    }

    def getLeadIds() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter

        // get leads for this user
        def leads = leadService.getForUser(user)

        // Convert leads to tabular format
        def table = tableService.parseLeads(user, leads)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)

        // Prepare response as list of IDs
        def respIds = []
        table.rows.each {row ->
            respIds.push(row.id)
        }
        // Send response
        def resp = [
                ids: respIds
        ]
        render resp as JSON
    }

    def exportLeads() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter
        def exportParams = request.JSON.exportParams

        // get leads for this user
        def leads = leadService.getForUser(user)

        // Convert leads to tabular format
        def table = tableService.parseLeads(user, leads)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)

        // Apply sorting to table
        table.rows = sortingService.sortRows(table.rows, filter.sortList)

        // Export table
        def exportData = tableService.getExportData(table, exportParams)

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
        leadsJson.each {leadJson -> leads.push(leadService.fromJson(user, leadJson))}

        // Save each lead
        leads.each {lead -> lead.save(flush: true, failOnError: true)}

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
        selectedTasks.each {task -> resp.push(taskService.toJson(task))}

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
        table.rows = sortingService.sortRows(table.rows, filter.sortList)

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

        // Prepare response as list of IDs
        def respIds = []
        table.rows.each {row ->
            respIds.push(row.id)
        }
        // Send response
        def resp = [
                ids: respIds
        ]
        render resp as JSON
    }

    def editTasks() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Parse task JSON to task objects
        def tasks = []
        request.JSON.tasks.each {taskJson ->
            tasks.push(taskService.fromJson(taskJson, user))
        }

        // Save tasks
        tasks.each {it -> it.save(flush: true, failOnError: true)}

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
        table.rows = sortingService.sortRows(table.rows, filter.sortList)

        // Export table
        def exportData = tableService.getExportData(table, exportParams)

        // Set response parameters
        response.setHeader("Content-disposition", "attachment; filename=exportfile.xls")
        response.contentType = grailsApplication.config.getProperty("grails.mime.types.excel")

        // Export data
        exportService.export('excel', response.outputStream, exportData.objects, exportData.fields, exportData.labels, [:], [:])
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
        table.rows = sortingService.sortRows(table.rows, filter.sortList)

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

        // Prepare response as list of IDs
        def respIds = []
        table.rows.each {row ->
            respIds.push(row.id)
        }
        // Send response
        def resp = [
                ids: respIds
        ]
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
        table.rows = sortingService.sortRows(table.rows, filter.sortList)

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

    // ----------------------- Private methods ----------------------- //
}
