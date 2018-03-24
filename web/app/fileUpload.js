"use strict";

// UPLOAD CLASS DEFINITION
// ======================
var dropZone = document.getElementById('drop-zone');
var uploadForm = document.getElementById('js-upload-form');
var prefix = "";
var msg = "";
var filename = "";

var startUpload = function (files) {

    // https://coligo.io/building-ajax-file-uploader-with-node/

    if (files.length > 0) {
        // One or more files selected, process the file upload

        // create a FormData object which will be sent as the data payload in the
        // AJAX request
        var formData = new FormData();

        // loop through all the selected files
        for (var i = 0; i < files.length; i++) {
            var file = files[i];

            // add the files to formData object for the data payload
            formData.append('file', file, file.name);
            var spinHandle = loadingOverlay().activate();
            $.ajax({
                url: prefix + '/uploadFile',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    loadingOverlay().cancel(spinHandle);
                    $.notify({
                        message: msg(data)
                    }, {
                            type: 'success'
                        });
        
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.log('error');
                    loadingOverlay().cancel(spinHandle);
                    $.notify({
                        message: "Operation failed. Debug info=" + errorThrown
                    }, {
                            type: 'danger'
                        });
                }
                /*xhr: function() {
                    // create an XMLHttpRequest
                    var xhr = new XMLHttpRequest();
 
                    // listen to the 'progress' event
                    xhr.upload.addEventListener('progress', function(evt) {
 
                    if (evt.lengthComputable) {
                        // calculate the percentage of upload completed
                        var percentComplete = evt.loaded / evt.total;
                        percentComplete = parseInt(percentComplete * 100);
 
                        // update the Bootstrap progress bar with the new percentage
                        $('.progress-bar').text(percentComplete + '%');
                        $('.progress-bar').width(percentComplete + '%');
 
                        // once the upload reaches 100%, set the progress bar text to done
                        if (percentComplete === 100) {
                            $('.progress-bar').html('Done');
                            // spinner.spin();
                            $(".overlay").fadeIn().append(spinner.el);
                            // new Spinner({color:'#999', lines: 12}).spin($(".overlay"));
 
                        }
 
                    }
 
                    }, false);
 
                    return xhr;
                }*/
            });
        }


    }


}


var startDownload = function (filename) {
    // ajax doesn't handle file downloads elegantly
    var req = new XMLHttpRequest();
    req.open("POST", prefix + "/downloadFile", true);
    req.setRequestHeader("Content-Type", "application/json");
    req.responseType = "blob";
    req.onreadystatechange = function () {
        if (req.readyState === 4 && req.status === 200) {
            // test for IE
            if (typeof window.navigator.msSaveBlob === 'function') {
                window.navigator.msSaveBlob(req.response, "PdfName-" + new Date().getTime() + ".pdf");
            } else {
                var blob = req.response;
                var link = document.createElement('a');
                link.href = window.URL.createObjectURL(blob);
                link.download = filename;
                // append the link to the document body
                document.body.appendChild(link);
                link.click();
            }
        }
    };
    req.send(JSON.stringify({ "filename": filename }));
}

var init = function (path, msgFn) {

    prefix = path;
    msg = msgFn;

    // EVENT LISTENERS
    // ===============
    uploadForm.addEventListener('submit', function (f) {
        var uploadFiles = document.getElementById('js-upload-files').files;
        f.preventDefault()
        startUpload(uploadFiles)
    })

    dropZone.ondrop = function (f) {
        // prevent brower from really opening the file
        f.preventDefault();
        this.className = 'upload-drop-zone';
        startUpload(f.dataTransfer.files)
    }

    dropZone.ondragover = function () {
        this.className = 'upload-drop-zone drop';   // change css
        return false;
    }

    dropZone.ondragleave = function () {
        this.className = 'upload-drop-zone';
        return false;
    }

    // flieselect event definition
    // put filename into text under input-group
    $(':file').on('fileselect', function (event, numFiles, label) {
        var input = $(this).parents('.input-group').find(':text'),
            log = numFiles > 1 ? numFiles + ' files selected' : label;

        if (input.length) {
            input.val(log);
        } else {
            if (log) alert(log);
        }
    });
    
    // We can attach the `fileselect` event to all file inputs on the page
    $(document).on('change', ':file', function () {
        var input = $(this),
            numFiles = input.get(0).files ? input.get(0).files.length : 1,
            label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
        input.trigger('fileselect', [numFiles, label]);
    });


    $(document).on('click', '#genHistoryTable .dlbtn', function () {
        startDownload($(this).closest("tr").children('td:nth-child(3)').text());
    });
}

module.exports.init = init;
