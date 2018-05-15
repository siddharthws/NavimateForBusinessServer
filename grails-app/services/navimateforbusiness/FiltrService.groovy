package navimateforbusiness

import grails.gorm.transactions.Transactional

import java.text.SimpleDateFormat

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
        } else {
            // Apply filters using filter types
            switch (filter.type) {
                case navimateforbusiness.Constants.Filter.TYPE_TEXT:
                    // Check if filter is applied
                    if (filter.value) {
                        // Check if value passes the filter
                        if (!value.toLowerCase().contains(filter.value.toLowerCase())) {
                            bFiltered = false
                        }
                    }
                    break
                case navimateforbusiness.Constants.Filter.TYPE_OBJECT:
                    // Check if filter is applied
                    if (filter.value) {
                        // Check if value passes the filter
                        if (value == '-') {
                            bFiltered = false
                        } else if (!value.name.toLowerCase().contains(filter.value.toLowerCase())) {
                            bFiltered = false
                        }
                    }
                    break
                case navimateforbusiness.Constants.Filter.TYPE_NUMBER:
                    // Check if 'from' filter is applied
                    if (filter.value.from) {
                        // Check if value passes the filter
                        if (value == '-') {
                            bFiltered = false
                        } else {
                            Double val = Double.parseDouble(String.valueOf(value))
                            if (filter.value.from > val) {
                                bFiltered = false
                            }
                        }
                    }

                    // Check if 'to' filter is applied
                    if (filter.value.to) {
                        // Check if value passes the filter
                        if (value == '-') {
                            bFiltered = false
                        } else {
                            Double val = Double.parseDouble(String.valueOf(value))
                            if (filter.value.to < val) {
                                bFiltered = false
                            }
                        }
                    }
                    break
                case navimateforbusiness.Constants.Filter.TYPE_DATE:
                    SimpleDateFormat sdf = new SimpleDateFormat(navimateforbusiness.Constants.Date.FORMAT_FRONTEND)
                    // Check if 'from' filter is applied
                    if (filter.value.from) {
                        if (value != '-') {
                            Date dateVal = sdf.parse(value)
                            Date dateFilter = sdf.parse(filter.value.from)
                            if (dateVal < dateFilter) {
                                bFiltered = false
                            }
                        } else {
                            bFiltered = false
                        }
                    }

                    // Check if 'to' filter is applied
                    if (filter.value.to) {
                        if (value != '-') {
                            Date dateVal = sdf.parse(value)
                            Date dateFilter = sdf.parse(filter.value.to)
                            if (dateVal > dateFilter) {
                                bFiltered = false
                            }
                        } else {
                            bFiltered = false
                        }
                    }
                    break
            }
        }

        return bFiltered
    }
}
