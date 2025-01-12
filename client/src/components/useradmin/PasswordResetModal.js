import React, { Component } from 'react';
import { ModalHeader, ModalBody, Button, Modal, Container, Row } from 'reactstrap';
import Endpoints from '@/api/Endpoints';
import { toast } from 'react-toastify';

class PasswordResetModal extends Component {

    submitPasswordReset() {
        Endpoints.passwordReset(
            { "username": this.props.selectedUser.username })
            .then(updatedUser => {
                // on success
                toast.info('User password reset successful');
                this.props.togglePasswordResetModal(updatedUser);
            })
            .catch(error => {
                toast.info("Error resetting password: ", error.message);
                this.props.togglePasswordResetModal();
            });
    }


    render() {
        if (this.props.selectedUser == null || this.props.selectedUser == undefined) {
            return (<div />)
        }
        return (
            <Modal isOpen={this.props.isPasswordResetModalOpen} toggle={() => this.props.togglePasswordResetModal()}>
                <ModalHeader>Password</ModalHeader>
                <ModalBody>
                    <Container>
                        <Row>
                            This will reset password for user {this.props.selectedUser.username}. Are you sure?
                            </Row>
                        <Row>
                            <Button outline color="primary" className="mt-3"
                                onClick={this.submitPasswordReset.bind(this)}>Confirm</Button>&nbsp;
                                 <Button outline color="danger" className="mt-3"
                                onClick={() => this.props.togglePasswordResetModal()}>Cancel</Button>
                        </Row>
                    </Container>

                </ModalBody>
            </Modal>
        );
    }
}

export default PasswordResetModal;