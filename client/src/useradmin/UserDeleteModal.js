import React, { Component } from 'react';
import { ModalHeader, ModalBody, Button, Modal } from 'reactstrap';
import Endpoints from '../services/Endpoints.js';
import { toast } from 'react-toastify';

class UserDeleteModal extends Component {

    state = {
    }

    submitUserDelete() {
        Endpoints.deleteUser(
            {"username" : this.props.selectedUser.username})
        .then(updatedUser => {
            // on success
            toast.info('User deleted successfully');
            this.props.toggleUserDeleteModal();
        })
        .catch(error => {
            toast.info("Error deleting user: ", error.message);
            this.props.toggleUserDeleteModal();
        });
    }

    render() {
        if(this.props.selectedUser == null || this.props.selectedUser == undefined) {
            return (<div />)
          }
        return (
            <Modal isOpen={this.props.isUserDeleteModalOpen} toggle={() => this.props.toggleUserDeleteModal()}>
                <ModalHeader>Delete User</ModalHeader>
                <ModalBody>This will delete the user {this.props.selectedUser.username}. Are you sure?
                <Button outline color="primary" className="mt-3"
                        onClick={this.submitUserDelete.bind(this)}>Confirm</Button>&nbsp;
                <Button outline color="danger" className="mt-3" 
                        onClick={() => this.props.toggleUserDeleteModal()}>Cancel</Button>
                </ModalBody>
            </Modal>
        );
    }
}

export default UserDeleteModal;