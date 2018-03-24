// var Spinner = require('spin');
var FileUpload = require('./fileUpload.js');
var ParamUpload = require('./paramUpload.js');

+ function ($) {
    'use strict';

    // MAIN STARTUP FLOW
    // ==================
    $(document).ready(function () {

        FileUpload.init("/admin", function(data) { return "File upload success. New Template: " + data.filename; });
        ParamUpload.init("/admin");
        
        // populate attributes
        $.ajax({
            url: '/admin/getParam',
            type: 'GET',
            contentType: "application/json",
            success: function (data) {
                $("#email").val(data["mail.sendto"]);
                $("#currentFile").val(data["xlsx.template.name"]);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $.notify({
                    message: "Parameter settings loading failed. Debug info=" + errorThrown
                }, {
                        type: 'danger'
                    });
            }
        });

    });

}(jQuery);