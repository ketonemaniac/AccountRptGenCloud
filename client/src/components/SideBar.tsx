import * as React from 'react';
import { useCallback } from 'react';
import { ListGroup, ListGroupItem } from 'reactstrap';
import { Link } from "react-router-dom";
import '../styles/SideBar.css';

interface SideBarProps {
    isSideBarCollapsed: boolean;
    toggleSidebar: () => void;
    isAdmin: boolean;
}

const SideBar = (props: SideBarProps) => {

    const linkOnclick = useCallback(() => {
        props.toggleSidebar();
    }, []);

    return (
        <div className={props.isSideBarCollapsed ? "width show" : "width"} id="sidebar">
            <ListGroup className="sidebar-list text-nowrap">
                <Link onClick={linkOnclick} to="/app/main"><ListGroupItem>Accounting Report Generation</ListGroupItem></Link>
                <Link onClick={linkOnclick} to="/app/excelExtract"><ListGroupItem>DBiz Funding Excel</ListGroupItem></Link>
                <Link onClick={linkOnclick} to="/app/breakdown"><ListGroupItem>Breakdown Tabs Generation</ListGroupItem></Link>
                <Link onClick={linkOnclick} to="/app/afsgen"><ListGroupItem>AFS Tabs Generation</ListGroupItem></Link>
                {props.isAdmin && <Link onClick={linkOnclick} to="/app/users"><ListGroupItem>Users</ListGroupItem></Link>}
                {props.isAdmin && <Link onClick={linkOnclick} to="/app/settings"><ListGroupItem>Settings</ListGroupItem></Link>}
            </ListGroup>
        </div>
    );

}

export default SideBar;
