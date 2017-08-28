/**
 * Created by Siddharth on 26-08-2017.
 */

app.service("ExcelService", function ($http, $localStorage) {

    this.excelRead = function (workbook) {
        // Get Worksheet
        var worksheet = workbook.Sheets[workbook.SheetNames[0]]

        // Parse worksheet into JSON
        var range = XLSX.utils.decode_range(worksheet['!ref'])
        var rows = []
        for (row = range.s.r; row <= range.e.r; row++){
            var cols = []
            for (col = range.s.c; col <= range.e.c; col++){
                var cell = worksheet[XLSX.utils.encode_cell({r: row, c: col})]
                if (cell != undefined)
                {
                    cols.push(cell.v)
                }
                else
                {
                    cols.push("")
                }
            }
            rows.push(cols)
        }

        // Upload to server
        return $http({
            method:     'POST',
            url:        '/api/leads/upload',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data:       {
                excelData:           JSON.stringify(rows)
        }
    })
    }
})