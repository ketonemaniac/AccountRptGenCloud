import React, { Component } from 'react';
import { Container ,Button, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import Endpoints from '../../api/Endpoints.js';
import { toast } from 'react-toastify';

class Settings extends Component {

    state = {
        settings : [],
        selectedFile : null,
        mailString : null
    }

    componentDidMount() {
        Endpoints.getAllSettings().then(data => this.setState({ settings: data }));;
    }

    // On file select (from the pop up) 
    onFileChange = event => { 
        // Update the state 
        const file = event.target.files[0];
        this.setState({ selectedFile: event.target.files[0] }); 
        console.log("acceptedFile=" + file.name + " size=" + file.size);
        const data = new FormData()
        data.append('file', file, file.name)

        Endpoints.putTemplate(data)
            .then(resData => {
                toast.info("File updated: " + resData.filename);
                this.setState(oldState => {
                    console.log(oldState.settings);
                    oldState.settings['xlsx.template.name'] = resData.filename;
                    return {
                        settings : oldState.settings
                    }
                })
          });
    };

    handleDownloadTemplate() {
        Endpoints.downloadTemplate(this.state.settings['xlsx.template.name'])
    }

    handleMailChange(event) {
        this.setState({
            "mailString" : event.target.value
        });            
    }

    submitMailChange() {
        if(this.state.mailString == null) {
            toast.error("Mailing string has not been changed")
            return;
        }
        const toChange = { "mail.sendto" : this.state.mailString };
        Endpoints.saveSettings(toChange)
        .then(res => {
            toast.info("Mailing List updated");
        });
    }

    render() {
        return (
            <main>
                <Container className="themed-container">
                
                    <h1>Settings</h1>

                    <Form>
                        <FormGroup className="mb-2 mb-sm-2">
                            <Label for="template" className="mr-2">Template</Label>
                            <Input type="text" className="formControl" disabled="disabled" name="template" id="template" defaultValue={this.state.settings["xlsx.template.name"]}></Input>
                            <Label className="btn btn-primary mr-3 mt-1 custom-file-upload">
                                <Input type="file" onChange={this.onFileChange} style={{display:"none"}}></Input>
                                Upload New
                            </Label>
                            <Button className="mr-3 mt-1" onClick={this.handleDownloadTemplate.bind(this)}>Download</Button>
                        </FormGroup>
                    </Form>
                    <Form>
                        <FormGroup className="mb-2 mt-5">
                            <Label for="mailingList" className="mr-2">Mandatory mailing list (colon seperated)</Label>
                            <Input type="text" name="text" id="mailingList" defaultValue={this.state.settings["mail.sendto"]}
                                onChange={this.handleMailChange.bind(this)}
                            />
                        </FormGroup>
                        <Button color="primary" onClick={this.submitMailChange.bind(this)}>Update</Button>
                    </Form>             
                    
                </Container>
                
            </main>
        );
    }
}

export default Settings;