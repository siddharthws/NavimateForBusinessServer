package navimateforbusiness.api

import grails.converters.JSON
import grails.core.GrailsApplication

// APIs exposed to users with manager access or higher
class ManagerApiController {
    // ----------------------- Dependencies ---------------------------//
    def authService
    def leadService
    def tableService
    def filtrService
    def sortingService
    def pagingService

    // ----------------------- APIs ----------------------- //
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

    // ----------------------- Private methods ----------------------- //
}
