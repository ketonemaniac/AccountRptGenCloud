import React, { Component } from 'react';
import { BrowserRouter as Router, Route, Redirect } from "react-router-dom";
import App from './App';
import UserAdmin from './UserAdmin.js'
import Frame from './Frame.js';

class Routing extends Component {

    render() {
        return (
            <Router>
                <Route path="/">
                    <Redirect to='/app/main' />
                </Route>
                <Route path="/app" component={Frame} />
                <Route path="/app/users" component={UserAdmin} />
                <Route path="/app/main" component={App} />
            </Router>
        );
    }
}

export default Routing; 