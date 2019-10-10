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
  CardDeck, Card, CardHeader, CardImg, CardText, CardBody,
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
    companies: [],
    fileUploadBlock: false
  }

  componentDidMount() {
    axios.get('/version')
      .catch(error => { console.log(error); throw Error(error) })
      .then(res => this.setState({ response: res.data.version }));
    this.getProgress();
  }

  getProgress = () => {
    return axios.get('/listFiles')
      .catch(error => { console.log(error); throw Error(error) })
      .then(res => { 
        var inProgress = res.data
        .filter(company => company.status != null)
        // .filter(company => company.status != "EMAIL_SENT") ;
        this.setState({ companies: inProgress }) 
      });

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
            console.log("loaded" + (ProgressEvent.loaded / ProgressEvent.total * 100));
            /*this.setState({
              loaded: (ProgressEvent.loaded / ProgressEvent.total*100),
            })*/
          },
        })
        .then(res => {

          this.setState(state => {
            const companies = [ {
              company: res.data.company,
              filename: res.data.filename,
              status: "PRELOADED"
            }, ...state.companies ];
            return {
              companies : companies,
              fileUploadBlock: false

            };
          });

        })
    }
    );
    this.setState({ fileUploadBlock: true });
  }

  handleStartGeneration = (event) => {
    event.preventDefault();
    const data = new FormData(event.target);

    axios
      .post("startGeneration", data)
      .then(res => this.getProgress());
  }


  handleDownload(company) {
    console.log("company=" + company.filename);
    // ajax doesn't handle file downloads elegantly
    var req = new XMLHttpRequest();
    req.open("POST", "/downloadFile", true);
    req.setRequestHeader("Content-Type", "application/json");
    req.responseType = "blob";
    req.onreadystatechange = function () {
        if (req.readyState === 4 && req.status === 200) {
            // test for IE
            if (typeof window.navigator.msSaveBlob === 'function') {
                window.navigator.msSaveBlob(req.response, company.filename + ".zip");
            } else {
                var blob = req.response;
                var link = document.createElement('a');
                link.href = window.URL.createObjectURL(blob);
                link.download = company.filename + ".zip";
                // append the link to the document body
                document.body.appendChild(link);
                link.click();
                link.remove();// you need to remove that elelment which is created before
            }
        }
    };
    req.send(JSON.stringify({ "filename": company.filename }));
  }

  render() {
    const dropzoneRef = React.createRef();
    const showAddDetail = this.state.companies.length > 0;
    return (
      <div>
        <AppHeader isAdmin={this.state.isAdmin} setAdmin={this.setAdmin} />
        <Dropzone ref={dropzoneRef} onDrop={this.onDrop.bind(this)}>
          {({ getRootProps, getInputProps, isDragActive }) => {
            return (
              <Jumbotron style={{
                paddingTop: showAddDetail ? "5%" : "15%",
                paddingBottom: showAddDetail ? "5%" : "15%"
              }}
                fluid {...getRootProps({ onClick: evt => evt.preventDefault() })}>
                <input {...getInputProps()} />
                <Container>
                  <h1 className="display-3">Instant Report Generation</h1>
                  <div>
                    <span className="lead">
                      Drag your input file over this area, or click to select the file from the file picker below
              </span>
                    <span className="lead" ><p />
                      <Button color="primary" className="bigButton" onClick={() => dropzoneRef.current.open()}
                        loading={this.state.fileUploadBlock}>
                        Select File
                    </Button>

                    </span>
                  </div>
                </Container>
              </Jumbotron>
            )
          }}
        </Dropzone>
        <CardDeck className="px-5">
        {this.state.companies
        .map((c, i) => {
          return (
              <Card body>
                <CardHeader>{c.company}</CardHeader>
                <CardBody>
                <Form className="form" onSubmit={this.handleStartGeneration}>
                  <Container>
                    <Row>
                    <Col xs="12" lg="10">
                    <Container>
                      <Input type="hidden" name="filename" value={c.filename} />
                      <Input type="hidden" name="company" value={c.company} />
                      <FormGroup row>
                        <Label sm={3} for="referredBy">Referrer <span className="text-muted">(Optional)</span></Label>
                        <Col sm={9}>
                          <Input type="text" name="referredBy" id="referredBy" placeholder="The referrer's name to appear in email" />
                          </Col>
                      </FormGroup>
                      <FormGroup row>
                        <Label sm={3} for="status">Status</Label>
                        <Col sm={9}>
                          <Input className="input-text-borderless" type="text" disabled name="status" id="status" value={c.status} />
                          </Col>
                      </FormGroup>
                      <FormGroup row>
                        <Label sm={3} for="status">Generation time</Label>
                        <Col sm={9}>
                          <Input className="input-text-borderless" type="text" disabled name="generationTime" id="generationTime" value={c.generationTime} />
                          </Col>
                      </FormGroup>
                    </Container>
                    </Col>
                    <Col xs="2">
                      <Button style={{display: c.status == "PRELOADED" ? "block" : "none"}} 
                          type="submit" color="success" className="generate-button mr-2">Generate</Button>
                          <Button style={{display: c.status == "EMAIL_SENT" ? "block" : "none"}}
                          onClick={this.handleDownload.bind(this, c)}
                          color="secondary" className="generate-button mr-2">Download</Button>
                    </Col>
                        </Row>
                  </Container>
                  </Form>
                </CardBody>
              </Card>

          )
        })}
      </CardDeck>

        <Container className="footer text-center">
          <span className="text-muted"> © Ketone Maniac @ 2019</span>
        </Container>
      </div>
    );
  }
}


export default App;
