import * as React from "react";
import Container from '@mui/material/Container';
import { useCallback, useMemo } from "react";
import { useDropzone } from "react-dropzone";
import { toast } from 'react-toastify';
import Endpoints from "@/api/Endpoints";
import '@/styles/home/RptGen.scss';
import { ClipLoader } from 'react-spinners';
import FilesList from "@/components/shared/FilesList";
import ErrorModal from "@/components/shared/ErrorModal";

interface RptGenProps {
    title: string;
    docType: string;
    docTypeString: string;
}

const RptGen = (props: RptGenProps) => {

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
            await Endpoints.generate(acceptedFiles[0], props.docType)
            .then(res => {
                toast.info(props.docTypeString + " generated");
            });
        } catch (error) {
            toast.error(props.docTypeString + " Generation error!" + error, {
                autoClose: false
            });
        }
        setGenerating(false);
    }, [props.docType]);
    
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
            <Container className="schedule-root" maxWidth={false} >
            <div>
                <div><h2>{props.title}</h2></div>
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
                    <FilesList docType={props.docType} loading={generating} openModal={openModal}/>
                </div>
            </div>
        </Container>
        </>
    );

} 

export default RptGen;