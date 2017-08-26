/**
 * Created by Siddharth on 26-08-2017.
 */

app.services("ExcelService", function () {

    this.fileRead = function (workbook) {
        // Get Worksheet
        var worksheet = workbook.Sheets[workbook.SheetNames[0]]

        // Parse worksheet into JSON
        var range = XLSX.utils.decode_range(worksheet['!ref'])
        var cols = []
        for (col = range.s.c; col < range.e.c; col++){
            var rows = []
            for (row = range.s.r; row < range.e.r; row++){
                rows.push(worksheet[XLSX.utils.encode_cell({r: row, c: col})].v)
            }
            cols.push(rows)
        }

        // Upload to server
        console.log(cols)
    }

    this.readError = function (e) {
        console.log("Excel Read Error = " + e)
    }
})