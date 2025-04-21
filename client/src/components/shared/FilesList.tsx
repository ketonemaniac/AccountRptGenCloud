import { ColDef, themeBalham } from "ag-grid-community";
import { AgGridReact } from "ag-grid-react";
import * as React from "react";
import { useFilesList } from "./useFilesList";

interface FilesListProps {
    docType: string;
    loading: boolean;
    openModal?: (msg: string) => void;
}


const FilesList = (props: FilesListProps) => {

    const {rowData,getRowData, colDefs} = useFilesList(props.openModal);

    React.useEffect(() => {
        getRowData(props.docType);
    } ,[props.loading, props.docType]);

    return (
        <>
            <AgGridReact theme={themeBalham} rowData={rowData} columnDefs={colDefs}  />
        </>
    )
}

export default FilesList;