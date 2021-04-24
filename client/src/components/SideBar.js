import React, { Component } from 'react';
import { ListGroup, ListGroupItem } from 'reactstrap';
import { Link } from "react-router-dom";
import '../styles/SideBar.css';

class SideBar extends Component {

    linkOnclick() {
        this.props.toggleSidebar();
    }

    render() {
        return (
            <div className={this.props.isSideBarCollapsed ? "width show" : "width"} id="sidebar">
                <ListGroup className="sidebar-list text-nowrap">
                    <Link onClick={this.linkOnclick.bind(this)} to="/app/main"><ListGroupItem>Report Generation</ListGroupItem></Link>
                    <Link onClick={this.linkOnclick.bind(this)} to="/app/users"><ListGroupItem>Users</ListGroupItem></Link>
                    <Link onClick={this.linkOnclick.bind(this)} to="/app/settings"><ListGroupItem>Settings</ListGroupItem></Link>
                </ListGroup>
            </div>
        );
    }
}

export default SideBar;

{/* <Collapse className="collapse" isOpen={this.props.isSideBarCollapsed} navbar id="sidebar">
                <Nav navbar>
                    <NavItem>
                        <NavLink href="/components/">Components</NavLink>
                    </NavItem>
                    <NavItem>
                        <NavLink href="https://github.com/reactstrap/reactstrap">GitHub</NavLink>
                    </NavItem>
                </Nav>
            </Collapse> */}