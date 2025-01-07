import React, { Component } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from "react-router-dom";
import App from './home/App';
import UserAdmin from './useradmin/UserAdmin.js'
import Settings from './settings/Settings'
import Frame from './Frame.js';
import Login from './login/Login';
import BreakdownTabsGen from './breakdownTabs/BreakdownTabsGen';


const Routing = () => {

    return (
            <Router>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/app" element={<Frame />} >
                        <Route path="users" element={<UserAdmin />} />
                        <Route path="breakdown" element={<BreakdownTabsGen />} />
                        <Route path="settings" element={<Settings />} />
                        <Route path="main" element={<App />} />
                    </Route>
                    <Route path="/" element={<Navigate to="/app/main" replace />} />
                </Routes>
            </Router>
    );
}

export default Routing; 