import Endpoints from "@/api/Endpoints";
import AccountJob from "@/domain/AccountJob";
import { CellStyleModule, ClientSideRowModelModule, ColDef, ICellRendererParams, ModuleRegistry, ValidationModule } from "ag-grid-community";
import * as React from "react";
import { Download } from '@mui/icons-material';


export const useFilesList = (openModal: (msg: string) => void) => {

    const [rowData, setRowData] = React.useState<AccountJob[]>([]);

    const getRowData = async (docType: string) => {
        const accountJobs : AccountJob[] = await Endpoints.listFiles(docType);
        setRowData(accountJobs);
    }

    const CustomCellRenderer = (props: ICellRendererParams) => {
        if(props.data.errorMsg) {
            return <div onClick={openModal.bind(null, props.data.errorMsg)} className="error-cell">{props.value}</div>    
        }else {
            return <div>{props.value}</div>
        }
        ;
      };

    // Column Definitions: Defines the columns to be displayed.
    const colDefs: ColDef[] = [
        { field: "filename",
            headerName: '',
            width: 50,
            cellRenderer: (params: any) => (<a href={"/api/accrptgen/file?file=" + encodeURIComponent(params.value)} ><Download /></a>)
        },
        { field: "company", sortable: true, filter: true, flex: 1 },        
        { field: "period", sortable: true, filter: true },
        { field: "filename", sortable: true, filter: true, flex: 1  },
        { field: "generationTime", sortable: true, filter: true },
        { field: "status", sortable: true, filter: true, 
            cellRenderer: CustomCellRenderer
        },
        { field: "referredBy", sortable: true, filter: true },
        { field: "submittedBy", sortable: true, filter: true },
    ];
    
    return {
        rowData, getRowData, colDefs
    }

}