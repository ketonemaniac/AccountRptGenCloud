import React, { useState, useEffect } from 'react';
import { AgGridColumn, AgGridReact } from 'ag-grid-react';
import '../../styles/settings/TemplateList.scss';
import Endpoints from '../../api/Endpoints.js';
import {ReactComponent as StarFillIcon} from '../../assets/svg/star-fill.svg'
import { toast } from 'react-toastify';
import {
    Card, CardImg, CardText, CardBody,
    CardTitle, CardSubtitle, Button, Label, Input
  } from 'reactstrap';

const TemplateList = (props) => {
    const [gridApi, setGridApi] = useState(null);
    const [rowData, setRowData] = useState(null);
    const [selected, setSelected] = useState({});

    useEffect(() => {
        setRowData(props.rowData)   
    }, [props.rowData])    

    const onGridReady = params => {
        params.api.sizeColumnsToFit();
        params.api.setDomLayout('autoHeight')
        setGridApi(params.api);
    };

    const iconCellRenderer = params  => params.value ? <StarFillIcon /> : <span/>
    
    const onSelectionChanged = (event) => {
        if(event.api.getSelectedNodes().length === 0) {
            setSelected({})
        } else {
            event.api.getSelectedNodes().forEach(
                node => setSelected(node.data))
        }
    }

    const handleDeleteTemplate = () => {
        Endpoints.deleteTemplate(props.fileType, selected.filename)
        .then(resp => props.init())
    }
    const handleSetActiveTemplate = () => {
        Endpoints.setActiveTemplate(props.fileType, selected.filename)
        .then(resp => props.init())
    }
    const handleDownloadTemplate = () => {
        Endpoints.downloadTemplate(props.fileType + "/" + selected.filename)        
    }
    const handleAddTemplate = event => { 
            // Update the state 
            const file = event.target.files[0];
            console.log("acceptedFile=" + file.name + " size=" + file.size);
            const data = new FormData()
            data.append('file', file, file.name)
    
            Endpoints.putTemplate(props.fileType, data)
                .then(resData => {
                    toast.info("File updated: " + resData.filename);
                    props.init();
            });
    };

    return (
        <React.Fragment>
            <Card className="template-excel-card">
                <CardBody>
                    <CardTitle>{props.title}
                        <Label className="btn btn-outline-primary template-excel-add-button custom-file-upload">
                                <Input type="file" onChange={handleAddTemplate} style={{display:"none"}}></Input>
                                Add New
                        </Label>
                    </CardTitle>
                    <Button className="template-excel-action-button" color="primary"
                        disabled={selected.inUse === undefined || selected.inUse === null || selected.inUse}  
                        onClick={handleSetActiveTemplate}
                        outline>Use Template</Button>
                    <Button className="template-excel-action-button" color="primary" 
                        onClick={handleDownloadTemplate}
                        disabled={selected.inUse === undefined} outline>Download</Button>
                    <Button className="template-excel-action-button" color="danger" 
                        onClick={handleDeleteTemplate}
                        disabled={selected.inUse === undefined || selected.inUse === null || selected.inUse} outline>Delete</Button>
                    <div className="ag-theme-alpine" style={{ height: '100%', width: '100%' }}>
                        <AgGridReact rowData={rowData} 
                                        onGridReady={onGridReady}
                                        onSelectionChanged={onSelectionChanged}
                                        >
                            <AgGridColumn sortable={ true } filter={ true } resizable={true} field="filename"
                                checkboxSelection={true}
                            ></AgGridColumn>
                            <AgGridColumn resizable={true} field="inUse" rowClass="testtest"
                                cellRendererFramework={iconCellRenderer}></AgGridColumn>
                        </AgGridReact>
                    </div>
                </CardBody>
            </Card>
        </React.Fragment>
    );
};

export default TemplateList;