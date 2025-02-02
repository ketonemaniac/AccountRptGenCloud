import Endpoints from "@/api/Endpoints";
import AccountJob from "@/domain/AccountJob";
import { CellStyleModule, ClientSideRowModelModule, ColDef, ICellRendererParams, ModuleRegistry, ValidationModule } from "ag-grid-community";
import * as React from "react";
import { Download } from '@mui/icons-material';

ModuleRegistry.registerModules([
    CellStyleModule,
    ClientSideRowModelModule,
    ValidationModule /* Development Only */,
  ]);


export const useFilesList = () => {

    const [rowData, setRowData] = React.useState<AccountJob[]>([]);

    const getRowData = async (docType: string) => {
        const accountJobs : AccountJob[] = await Endpoints.listFiles(docType);
        setRowData(accountJobs);
    }

    // Column Definitions: Defines the columns to be displayed.
    const colDefs: ColDef[] = [
        { field: "filename",
            headerName: '',
            width: 50,
            cellRenderer: (params: any) => (<a href={"/api/accrptgen/file?file=" + params.value} ><Download /></a>)
        },
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