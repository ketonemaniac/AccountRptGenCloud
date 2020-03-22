import React, { Component } from 'react';
import { Table, Container ,Button, Row, Col } from 'reactstrap';
import Endpoints from '../services/Endpoints.js';
import UserEditModal from './UserEditModal.js'
import UserDeleteModal from './UserDeleteModal.js'

class UserAdmin extends Component {

    state = {
        users: [],
        isUserEditModalOpen: false,
        isUserDeleteModalOpen: false,
        isEdit: false,
        selectedUser: null
    }

    toggleUserEditModal(user) {
        const userExist = user != null && user != undefined && user.username != undefined;
        this.setState((oldState) => {
            if(!oldState.isUserEditModalOpen) {
                // "New User" or "Edit User" OnPress
                return {
                    isUserEditModalOpen: true,
                    selectedUser: user,
                    users: oldState.users,
                    isEdit: userExist
                }
            } else if(userExist) {
                var newUsers;
                // returning from some successful operation
                if(oldState.isEdit) {
                    newUsers = oldState.users.map(us => 
                        (us.username == user.username) ? user : us);
                } else {
                    newUsers = oldState.users.concat(user);
                }
                return {
                    isUserEditModalOpen: false,
                    selectedUser: null,
                    users: newUsers,
                    isEdit: false
                }
            } else {
                // a cancellation operation
                return {
                    isUserEditModalOpen: false,
                    selectedUser: null,
                    users: oldState.users,
                    isEdit: false
                }
            }
        })
    }

    toggleUserDeleteModal(user) {
        const userExist = user != null && user != undefined && user.username != undefined;
        this.setState((oldState) => {
            var newUsers = oldState.users;
            if(oldState.isUserDeleteModalOpen && userExist) {
                newUsers = oldState.users.filter(us => us.username !== user.username);
            }
            return {
                isUserDeleteModalOpen: !oldState.isUserDeleteModalOpen,
                selectedUser: oldState.isUserDeleteModalOpen ? null : user,
                users: newUsers
            }
        })
    }

    
    componentDidMount() {
        Endpoints.getAllUsers().then(data => this.setState({ users: data }));;
    }

    render() {
        return (
            <main>
                <Container className="themed-container">
                <Row className="py-3">
                    <Col><h1>User Maintenance</h1></Col>
                    <Col><Button outline className="float-right" color="primary"
                    onClick={this.toggleUserEditModal.bind(this)}>Add User</Button></Col>
                </Row>
                    
                    <Table striped>
                        <thead>
                            <tr>
                                <th>Username</th>
                                <th>Email</th>
                                <th>Roles</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {this.state.users.map(user => {
                                return (
                                    <tr>
                                        <td scope="row">{user.username}</td>
                                        <td>{user.email}</td>
                                        <td>{user.roles.map(
                                            role => {return role.name + " ";}
                                        )}</td>
                                        <td>
                                        <Button outline color="primary" size="sm"
                                        onClick={this.toggleUserEditModal.bind(this,user)}>Edit</Button>&nbsp;
                                            <Button outline color="primary" size="sm">Reset Password</Button>&nbsp;
                                        <Button outline color="danger" size="sm"
                                        onClick={this.toggleUserDeleteModal.bind(this,user)}>Delete User</Button></td>
                                    </tr>
                                )
                            })}
                        </tbody>
                    </Table>
                </Container>
                <UserEditModal isUserEditModalOpen={this.state.isUserEditModalOpen} 
                                toggleUserEditModal={this.toggleUserEditModal.bind(this)}
                                selectedUser={this.state.selectedUser}
                                isEdit={this.state.isEdit} />
                <UserDeleteModal isUserDeleteModalOpen={this.state.isUserDeleteModalOpen} 
                                toggleUserDeleteModal={this.toggleUserDeleteModal.bind(this)}
                                selectedUser={this.state.selectedUser} />
            </main>
        );
    }
}

export default UserAdmin;