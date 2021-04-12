import { Container ,Button, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import Endpoints from '../../api/Endpoints.js';
import { toast } from 'react-toastify';
import TemplateList from './TemplateList.js'
import React, { useState, useEffect } from 'react';

const Settings = (props) => {

    const [mailString, setMailString] = useState("default");
    const [allDocsData, setAllDocsData] = useState([]);
    const [auditPrgData, setAuditPrgData] = useState([]);

    async function init() {
        const resp = await Endpoints.getFileList()
        const configs = await Endpoints.getAllSettings()

        setAllDocsData(resp.alldocs.map(filename => ({"filename" : filename, "inUse" : filename == configs.allDocs})))
        setAuditPrgData(resp.auditprg.map(filename => ({"filename" : filename, "inUse" : filename == configs.auditPrg})))
        setMailString(configs.sendTo)
    }

    useEffect(() => {
        init();    
    }, [])

    const handleMailStringChange = (event) => {
        setMailString(event.target.value);
    }

    const submitMailChange = (event) => {
        if(mailString == null) {
            toast.error("Mailing string has not been changed")
            return;
        }
        const toChange = { "mail.sendto" : mailString };
        Endpoints.saveSettings(toChange)
        .then(res => {
            toast.info("Mailing List updated");
        });
    }

    return (
        <React.Fragment>
            <Container className="themed-container">
            
                <h1>Settings</h1>

                <Form>
                    <FormGroup className="mb-2 mt-5">
                        <Label for="mailingList" className="mr-2">Mandatory mailing list (colon seperated)</Label>
                        <Input type="text" name="text" id="mailingList" value={mailString} onChange={handleMailStringChange} />
                    </FormGroup>
                    <Button color="primary" onClick={submitMailChange}>Update</Button>
                </Form>             
                <TemplateList rowData={allDocsData} init={init} fileType={"allDocs"} title={"Template Excel"}/>
                <TemplateList rowData={auditPrgData} init={init} fileType={"auditprg"} title={"Audit Programme Excel"}/>
            </Container>
        </React.Fragment>
    );

}

export default Settings;