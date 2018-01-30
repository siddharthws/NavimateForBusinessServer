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
    this.export = function (values, columns, fileNamePrefix){
        // Create JSON based on visible columns
        var json = []
        for (var i = 0; i < values.length; i++) {
            var row = values[i]
            json.push({})
            for (var j = 0; j < columns.length; j++) {
                var column = columns[j]
                var value = row['Col' + column.id]
                if (column.show) {
                    if (value != '-') {
                        if (column.type == Constants.Template.FIELD_TYPE_LOCATION) {
                            value = "https://www.google.com/maps/search/?api=1&query=" + value
                        } else if ((column.type == Constants.Template.FIELD_TYPE_PHOTO) || (column.type == Constants.Template.FIELD_TYPE_SIGN)) {
                            value = "https://biz.navimateapp.com/#/photos?name=" + value
                        }
                    }

                    json[json.length - 1][column.title] = value
                }
            }
        }

        // Convert json to workbook
        var ws = XLSX.utils.json_to_sheet(json)
        var wb = { SheetNames: [], Sheets: {} }
        XLSX.utils.book_append_sheet(wb, ws, "Sheet 1")

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

    // API to save lead upload template in excel file
    this.leadUploadTemplate = function (){
        // Create worksheet from JSON
        var json = [{
            "Title*":       "",
            "Phone*":       "",
            "Address*":     "",
            "Description":  "",
            "Email":        "",
            "Latitude":     "",
            "Longitude":    "",
        }]
        var ws = XLSX.utils.json_to_sheet(json)

        // Create workbook
        var wb = { SheetNames: [], Sheets: {} }
        XLSX.utils.book_append_sheet(wb, ws, "Sheet 1")

        // Save workbook
        var wbOut = XLSX.write(wb, {bookType:'xlsx', bookSST:true, type: 'binary'})
        var blob  = new Blob([s2ab(wbOut)],{type:"application/octet-stream"})

        try {
            FileSaver.saveAs(blob, "Leads_Template.xlsx")
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