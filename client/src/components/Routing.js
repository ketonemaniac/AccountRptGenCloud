import React, { Component } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from "react-router-dom";
import RptGen from './home/RptGen';
import UserAdmin from './useradmin/UserAdmin.js'
import Settings from './settings/Settings'
import Frame from './Frame.js';
import Login from './login/Login';


const Routing = () => {

    return (
            <Router>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/app" element={<Frame />} >
                        <Route path="users" element={<UserAdmin />} />
                        <Route path="breakdown" element={<RptGen docType='BreakdownTabs' docTypeString='Schedules Breakdown' title='Schedule Breakdown Generation'/>} />
                        <Route path="afsgen" element={<RptGen docType='GenerateAFS' docTypeString='AFS Sheets' title='AFS Sheets Generation'/>} />
                        <Route path="main" element={<RptGen docType='AccountRpt' docTypeString='Accounting Report' title='Accounting Report Generation'/>} />
                        <Route path="excelExtract" element={<RptGen docType='ExcelExtract' docTypeString='DBiz Funding Excel Extract' title='DBiz Funding Excel Extract Generation'/>} />
                        <Route path="settings" element={<Settings />} />
                    </Route>
                    <Route path="/" element={<Navigate to="/app/main" replace />} />
                </Routes>
            </Router>
    );
}

export default Routing; 