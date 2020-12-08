import axios from 'axios';

export default {

    getAllUsers() {
        return axios.get('/api/admin/user/all')
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
    },
    createUser(user) {
        return axios.post('/api/admin/user', user)
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
    },
    updateUser(user) {
        return axios.put('/api/admin/user', user)
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
    },
    deleteUser(user) {
        return axios.delete('/api/admin/user/' + user.username)
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
    },
    passwordReset(user) {
        return axios.post('/api/admin/user/password/reset', user)
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
    },
    getUser() {
        return axios.get('/api/user')
        .catch(error => { console.log(error); throw Error(error) })
        .then(res => res.data);
    },
    getAllSettings() {
        return axios.get('/api/settings/all')
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
    },
    saveSettings(settings) {
        return axios.post('/api/settings/upsert', settings)
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
    },
}
