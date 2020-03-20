import React, { Component } from 'react';
import AppHeader from './AppHeader.js';
import SideBar from './SideBar.js'
import User from './User.js';
import './Frame.css'
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

class Frame extends Component {

    state = {
        isUserModalOpen: false,
        isSideBarCollapsed: false
    }

    // USER MODAL
    toggleUserModal() {
        this.setState((oldState) => {
            return { isUserModalOpen: !oldState.isUserModalOpen }
        });
    }

    // SIDEBAR
    toggleSidebar() {
        this.setState((prevState) => {
            return { isSideBarCollapsed: !prevState.isSideBarCollapsed }
        })
    };


    render() {
        return (
            <div>
                <AppHeader isAdmin={this.state.isAdmin} setAdmin={this.setAdmin} toggleUserModal={this.toggleUserModal.bind(this)}
                    toggleSidebar={this.toggleSidebar.bind(this)} />
                <SideBar isSideBarCollapsed={this.state.isSideBarCollapsed} toggleSidebar={this.toggleSidebar.bind(this)}></SideBar>
                <User toggleUserModal={this.toggleUserModal.bind(this)} isUserModalOpen={this.state.isUserModalOpen}></User>
                <ToastContainer
                    position="top-right"
                    autoClose={5000}
                    hideProgressBar
                    newestOnTop={false}
                    closeOnClick
                    rtl={false}
                    pauseOnVisibilityChange
                    draggable
                    pauseOnHover
                />

            </div>
        );
    }

}

export default Frame;