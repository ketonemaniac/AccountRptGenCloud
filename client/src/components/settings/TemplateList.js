import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { AgGridReact } from 'ag-grid-react';
import '@/styles/settings/TemplateList.scss';
import { themeAlpine, ClientSideRowModelModule, EventApiModule, ModuleRegistry, provideGlobalGridOptions, AllCommunityModule } from 'ag-grid-community';
import Endpoints from '@/api/Endpoints.js';
import StarFillIcon from '@/assets/star-fill.svg'
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


    const apiRef = React.useRef({
      grid: undefined,
      column: undefined
    });
    const onGridReady = (params) => {
      apiRef.current.grid = params.api;
      apiRef.current.column = params.columnApi;
  
      params.api.sizeColumnsToFit();
      setGridApi(params.api);
    };

    const iconCellRenderer = params  => {
        return Boolean(params.value) ? <img src={StarFillIcon} /> : <span/>
    }
    
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
                })
                .catch(err => toast.warn(err?.response?.data?.message, {
                    autoClose : false,
                    position: "top-center"
                }));
    };

    // Register all community features
    ModuleRegistry.registerModules([AllCommunityModule, ClientSideRowModelModule, EventApiModule]);

    const columnDefs = [{sortable: true,
         filter: true,
         resizable: true,
         field: "filename",
         checkboxSelection: true
        },
        {resizable: true,
             field: "inUse",
             rowClass: "testtest",
             cellRenderer: iconCellRenderer
        }
    ];

    const rowSelection = useMemo(() => { 
        return {
            mode: 'singleRow',
            checkboxes: false,
            enableClickSelection: true,
        };
    }, []);

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
                    <div style={{ height: (rowData?.length + 1) * 50, width: '100%' }}>
                        <AgGridReact rowData={rowData} 
                                        onGridReady={onGridReady}
                                        onRowSelected={onSelectionChanged}
                                        theme={themeAlpine}
                                        columnDefs={columnDefs}
                                        rowSelection={rowSelection}
                                        >                            
                        </AgGridReact>
                    </div>
                </CardBody>
            </Card>
        </React.Fragment>
    );
};

export default TemplateList;