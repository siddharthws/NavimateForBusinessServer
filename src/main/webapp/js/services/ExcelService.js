/**
 * Created by Siddharth on 26-08-2017.
 */

app.service("ExcelService", function ($http, $localStorage, $filter, FileSaver, Blob) {

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
                if (cell)
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

    // APIs to save table in excel file
    this.export = function (table, fileNamePrefix){
        // Convert table to workbook
        var wb = XLSX.utils.table_to_book(table, {sheet:"Sheet 1"})

        // Create saveable blob object
        var wbOut = XLSX.write(wb, {bookType:'xlsx', bookSST:true, type: 'binary'})
        var blob  = new Blob([s2ab(wbOut)],{type:"application/octet-stream"})

        // Get suffix (timestamp) to attach to file name
        var fileNameSuffix = getFileSuffix()
        var fileName = fileNamePrefix + fileNameSuffix + ".xlsx"

        try {
            FileSaver.saveAs(blob, fileName)
        } catch(e) {
            console.log(e, wbOut)
        }
    }

    function getFileSuffix(){
        var timestamp = new Date()
        timestamp = $filter('date')(timestamp, "-dd-MM-yy-HHmm")
        return timestamp
    }

    function s2ab(s) {
        if(typeof ArrayBuffer !== 'undefined') {
            var buf = new ArrayBuffer(s.length);
            var view = new Uint8Array(buf);
            for (var i=0; i!=s.length; ++i) view[i] = s.charCodeAt(i) & 0xFF;
            return buf;
        } else {
            var buf = new Array(s.length);
            for (var i=0; i!=s.length; ++i) buf[i] = s.charCodeAt(i) & 0xFF;
            return buf;
        }
    }
})