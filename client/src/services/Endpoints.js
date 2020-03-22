import axios from 'axios';

export default {

    getAllUsers() {
        return axios.get('/api/user/all')
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
    },
    createUser(user) {
        return axios.post('/api/user', user)
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
    },
    updateUser(user) {
        return axios.put('/api/user', user)
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
    },
    deleteUser(user) {
        return axios.delete('/api/user/' + user.username)
            .catch(error => { console.log(error); throw Error(error) })
            .then(res => res.data);
    }

}
