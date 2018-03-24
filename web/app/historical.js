var DateFormat = require('dateformat');

'use strict';

// UPLOAD CLASS DEFINITION
// ======================
var histTable = $('#genHistoryTable').DataTable({
    "paging": false,
    "ordering": false,
    "info": false,
    "searching": false
});


// AJAX FUNCTIONS
// ===============

var listFiles = function () {
    $.ajax({
        url: '/listFiles',
        type: 'GET',
        processData: false,
        contentType: false,
        success: function (dataArr) {
            histTable.clear();
            $.each(dataArr, function (idx, data) {
                var statusText = "<span class=\"glyphicon glyphicon-envelope\" style=\"color:green\"></span>&nbsp;" + data.status;
                histTable.row.add([
                    data.company,
                    DateFormat(new Date(data.generationTime), "yyyy-mm-dd HH:MM:ss"),
                    statusText,
                ]);
            });
            histTable.draw(false);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            $.notify({
                message: "Historical listing failed, trying in 10 secs. Debug info=" + errorThrown
            }, {
                    type: 'danger'
                });
        }
    });
}

var init = function () {
    listFiles();
    setInterval(listFiles, 10000);

    // initialize historical chart
    var ctx = document.getElementById("myChart");
    var labels = ['January', 'February', 'March', 'April', 'May', 'June'];
    var data = [20, 10, 30, 40, 50];
    var myChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                borderColor: "#3e95cd",
                fill: false
            }]
        },
        options: {
            legend: {
                display: false
            },
            title: {
                display: true,
                text: 'Reports Generated'
            }
        }
    });
}

module.exports.init = init;