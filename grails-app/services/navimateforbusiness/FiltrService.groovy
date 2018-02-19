package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class FiltrService {
    // ----------------------- Dependencies ---------------------------//
    // ----------------------- Public APIs ---------------------------//
    // Method to apply filters to table rows
    def applyToTable(def rows, def colFilters) {
        def filteredRows = []

        // Iterate through each row
        rows.each { row ->
            def bFilter = true

            // Iterate through column filters
            for (int i = 0; i < colFilters.size(); i++) {
                def filter = colFilters[i]
                def value = row.values[filter.colId]

                // Check if value is filtered
                bFilter = isValueFiltered(value, filter)
                if (!bFilter) {
                    break
                }
            }

            // Add row if successfully filtered
            if (bFilter) {
                filteredRows.push(row)
            }
        }

        filteredRows
    }

    // ----------------------- Private APIs ---------------------------//
    // Method to check if given value passes the filter or not
    private def isValueFiltered(def value, def filter) {
        def bFiltered = true

        // Apply Blank filter
        if (filter.bNoBlanks && value == '-') {
            bFiltered = false
        }

        return bFiltered
    }
}
