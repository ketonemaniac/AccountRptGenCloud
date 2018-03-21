// var Spinner = require('spin');
var FileUpload = require('./fileUpload.js');
var Historical = require('./historical.js');

+ function ($) {
    'use strict';

    // MAIN STARTUP FLOW
    // ==================
    $(document).ready(function () {

        FileUpload.init();
        Historical.init();

    });

}(jQuery);