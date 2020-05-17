import React, { Component } from 'react';
import axios from 'axios';
import { Alert, Button, Modal, ModalHeader, ModalBody, ModalFooter, Input, Label, Form, FormGroup } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUserCircle } from '@fortawesome/free-regular-svg-icons';
import { toast } from 'react-toastify';
import './User.css'

class User extends Component {

  state = {
    isChangePasswordDialogOpen: false,
    passwordErr: null
  }

  componentDidUpdate(prevProps, prevState) {
    if(prevProps.isUserModalOpen && !this.props.isUserModalOpen) {
      // closing Modal, reset everything
      this.setState((oldState) => {return {isChangePasswordDialogOpen: false,
      passwordErr: null}})
    }
  }

  toggleChangePasswordDialog() {
    this.setState((oldState) => {
      return {isChangePasswordDialogOpen: !oldState.isChangePasswordDialogOpen}
    })
  }

  clearPasswordErr() {
    this.setState({passwordErr : null});
  }

  submitPasswordChange(event) {
    event.preventDefault();
    const data = event.target;
    if(data.password.value.length === 0) {
      this.setState({passwordErr : "password cannot be empty"});
    } else if (data.password.value != data.confirmPassword.value) {
      this.setState({passwordErr : "passwords do not match"});
    } else {
      axios
        .post("/api/user/password", {"password" : data.password.value})
        .then(res => {
          // on success
          toast.info('Password updated successfully');
          this.props.toggleUserModal();
        })
        .catch(error => {
          this.setState({passwordErr : "Error updating password: " + error.response.data.message});
        });

    }
  }

  render() {
    if (!this.props.isUserModalOpen) {
      return "";
    }
    console.log(this.props.user);
    return (
      <Modal className="user-modal" isOpen={this.props.isUserModalOpen} toggle={this.props.toggleUserModal}>
        <ModalBody className="user-heading">
          <FontAwesomeIcon icon={faUserCircle} style={{ "fontSize": "3em" }} />
          <p /><b>{this.props.user.username}</b>
          <br />{this.props.user.email}
          <p />{this.renderPasswordError()}
          <p />{this.showChangePasswordDialog()}
        </ModalBody>
      </Modal>

    );
  }

  showChangePasswordDialog() {
    if(!this.state.isChangePasswordDialogOpen) {
      return (
        <Button color="primary" onClick={this.toggleChangePasswordDialog.bind(this)}>Change Password</Button>
      )
    } else {
      return (
        <Form onSubmit={this.submitPasswordChange.bind(this)}>
            <FormGroup className="mx-5">
              <Label for="password">Password</Label>
              <Input type="password" name="password" id="password" onChange={this.clearPasswordErr.bind(this)}/>
            </FormGroup>
            <FormGroup className="mx-5">
              <Label for="confirmPassword">Confirm Password</Label>
              <Input type="password" name="confirmPassword" id="confirmPassword" onChange={this.clearPasswordErr.bind(this)}/>
            </FormGroup>
            <Button type="submit" color="primary" className="mt-3">Submit Change</Button>
          </Form>
      )
    }
  }


  renderPasswordError() {
    if (this.state.passwordErr !== null) {
      return (
        <Alert color="danger" className="px-5">
          {this.state.passwordErr}
        </Alert>
      )
    }
    return <span />;
  }

}

export default User;