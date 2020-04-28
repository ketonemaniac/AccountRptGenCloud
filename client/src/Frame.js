import React, { Component } from 'react';
import AppHeader from './AppHeader.js';
import SideBar from './SideBar.js'
import User from './User.js';
import './Frame.css'
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import Endpoints from './services/Endpoints.js';

class Frame extends Component {

    state = {
        isUserModalOpen: false,
        isSideBarCollapsed: false,
        user: {}
    }

    componentDidMount() {
        Endpoints.getUser().then(data => {
            this.setState({ user: data,
                isAdmin: data.roles.map(role => role.name).includes("Admin")
            });
        });
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
                <AppHeader user={this.state.user} isAdmin={this.state.isAdmin} toggleUserModal={this.toggleUserModal.bind(this)}
                    toggleSidebar={this.toggleSidebar.bind(this)} />
                    {this.state.isAdmin ? (
                        <SideBar isSideBarCollapsed={this.state.isSideBarCollapsed} toggleSidebar={this.toggleSidebar.bind(this)}></SideBar>
                    ) : ""}
                <User user={this.state.user}
                    toggleUserModal={this.toggleUserModal.bind(this)} isUserModalOpen={this.state.isUserModalOpen}></User>
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