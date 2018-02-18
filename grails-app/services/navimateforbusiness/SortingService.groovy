package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class SortingService {
    // ----------------------- Dependencies ---------------------------//
    // ----------------------- Public APIs ---------------------------//
    def sortRows (rows, sortList) {
        def sortedRows = rows

        // Create reverse order array for applying sorting
        def revSortList = sortList.reverse()

        // Iterate through each sorting object
        revSortList.each {sortObj ->
            // Get column index
            def colId = sortObj.colId

            // Check type of sorting required
            switch (sortObj.type) {
                case navimateforbusiness.Constants.Filter.SORT_ASC:
                    // Sort rows in ascending order
                    sortedRows = sortedRows.sort {row -> row.values[colId] ? row.values[colId].toLowerCase() : ''}
                    break
                case navimateforbusiness.Constants.Filter.SORT_DESC:
                    // Sort rows in ascending order
                    sortedRows = sortedRows.sort {row -> row.values[colId] ? row.values[colId].toLowerCase() : ''}

                    // Reverse list
                    sortedRows = sortedRows.reverse()
                    break
            }

        }

        sortedRows
    }

    // ----------------------- Private APIs ---------------------------//
}
