import axios from 'axios';

function get(path) {
    return axios.get(path)
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
}

function post(path, data) {
    return axios.get("/csrf")
            .then(tokenResp => {
                let config = {
                    headers: {
                        'X-CSRF-TOKEN': tokenResp.data.token,
                    }
                  }
                return axios.post(path, data, config);
            })
            .then(res => res.data)
}

function put(path, data) {
    return axios.get("/csrf")
            .then(tokenResp => {
                let config = {
                    headers: {
                        'X-CSRF-TOKEN': tokenResp.data.token,
                    }
                }
                return axios.put(path, data, config);
            })
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
}

function del(path) {
    return axios.get("/csrf")
            .then(tokenResp => {
                let config = {
                    headers: {
                        'X-CSRF-TOKEN': tokenResp.data.token,
                    }
                }
                return axios.delete(path, config);
            })
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
}

/**
 * ajax doesn't handle file downloads elegantly
 * @param {*} filename 
 */
function download(url, filename) {
    var req = new XMLHttpRequest();
    var fullUrl = url + "?file=" + encodeURIComponent(filename); 
    req.open("GET", fullUrl, true);
    req.setRequestHeader("Content-Type", "application/json");
    req.responseType = "blob";
    req.onreadystatechange = function () {
        if (req.readyState === 4 && req.status === 200) {
        // test for IE
        if (typeof window.navigator.msSaveBlob === 'function') {
            window.navigator.msSaveBlob(req.response, filename);
        } else {
            var blob = req.response;
            var link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = filename;
            // append the link to the document body
            document.body.appendChild(link);
            link.click();
            link.remove();// you need to remove that element which is created before
        }
        }
    };
    req.send(JSON.stringify({ "filename": filename }));
}


export default {

    getAllUsers() {
        return get('/api/admin/user/all')
    },
    createUser(user) {
        return post('/api/admin/user', user)
    },
    updateUser(user) {
        return put('/api/admin/user', user)
    },
    deleteUser(user) {
        return del('/api/admin/user/' + user.username)
    },
    passwordReset(user) {
        return post('/api/admin/user/password/reset', user)
    },
    getUser() {
        return get('/api/user')
    },
    getAllSettings() {
        return get('/api/settings/all')
    },
    getFileList() {
        return get('/api/settings/fileList')
    },
    saveSettings(settings) {
        return post('/api/settings/upsert', settings)
    },
    putTemplate(fileType, template) {
        return put('/api/settings/template/' + fileType, template)
    },
    setActiveTemplate(fileType, template) {
        return post('/api/settings/template/' + fileType + '/active/' + template)
    },
    deleteTemplate(fileType, template) {
        return del('/api/settings/template/' + fileType + '/' + template)
    },
    getVersion() {
        return get('/api/version')
    },
    resetPassword(pass) {
        return post('/api/user/password', pass)
    },
    listFiles() {
        return get('/api/accrptgen/taskList')
    },
    uploadFile(file) {
        return post('/api/accrptgen/file', file)
    },
    generate(accountJob) {
        return post('/api/accrptgen/startGeneration', accountJob)
    },
    downloadGeneratedZip(filename) {
        return download('/api/accrptgen/file', filename)
    },
    downloadTemplate(filename) {
        return download('/api/settings/file', filename)
    }
}
