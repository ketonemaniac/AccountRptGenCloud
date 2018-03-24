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

    });

}(jQuery);