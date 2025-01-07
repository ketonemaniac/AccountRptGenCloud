import * as React from 'react';
import { Container ,Button, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import Endpoints from '../../api/Endpoints';
import { toast } from 'react-toastify';
import TemplateList from './TemplateList.js'
import { useState, useEffect } from 'react';
import '../../styles/settings/Settings.scss';
import {FileList} from '../../domain/settings/FileList';

interface SettingFile {
    filename: string;
    inUse: boolean;
}


const Settings = (props: any) => {

    const [mailString, setMailString] = useState("default");
    const [allDocsData, setAllDocsData] = useState<SettingFile[]>([]);
    const [auditPrgData, setAuditPrgData] = useState<SettingFile[]>([]);
    const [dBizFundingData, setDBizFundingData] = useState<SettingFile[]>([]);
    const [breakdownTabsData, setBreakdownTabsData] = useState<SettingFile[]>([]);

    async function init() {
        const resp: FileList = await Endpoints.getFileList()
        const configs = await Endpoints.getAllSettings()

        setAllDocsData(resp.allDocs.map(filename => ({filename: filename, inUse: filename == configs.allDocs})))
        setAuditPrgData(resp.auditPrg.map(filename => ({filename : filename, inUse : filename == configs.auditPrg})))
        setDBizFundingData(resp.dBizFunding.map(filename => ({filename: filename, inUse : filename == configs?.dBizFunding})))
        setBreakdownTabsData(resp?.breakdownTabs?.map(filename => ({filename : filename, inUse : filename == configs?.breakdownTabs})))
        setMailString(configs.sendTo)
    }

    useEffect(() => {
        init();    
    }, [])

    const handleMailStringChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setMailString(event.target.value);
    }

    const submitMailChange = (event: React.MouseEvent<HTMLButtonElement>) => {
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
                <TemplateList rowData={breakdownTabsData} init={init} fileType={"breakdownTabs"} title={"Breakdown Tabs Excel"}/>
                <TemplateList rowData={allDocsData} init={init} fileType={"allDocs"} title={"Template Excel"}/>
                <TemplateList rowData={auditPrgData} init={init} fileType={"auditPrg"} title={"Audit Programme Excel"}/>
                <TemplateList rowData={dBizFundingData} init={init} fileType={"dBizFunding"} title={"Funding Excel"}/>
            </Container>
        </React.Fragment>
    );

}

export default Settings;