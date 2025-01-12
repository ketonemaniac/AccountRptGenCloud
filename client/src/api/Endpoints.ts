import axios from 'axios';

function get(path: string) {
    return axios.get(path)
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
}

function post(path: string, data?: any) {
    return axios.post(path, data)
            .then(res => res.data);
}

function put(path: string, data: any) {
    return axios.put(path, data)
            // .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
}

function del(path: string) {
    return axios.delete(path)
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
}

/**
 * ajax doesn't handle file downloads elegantly
 * @param {*} filename 
 */
function download(url: string, filename: string) {
    var req = new XMLHttpRequest();
    var fullUrl = url + "?file=" + encodeURIComponent(filename); 
    req.open("GET", fullUrl, true);
    req.setRequestHeader("Content-Type", "application/json");
    req.responseType = "blob";
    req.onreadystatechange = function () {
        if (req.readyState === 4 && req.status === 200) {
            var blob = req.response;
            var link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = filename;
            // append the link to the document body
            document.body.appendChild(link);
            link.click();
            link.remove();// you need to remove that element which is created before
        }
    };
    req.send(JSON.stringify({ "filename": filename }));
}


export default {

    getAllUsers() {
        return get('/api/admin/user/all')
    },
    createUser(user: any) {
        return post('/api/admin/user', user)
    },
    updateUser(user: any) {
        return put('/api/admin/user', user)
    },
    deleteUser(user: any) {
        return del('/api/admin/user/' + user.username)
    },
    passwordReset(user: any) {
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
    saveSettings(settings: any) {
        return post('/api/settings/upsert', settings)
    },
    putTemplate(fileType: string, template: any) {
        return put('/api/settings/template/' + fileType, template)
    },
    setActiveTemplate(fileType: string, template: any) {
        return post('/api/settings/template/' + fileType + '/active/' + template)
    },
    deleteTemplate(fileType: string, template: any) {
        return del('/api/settings/template/' + fileType + '/' + template)
    },
    getVersion() {
        return get('/api/version')
    },
    resetPassword(pass: any) {
        return post('/api/user/password', pass)
    },
    listFiles() {
        return get('/api/accrptgen/taskList')
    },
    uploadFile(file: any) {
        return post('/api/accrptgen/file', file)
    },
    generate(accountJob: any) {
        return post('/api/accrptgen/startGeneration', accountJob)
    },
    downloadGeneratedZip(filename: any) {
        return download('/api/accrptgen/file', filename)
    },
    downloadTemplate(filename: any) {
        return download('/api/settings/file', filename)
    },
    generateTabs(file: File) {
        const randInt:number = Math.floor(Math.random() * 10000000);
        console.log("acceptedFile=" + file.name + " size=" + file.size);
        const data = new FormData()
        data.append('file', file, file.name)
        data.append('seed', randInt.toString())
        return post('/api/v2/accrptgen/file', data);
    }
}
