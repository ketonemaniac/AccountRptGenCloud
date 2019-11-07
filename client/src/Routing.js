import React, { Component } from 'react';
import { BrowserRouter as Router, Route } from "react-router-dom";
import App from './App';

class Routing extends Component {


    render() {
        return (
            <Router>
                <Route path="/main" component={App} />
                <Route component={App} />
            </Router>
        );
    }
}

export default Routing; 