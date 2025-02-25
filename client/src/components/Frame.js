import React, { Component } from 'react';
import AppHeader from './header/AppHeader.js';
import SideBar from './SideBar'
import User from './header/User.js';
import '@/styles/Frame.css'
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import Endpoints from '@/api/Endpoints';
import { Outlet } from 'react-router-dom';

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
            <div className={'body-main'}  style={{height:'100%', overflow: 'hidden'}}>
                <AppHeader user={this.state.user} isAdmin={this.state.isAdmin} toggleUserModal={this.toggleUserModal.bind(this)}
                    toggleSidebar={this.toggleSidebar.bind(this)} />
                <SideBar isSideBarCollapsed={this.state.isSideBarCollapsed} toggleSidebar={this.toggleSidebar.bind(this)}
                    isAdmin={this.state.isAdmin}></SideBar>
                <User user={this.state.user}
                    toggleUserModal={this.toggleUserModal.bind(this)} isUserModalOpen={this.state.isUserModalOpen}></User>
                <ToastContainer className="myToast" />
                <div style={{marginTop: '6rem',height: '80%'}}>
                    <Outlet />
                </div>                
            </div>
        );
    }

}

export default Frame;