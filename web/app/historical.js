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
            var regen = false;
            histTable.clear();
            $.each(dataArr, function (idx, data) {
                switch (data.status) {
                    case "EMAIL_SENT":
                        var statusText = "<span><i class=\"fas fa-envelope\" style=\"color:green\"></i></span>&nbsp;Email Sent";
                        break;
                    case "FAILED":
                        statusText = "<span><i class=\"fas fa-exclamation-circle\" style=\"color:red\"></i></span>&nbsp;Failed";
                        break;
                    case "GENERATING":
                        statusText = "<span><i class=\"fas fa-cog blink\" style=\"color:grey\"></i></span>&nbsp;Generating";
                        regen = true;
                        break;
                }
                histTable.row.add([
                    data.company,
                    DateFormat(new Date(data.generationTime), "yyyy-mm-dd HH:MM:ss"),
                    statusText,
                ]);
            });
            histTable.draw(false);
            if(regen) {
                setTimeout(listFiles, 10000);
            }
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
    // setInterval(listFiles, 10000);

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
module.exports.listFiles = listFiles;