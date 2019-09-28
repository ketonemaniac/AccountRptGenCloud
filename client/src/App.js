import React, { Component } from 'react';
import logo from './logo.svg';
import 'bootstrap/dist/css/bootstrap.min.css';
import AppHeader from './AppHeader.js';
import Progress from './Progress.js';
import Done from './Done.js';
import './App.css';
import {
  Container, Row, Col,
  Jumbotron,
  Card, CardHeader, CardImg, CardText, CardBody,
  CardTitle, CardSubtitle, Media,
  Form, FormGroup, Label, Input
} from 'reactstrap';
import Button from 'reactstrap-button-loader';
import { faRedo } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import Moment from 'react-moment';
import Dropzone from 'react-dropzone';
import axios from 'axios';

class App extends Component {

  state = {
    isAdmin: false,
    date: new Date(),
    companies : [],
    progress: "init"
  }

  componentDidMount() {
    axios.get('/version')
      .catch(error => {console.log(error); throw Error(error)})
      .then(res => this.setState({ response: res.data.version }));
    this.getProgress();
  }

  getProgress = () => {
    return axios.get('/listFiles')
    .catch(error => {console.log(error); throw Error(error)})
    .then(res => {this.setState({ companies: res.data })});

  }


  setAdmin = (admin) => {
    this.setState({ isAdmin: admin });
  }



  onDrop = (acceptedFiles, rejectedFiles) => {
    acceptedFiles.map(file => {
      console.log("acceptedFile=" + file.name + " size=" + file.size);
      const data = new FormData()
      data.append('file', file, file.name)
  
      axios
        .post("uploadFile", data, {
          onUploadProgress: ProgressEvent => {
            console.log("loaded" + (ProgressEvent.loaded / ProgressEvent.total*100));
            /*this.setState({
              loaded: (ProgressEvent.loaded / ProgressEvent.total*100),
            })*/
          },
        })
        .then(res => {
          console.log()
          this.setState({progress: "loaded",
                  loadedFile: res.data.company});
        })
    }
    );
    this.setState({progress: "uploading"});
  }

  render() {
    const dropzoneRef = React.createRef();
    const showFileButton = this.state.progress == "init" || this.state.progress == "uploading";
    const showAddDetail = this.state.progress == "loaded";
    return (
      <div>
        <AppHeader isAdmin={this.state.isAdmin} setAdmin={this.setAdmin} />
        <Dropzone ref={dropzoneRef} onDrop={this.onDrop.bind(this)}>
          {({getRootProps, getInputProps, isDragActive}) => { 
            return (
            <Jumbotron fluid {...getRootProps({onClick: evt => evt.preventDefault()})}>
              <input {...getInputProps()} />
              <Container>
                <h1 className="display-3">Instant Report Generation</h1>
                <div style={{display : showFileButton ? "block" : "none"}}>
                <p className="lead">
                  Drag your input file over this area, or click to select the file from the file picker below
              </p>
                <p className="lead">
                    <Button color="primary" className="bigButton" onClick={() => dropzoneRef.current.open()}
                      loading={this.state.progress == "uploading"}>
                      Select File
                    </Button>
                </p>
              </div>

                <p className="lead" style={{display: showAddDetail ? "block" : "none"}} >
                  <Container className="py-5 loadedForm">
                    <Row>
                    <Col><h3>{this.state.loadedFile}</h3></Col>
                    </Row>
                    <Row>
                    <Col>Add additional details before generating the final report</Col>
                    </Row>
                  <Form className="form">
                    <FormGroup row>
                      <Label for="referrer" sm={2}>Referrer</Label>
                      <Col sm={10}>
                        <Input type="text" name="referrerText" id="referrer" placeholder="The referrer's name to appear in email" />
                      </Col>
                    </FormGroup>
                    <FormGroup row>
                      <Col sm={12}>
                      <Button color="success" className="mr-2">Generate</Button><Button color="danger">Start Over</Button>
                      </Col>
                    </FormGroup>
                  </Form>

                  </Container>
                
                </p>
              </Container>
            </Jumbotron>
          )
          }}
        </Dropzone>
        <Container className="footer text-center">
          <span className="text-muted"> © Ketone Maniac @ 2019</span>
        </Container>        
      </div>
    );
  }
}


export default App;
