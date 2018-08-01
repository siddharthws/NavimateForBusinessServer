package navimateforbusiness

import grails.gorm.transactions.Transactional
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants
import org.apache.poi.hssf.usermodel.HSSFDateUtil
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile

@Transactional
class ExcelService {
    /*------------------------ Dependencies --------------------*/
    def leadService
    def taskService
    def productService

    /*------------------------ Public methods --------------------*/
    def importFile(User user, int type, MultipartFile file) {
        // Parse data from excel file to JSON Array
        def json = fileToJson(file)

        // Parse each JSON to object of respective type
        def objects = json.collect { parseJsonToObject(user, type, it) }

        // Save all objects
        objects.each {it.save(flush: true, failOnError: true)}
    }

    /*------------------------ Private methods --------------------*/
    // Method to convert file to JSON Array
    private def fileToJson(MultipartFile file) {
        // Validate File
        if (file.isEmpty()) {
            throw new ApiException("Empty File uploaded", Constants.HttpCodes.BAD_REQUEST)
        }

        // Convert file to workbook
        def wb
        try {
            wb = new XSSFWorkbook(file.getInputStream())
        } catch (Exception e) {
            throw new ApiException("Invalid File uploaded", Constants.HttpCodes.BAD_REQUEST)
        }

        // Get all column headers
        def sheet = wb.getSheetAt(0)
        def headerRow = sheet.getRow(0)
        def columns = headerRow.cellIterator().collect {[cell: new CellReference(it).formatAsString(),
                                                         value: getCellText(it)]}

        // Check for empty column names
        def emptyCol = columns.find { !it || !it.value.length() }
        if (emptyCol) { throw new ApiException("Cell " + emptyCol.cell + " : Column name cannot be empty") }

        // Check for duplicate in header
        def uniqueCols = columns.unique { it.value }
        if (uniqueCols.size() != columns.size()) { throw new ApiException("Duplicate column names are not allowed. Check first row of file.") }

        // Convert Data rows to JSON Objects
        def dataRows = sheet.rowIterator().withIndex().findAll {it, i -> i > 0}
        def json = dataRows.collect { rowToJson(columns, it) }
        json = json.findAll {it}

        json
    }

    // Method to convert a row to JSON
    private def rowToJson(def columns, XSSFRow row) {
        def json = [:]
        boolean bValid = false

        // Iterate through each cell in row
        row.cellIterator().withIndex().each { XSSFCell cell, int i ->
            // Get column for this cell. Ignore if column entry does not exist
            def column = columns[i]
            if (!column) { return }

            // Get string value from the cell
            String value = ""
            if (cell) {
                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_NUMERIC:
                        value = getCellNumeric(cell)
                        break
                    case Cell.CELL_TYPE_FORMULA:
                        value = getCellFormula(cell)
                        break
                    case Cell.CELL_TYPE_STRING:
                    default:
                        value = getCellText(cell)
                        break
                }
            }

            // Remove leading / trailing blank spaces
            value = value.trim()

            // Set as valid for first non null value
            if (value) {
                bValid = true
            }

            // Add JSON Entry
            json[column.value] = [  cell: new CellReference(cell).formatAsString(),
                                    value: value]
        }

        bValid ? json : null
    }

    // Method to convert row JSON to Object
    private def parseJsonToObject(User user, int type, def json) {
        def object

        switch (type) {
            case Constants.Template.TYPE_LEAD:
                object = leadService.fromExcelJson(user, json)
                break
            case Constants.Template.TYPE_TASK:
                object = taskService.fromExcelJson(user, json)
                break
            case Constants.Template.TYPE_PRODUCT:
                object = productService.fromExcelJson(user, json)
                break
            default:
                throw new ApiException("Cannot parse object of unknown type")
        }

        object
    }

    // Methods to get String value based on Cell Type
    private def getCellFormula(XSSFCell cell) {
        switch(cell.getCachedFormulaResultType()) {
            case Cell.CELL_TYPE_NUMERIC:
                return getCellNumeric(cell)
            case Cell.CELL_TYPE_STRING:
                return cell.getRichStringCellValue().toString()
        }

        return getCellText(cell)
    }

    private def getCellNumeric(XSSFCell cell) {
        if (HSSFDateUtil.isCellDateFormatted(cell)) {
            return getCellDate(cell)
        } else {
            return String.valueOf(cell.getNumericCellValue())
        }

    }

    private def getCellText(XSSFCell cell) {
        Constants.Formatters.DATA.formatCellValue(cell)
    }

    private def getCellDate(XSSFCell cell) {
        Constants.Formatters.LONG.format(cell.getDateCellValue())
    }
}
