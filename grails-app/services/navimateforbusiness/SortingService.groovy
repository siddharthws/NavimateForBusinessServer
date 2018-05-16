package navimateforbusiness

import grails.gorm.transactions.Transactional

import java.text.SimpleDateFormat

@Transactional
class SortingService {
    // ----------------------- Dependencies ---------------------------//
    // ----------------------- Public APIs ---------------------------//
    def sortRows (columns, rows, sortList) {
        def sortedRows = rows

        // Create reverse order array for applying sorting
        def revSortList = sortList.reverse()

        // Iterate through each sorting object
        revSortList.each {sortObj ->
            // Get column index
            def colId = sortObj.colId

            // Get column type
            def type = columns.find{it -> it.id == colId}.type

            // Check type of sorting required
            switch (sortObj.type) {
                case navimateforbusiness.Constants.Filter.SORT_ASC:
                    // Sort rows in ascending order
                    sortedRows = sortedRows.sort {row -> getSortableValue(type, row.values[colId])}
                    break
                case navimateforbusiness.Constants.Filter.SORT_DESC:
                    // Sort rows in ascending order
                    sortedRows = sortedRows.sort {row -> getSortableValue(type, row.values[colId])}

                    // Reverse list
                    sortedRows = sortedRows.reverse()
                    break
            }

        }

        sortedRows
    }

    // ----------------------- Private APIs ---------------------------//
    def getSortableValue (int colType, def value) {
        def sortableValue

        if ( colType == navimateforbusiness.Constants.Template.FIELD_TYPE_LEAD ||
                    colType == navimateforbusiness.Constants.Template.FIELD_TYPE_TASK) {
            sortableValue = (value != '-') ? value.name : ''
        } else {
            sortableValue = (value != '-') ? value.toLowerCase() : ''
        }

        sortableValue
    }
}
