import React, { Component } from 'react';
import { Modal, ModalBody, ModalHeader, Form, FormGroup, Label, Input, Button, Alert } from 'reactstrap';
import Endpoints from '../../api/Endpoints.js';
import { toast } from 'react-toastify';

class UserEditModal extends Component {

    state = {
        selectedUser : null
    }

    componentDidUpdate(prevProps) {
      if(this.props.selectedUser?.cc !== prevProps.selectedUser?.cc) {
        this.setState({
          cc : this.props.selectedUser?.cc == null ? null : [...this.props.selectedUser.cc]
        })      
      }
    }


    addCc() {
      this.setState((oldState) => {
        oldState.cc = oldState.cc == null ? [""] :   
         [...this.state.cc, ""]
        return oldState
      })
    }

    delCc(i) {
      this.setState((oldState) => {
        oldState.cc.splice(i, 1);
        return oldState
      })
    }

    validateForm(event) {
      const form = document.querySelector('#userForm')
      if (!form.checkValidity()) {
            event.preventDefault()
            event.stopPropagation()
            form.classList.add('was-validated')
            return false
      }
      return true
    }

    submitUserEdit(event) {
        if(!this.validateForm(event))
          return
        event.preventDefault();
        const data = event.target;
        const ccs = Array.from(document.querySelectorAll('.cc'), el => el.value)
        var roles = [];
        if(data.userRole.checked) roles.push({"name": "User"});
        if(data.adminRole.checked) roles.push({"name": "Admin"});
        var action;
        if(this.props.isEdit) {
          action = Endpoints.updateUser(
            {"username" : data.username.value,
            "email": data.email.value,
            "roles" : roles,
            "cc" : ccs
          });
        } else {
          action = Endpoints.createUser(
            {"username" : data.username.value,
            "password" : data.password.value,
            "email": data.email.value,
            "roles" : roles,
            "cc" : ccs
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

    render() {
        return (
            <Modal isOpen={this.props.isUserEditModalOpen} toggle={() => this.props.toggleUserEditModal()}>
                <ModalHeader>{this.props.isEdit ? "Edit" : "Add"} User</ModalHeader>
                <ModalBody className="user-heading">
                    <Form id="userForm" onSubmit={this.submitUserEdit.bind(this)} noValidate>
                        <FormGroup className="mx-5">
                            <Label for="username">Username</Label>
                            <Input disabled={this.props.isEdit ? true : false}
                             type="username" name="username" id="username"
                                defaultValue={this.props.isEdit ? 
                                  this.props.selectedUser?.username : ""} required></Input>
                        </FormGroup>
                        <FormGroup className="mx-5">
                            <Label for="email">Email</Label>
                            <Input type="email" name="email" id="email"
                                defaultValue={this.props.isEdit ? 
                                  this.props.selectedUser?.email : ""} required />
                            <div className="invalid-feedback">
                              Invalid email Format
                            </div>
                        </FormGroup>
                        {this.renderPasswordField()}
                        <FormGroup className="mx-5">
                              <Label for="cc">cc</Label>

                          {this.state?.cc?.map((element, index) => (
                                <div className="d-flex" key={index}>
                                  <Input className="formControl flex-grow-1 cc" type="email" name="cc"
                                    defaultValue={element}
                                    required                             
                                  />
                                  <Button outline onClick={() => this.delCc(index)}>-</Button> 
                                </div>                                
                          ))}
                          <div className="d-flex justify-content-end">
                            <Button outline color="primary" onClick={this.addCc.bind(this)}>Add cc</Button>                    
                          </div>  
                        </FormGroup>
                        <Label className="mx-5">Roles</Label>
                        <div className="d-flex">
                          <FormGroup className="mx-5" check>
                              <Label>
                                  <Input type="checkbox" name="userRole" style={{"margin-top" : 0}}
                                  defaultChecked={
                                    this.props.isEdit ?
                                    this.props.selectedUser?.roles.filter(
                                      role => role.name=="User").length > 0
                                    : false}/>{' '}User
                              </Label>
                          </FormGroup>
                          <FormGroup className="mx-5" check>
                              <Label>
                                  <Input type="checkbox" name="adminRole" style={{"margin-top" : 0}}
                                  defaultChecked={this.props.isEdit ?
                                    this.props.selectedUser?.roles.filter(
                                      role => role.name=="Admin").length > 0
                                      : false}/>{' '}Admin
                              </Label>
                          </FormGroup>
                        </div>
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
        <Input type="password" name="password" id=" password"></Input>
    </FormGroup>)
      } return "";
    }

}

export default UserEditModal;