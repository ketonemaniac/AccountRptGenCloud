import { Container ,Button, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import Endpoints from '../../api/Endpoints.js';
import { toast } from 'react-toastify';
import TemplateList from './TemplateList.js'
import React, { useState, useEffect } from 'react';
import '../../styles/settings/Settings.scss';

const Settings = (props) => {

    const [mailString, setMailString] = useState("default");
    const [allDocsData, setAllDocsData] = useState([]);
    const [auditPrgData, setAuditPrgData] = useState([]);
    const [dBizFundingData, setDBizFundingData] = useState([]);

    async function init() {
        const resp = await Endpoints.getFileList()
        const configs = await Endpoints.getAllSettings()

        setAllDocsData(resp.allDocs.map(filename => ({"filename" : filename, "inUse" : filename == configs.allDocs})))
        setAuditPrgData(resp.auditPrg.map(filename => ({"filename" : filename, "inUse" : filename == configs.auditPrg})))
        setDBizFundingData(resp?.dBizFunding?.map(filename => ({"filename" : filename, "inUse" : filename == configs?.dBizFunding})))
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
            <Container className="settings">
            
                <h1>Settings</h1>

                <Form>
                    <FormGroup className="mb-2 mt-5">
                        <Label for="mailingList" className="mr-2">Mandatory mailing list (colon seperated)</Label>
                        <Input type="text" name="text" id="mailingList" value={mailString} onChange={handleMailStringChange} />
                    </FormGroup>
                    <Button color="primary" onClick={submitMailChange}>Update</Button>
                </Form>             
                <TemplateList rowData={allDocsData} init={init} fileType={"allDocs"} title={"Template Excel"}/>
                <TemplateList rowData={auditPrgData} init={init} fileType={"auditPrg"} title={"Audit Programme Excel"}/>
                <TemplateList rowData={dBizFundingData} init={init} fileType={"dBizFunding"} title={"Funding Excel"}/>
            </Container>
        </React.Fragment>
    );

}

export default Settings;