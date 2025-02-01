import Endpoints from "@/api/Endpoints";
import AccountJob from "@/domain/AccountJob";
import { ColDef } from "ag-grid-community";
import * as React from "react";

export const useFilesList = () => {

    const [rowData, setRowData] = React.useState<AccountJob[]>([]);

    const getRowData = async (docType: string) => {
        const accountJobs : AccountJob[] = await Endpoints.listFiles(docType);
        setRowData(accountJobs);
    }


    // const [rowData, setRowData] = React.useState([
    //     { make: "Tesla", model: "Model Y", price: 64950, electric: true },
    //     { make: "Ford", model: "F-Series", price: 33850, electric: false },
    //     { make: "Toyota", model: "Corolla", price: 29600, electric: false },
    // ]);

    // Column Definitions: Defines the columns to be displayed.
    const colDefs: ColDef[] = [
        { field: "company", sortable: true, filter: true },        
        { field: "filename", sortable: true, filter: true },
        { field: "generationTime", sortable: true, filter: true },
        { field: "status", sortable: true, filter: true },
        { field: "referredBy", sortable: true, filter: true },
        { field: "submittedBy", sortable: true, filter: true },
        { field: "period", sortable: true, filter: true },
        { field: "docType", sortable: true, filter: true }
    ];
    
    return {
        rowData, getRowData, colDefs
    }

}