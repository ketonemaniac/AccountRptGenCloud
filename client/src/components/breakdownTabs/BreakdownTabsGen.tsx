import * as React from "react";
import Container from '@mui/material/Container';
import { useCallback, useMemo } from "react";
import { useDropzone } from "react-dropzone";
import Endpoints from "@/api/Endpoints";
import '@/styles/breakdownTabs/BreakdownTabs.scss';

interface BreakdownTabsGenProps {

}

const BreakdownTabsGen = (props: BreakdownTabsGenProps) => {

    const onDrop = useCallback((acceptedFiles: File[]) => {
        console.log(acceptedFiles.map(file => file.name).join(', '));
        Endpoints.generateTabs(acceptedFiles[0]);
    }, []);
    
    const {getRootProps, getInputProps, isDragActive,isFocused,
        isDragAccept,
        isDragReject} = useDropzone({onDrop});


    const classsName = useMemo(() => {
        if(isFocused) {
            return 'focused';
        }
        return 'base';
    }, [isFocused,isDragAccept,isDragReject]);    
        
    return (
        <Container>
            <div>
                <div className="container">
                    <div>
                        <div><h1>Breakdown Tabs Generation</h1></div>
                    </div>
                    <div {...getRootProps({className: classsName})}>
                        <input {...getInputProps()} />
                        {
                            isDragActive ?
                            <p>Drop the files here ...</p> :
                            <p>Drag 'n' drop some files here, or click to select files</p>
                        }
                    </div>
                </div>
            </div>
        </Container>
    );

} 

export default BreakdownTabsGen;