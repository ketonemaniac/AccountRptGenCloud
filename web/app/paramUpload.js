"use strict";

// UPLOAD CLASS DEFINITION
// ======================
var uploadForm = document.getElementById('js-param-form');
var prefix = "";

var startUpload = function (email) {

    var spinHandle = loadingOverlay().activate();
    $.ajax({
        url: prefix + '/saveParam',
        type: 'POST',
        data: '{ "mail.sendto" : "' + email + '" }',
        contentType: "application/json",
        success: function (data) {
            loadingOverlay().cancel(spinHandle);
            $.notify({
                message: 'Successfully updated parameters to server'
            }, {
                    type: 'success'
                });
            $("#email").text(email);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            loadingOverlay().cancel(spinHandle);
            $.notify({
                message: 'Failed to save parameters'
            }, {
                    type: 'danger'
                });
        }
    });
}

var init = function (path) {

    prefix = path;

    // EVENT LISTENERS
    // ===============
    uploadForm.addEventListener('submit', function (f) {
        var email = $('#email').val();
        f.preventDefault()
        startUpload(email)
    })

}

module.exports.init = init;
