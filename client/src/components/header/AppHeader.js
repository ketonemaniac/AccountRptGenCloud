import React, { Component } from 'react';
import { Navbar, NavbarBrand, Nav, NavItem, NavLink, NavbarToggler, Collapse  } from 'reactstrap';
import '@/styles/AppHeader.css';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUserAlt } from '@fortawesome/free-solid-svg-icons';
import Endpoints from '@/api/Endpoints';

class AppHeader extends Component {

  state = {
    info: {}
  }

  componentDidMount() {
    Endpoints.getVersion()
      .then(res => this.setState({ info: res }))
      .catch(err => console.log(err));
  }

  render() {
    return (
      <Navbar className="fixed-top" color="faded" light>
        <Nav>
          {this.props.isAdmin ? (
            <NavbarToggler onClick={this.props.toggleSidebar} className="mr-2"/>
          ) : ""}
        <NavbarBrand href="/">
          <span className="logo">Account Report Generator</span>
          <span><small className="text-sm font-weight-light">{this.state.info.version}</small></span>
        </NavbarBrand>
        </Nav>
        <Nav>
          <NavItem>
            <div className="header-user" onClick={this.props.toggleUserModal}>
              <FontAwesomeIcon icon={faUserAlt} className="mr-2" />{this.props.user.username}
            </div>
          </NavItem>
        </Nav>
      </Navbar>
    );
  }
}


export default AppHeader;
