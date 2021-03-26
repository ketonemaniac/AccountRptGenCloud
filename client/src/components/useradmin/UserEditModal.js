import React, { Component } from 'react';
import { Modal, ModalBody, ModalHeader, Form, FormGroup, Label, Input, Button, Alert } from 'reactstrap';
import Endpoints from '../../api/Endpoints.js';
import { toast } from 'react-toastify';

class UserEditModal extends Component {

    state = {
        errMsg : null
    }

    submitUserEdit(event) {
        event.preventDefault();
        const data = event.target;
        if(data.username.value.length == 0) {
          this.setState({errMsg : "Username cannot be empty"});
        } else if (data.email.value.length == 0) {
          this.setState({errMsg : "Email cannot be empty"});
        } else if (!this.props.isEdit && data.password.value.length == 0) {
          this.setState({errMsg : "Password cannot be empty"});
        } else {
            var roles = [];
            if(data.userRole.checked) roles.push({"name": "User"});
            if(data.adminRole.checked) roles.push({"name": "Admin"});
            var action;
            if(this.props.isEdit) {
              action = Endpoints.updateUser(
                {"username" : data.username.value,
                "email": data.email.value,
                "roles" : roles
              });
            } else {
              action = Endpoints.createUser(
                {"username" : data.username.value,
                "password" : data.password.value,
                "email": data.email.value,
                "roles" : roles
              });
            }
            action.then(updatedUser => {
              // on success
              toast.info('User '.concat(this.props.isEdit ? "updated" : "created", ' successfully'));
              this.props.toggleUserEditModal(updatedUser);
            })
            .catch(error => {
              this.setState({errMsg : "Error ".concat(this.props.isEdit ? "creating" : "updating", " user: ", error.message)});
            });
        }
      }

    render() {
        return (
            <Modal isOpen={this.props.isUserEditModalOpen} toggle={() => this.props.toggleUserEditModal()}>
                <ModalHeader>{this.props.isEdit ? "Edit" : "Add"} User</ModalHeader>
                <ModalBody className="user-heading">
                    <p />{this.renderInputError()}
                    <p /><Form onSubmit={this.submitUserEdit.bind(this)}>
                        <FormGroup className="mx-5">
                            <Label for="username">Username</Label>
                            <Input disabled={this.props.isEdit ? true : false}
                             type="username" name="username" id="username"
                                defaultValue={this.props.isEdit ? 
                                  this.props.selectedUser.username : ""}></Input>
                        </FormGroup>
                        <FormGroup className="mx-5">
                            <Label for="email">Email</Label>
                            <Input type="email" name="email" id="email"
                                defaultValue={this.props.isEdit ? 
                                  this.props.selectedUser.email : ""}
                                onChange={this.clearInputError.bind(this)}></Input>
                        </FormGroup>
                        {this.renderPasswordField()}
                        <Label className="mx-5">Roles</Label>
                        <FormGroup className="mx-5" check>
                            <Label>
                                <Input type="checkbox" name="userRole" style={{"margin-top" : 0}}
                                defaultChecked={
                                  this.props.isEdit ?
                                  this.props.selectedUser.roles.filter(
                                    role => role.name=="User").length > 0
                                  : false}/>{' '}User
                             </Label>
                        </FormGroup>
                        <FormGroup className="mx-5" check>
                            <Label>
                                <Input type="checkbox" name="adminRole" style={{"margin-top" : 0}}
                                defaultChecked={this.props.isEdit ?
                                  this.props.selectedUser.roles.filter(
                                    role => role.name=="Admin").length > 0
                                    : false}/>{' '}Admin
                             </Label>
                        </FormGroup>
                        <Button outline type="submit" color="primary" className="mt-3">Submit</Button>&nbsp;
                        <Button outline color="danger" className="mt-3" onClick={
                          () => this.props.toggleUserEditModal()
                        }>Cancel</Button>
                    </Form>
                </ModalBody>
            </Modal>

        );
    }

    renderPasswordField() {
      if(!this.props.isEdit) {
        return (<FormGroup className="mx-5">
        <Label for="password">Password</Label>
        <Input type="password" name="password" id=" password"
            onChange={this.clearInputError.bind(this)}></Input>
    </FormGroup>)
      } return "";
    }


    clearInputError() {
        if(this.state.errMsg != null) {
            this.setState({errMsg : null});
        }
      }
    
    renderInputError() {
        if (this.state.errMsg !== null) {
          return (
            <Alert color="danger" className="px-5">
              {this.state.errMsg}
            </Alert>
          )
        }
        return <span />;
      }
    
}

export default UserEditModal;