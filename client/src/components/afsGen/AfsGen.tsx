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
import FilesList from "@/components/shared/FilesList";
import ErrorModal from "@/components/shared/ErrorModal";


// Register all Community features
ModuleRegistry.registerModules([AllCommunityModule]);

interface AfsGenProps {

}

const AfsGen = (props: AfsGenProps) => {

    const [generating, setGenerating] = React.useState(false);
    const [isModalOpen, setIsModalOpen] = React.useState(false);
    const [modalMsg, setModalMsg] = React.useState("");


    const openModal = (msg:string) => {
        setModalMsg(msg);
        setIsModalOpen(true);
    }
    const closeModal = () => setIsModalOpen(false);

    const onDrop = useCallback(async (acceptedFiles: File[]) => {
        console.log(acceptedFiles.map(file => file.name).join(', '));
        setGenerating(true);
        try {
            await Endpoints.generateTabs(acceptedFiles[0], 'GenerateAFS')
            .then(res => {
                toast.info("AFS Sheets generated");
            });
        } catch (error) {
            toast.error("AFS Sheets Generation error!" + error);
        }
        setGenerating(false);
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
        <>
            <ErrorModal isOpen={isModalOpen} closeModal={closeModal} msg={modalMsg} />
            <div className="container schedule-root" >
            <div>
                <div><h2>AFS Sheets Generation</h2></div>
            </div>
            <div className="schedule-breakdown">
                <div {...getRootProps({className: dropzoneClassName})}>
                    <input {...getInputProps()} />
                    {!generating && 
                        (isDragActive ?
                        <p>Drop the files here ...</p> :
                        <p>Drag 'n' drop some files here, or click to select files</p>)}
                    {generating && (<>
                        <h1>Generating...</h1>
                        <ClipLoader loading={generating} color="#bdbdbd" />
                    </>)}
                </div>
                <div className="schedule-history">
                    <FilesList docType={'GenerateAFS'} loading={generating} openModal={openModal}/>
                </div>
            </div>
        </div>
        </>
    );

} 

export default AfsGen;