import * as React from "react";
import Container from '@mui/material/Container';
import { useCallback, useMemo } from "react";
import { useDropzone } from "react-dropzone";
import { toast } from 'react-toastify';
import Endpoints from "@/api/Endpoints";
import '@/styles/breakdownTabs/BreakdownTabs.scss';
import { ClipLoader } from 'react-spinners';
import { AgGridReact } from 'ag-grid-react';
import { AllCommunityModule, ColDef, ModuleRegistry } from 'ag-grid-community'; 
import FilesList from "../shared/FilesList";

// Register all Community features
ModuleRegistry.registerModules([AllCommunityModule]);

interface BreakdownTabsGenProps {

}

const BreakdownTabsGen = (props: BreakdownTabsGenProps) => {

    const [generatingTabs, setGeneratingTabs] = React.useState(false);

    const onDrop = useCallback(async (acceptedFiles: File[]) => {
        console.log(acceptedFiles.map(file => file.name).join(', '));
        setGeneratingTabs(true);
        await Endpoints.generateTabs(acceptedFiles[0])
                .then(res => {
                    toast.info("Schedules Breakdown updated");
                });
        setGeneratingTabs(false);
    }, []);
    
    const {getRootProps, getInputProps, isDragActive,isFocused,
        isDragAccept,
        isDragReject} = useDropzone({onDrop});


    const dropzoneClassName = useMemo(() => {
        if(isFocused) {
            return 'dropzone-canvas-focused';
        }
        return 'dropzone-canvas-base';
    }, [isFocused,isDragAccept,isDragReject]);    
        
    return (
        <div className="container schedule-root" >
            <div>
                <div><h2>Schedule Breakdown Generation</h2></div>
            </div>
            <div className="schedule-breakdown">
                <div {...getRootProps({className: dropzoneClassName})}>
                    <input {...getInputProps()} />
                    {!generatingTabs && 
                        (isDragActive ?
                        <p>Drop the files here ...</p> :
                        <p>Drag 'n' drop some files here, or click to select files</p>)}
                    {generatingTabs && (<>
                        <h1>Generating...</h1>
                        <ClipLoader loading={generatingTabs} color="#bdbdbd" />
                    </>)}
                </div>
                <div className="schedule-history">
                    <FilesList docType={'BreakdownTabs'} loading={generatingTabs}/>
                </div>
            </div>
        </div>
    );

} 

export default BreakdownTabsGen;