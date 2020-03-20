import React, { Component } from 'react';
import { Navbar, NavbarBrand, Nav, NavItem, NavLink, NavbarToggler, Collapse  } from 'reactstrap';
import './AppHeader.css';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUserAlt } from '@fortawesome/free-solid-svg-icons';

class AppHeader extends Component {

  state = {
    info: {}
  }

  componentDidMount() {
    this.callApi()
      .then(res => this.setState({ info: res }))
      .catch(err => console.log(err));
  }

  callApi = async () => {
    const response = await fetch('/version');
    const body = await response.json();
    if (response.status !== 200) throw Error(body.message);
    return body;
  };

  render() {
    return (
      <Navbar className="fixed-top" color="faded" light>
        <Nav>
        <NavbarToggler onClick={this.props.toggleSidebar} className="mr-2"/>
        <NavbarBrand href="/">
          Account Report Generator <span><small className="text-sm font-weight-light">{this.state.info.version}</small></span>
        </NavbarBrand>
        </Nav>
        <Nav>
          <NavItem>
            <div className="header-user" onClick={this.props.toggleUserModal}>
              <FontAwesomeIcon icon={faUserAlt} className="mr-2" />{this.state.info.user}
            </div>
          </NavItem>
        </Nav>
      </Navbar>
    );
  }
}


export default AppHeader;
