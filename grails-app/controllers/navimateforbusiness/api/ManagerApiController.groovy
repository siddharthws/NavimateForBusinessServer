package navimateforbusiness.api

import grails.converters.JSON
import grails.core.GrailsApplication

// APIs exposed to users with manager access or higher
class ManagerApiController {
    // ----------------------- Dependencies ---------------------------//
    def authService
    def leadService
    def taskService
    def formService
    def tableService
    def filtrService
    def sortingService
    def pagingService
    def exportService
    def importService

    GrailsApplication grailsApplication

    // ----------------------- APIs ----------------------- //

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

    // ----------------------- Private methods ----------------------- //
}
