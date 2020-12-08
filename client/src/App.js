import React, { Component, useEffect } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';
import {
  Container, Row, Col,
  Jumbotron,
  CardDeck, Card, CardHeader, CardImg, CardText, CardBody,
  CardTitle, CardSubtitle, Media,
  Form, FormGroup, Label, Input,
  Alert, Modal, ModalHeader, ModalBody, ModalFooter
} from 'reactstrap';
import Button from 'reactstrap-button-loader';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faRedo } from '@fortawesome/free-solid-svg-icons'
import Moment from 'react-moment';
import Dropzone from 'react-dropzone';
import axios from 'axios';
import moment from 'moment';
import { CSSTransition } from 'react-transition-group';


class App extends Component {

  state = {
    isAdmin: false,
    date: new Date(),
    companies: [],
    fileUploadBlock: false,
    uploadError: null,
    isUploadErrorModalOpen: false
  };
  timer = null;

  // INIT ======================
  componentDidMount() {
    this.getProgress();
  }

  getProgress() {
    return axios.get('/listFiles')
      .catch(error => { console.log(error); throw Error(error) })
      .then(res => {
        var inProgress = res.data.filter(company => company.status != null);
        this.setState({ companies: inProgress })
      });

  }

  componentDidUpdate() {
    if (this.timer != null) {
      clearTimeout(this.timer);
    }
    if (this.state.companies.filter(c => (c.status == "PENDING" || c.status == "GENERATING")).length > 0) {
      this.timer = setTimeout(() => {
        this.getProgress();
      }, 10000);
    }
  }


  setAdmin = (admin) => {
    this.setState({ isAdmin: admin });
  }


  // ON DROP ======================
  onDrop = (acceptedFiles, rejectedFiles) => {
    acceptedFiles.map(file => {
      console.log("acceptedFile=" + file.name + " size=" + file.size);
      const data = new FormData()
      data.append('file', file, file.name)

      axios
        .post("/uploadFile", data, {
          onUploadProgress: ProgressEvent => {
            console.log("loaded" + (ProgressEvent.loaded / ProgressEvent.total * 100));
            /*this.setState({
              loaded: (ProgressEvent.loaded / ProgressEvent.total*100),
            })*/
          }
        })
        .then(res => {

          this.setState(state => {
            const companies = [{
              company: res.data.company,
              filename: res.data.filename,
              status: "PRELOADED",
              id: res.data.id
            }, ...state.companies];
            return {
              companies: companies,
              fileUploadBlock: false
            };
          });
        })
        .catch(e => {
          this.setState({
            uploadError: e.response.status + " " + JSON.stringify(e.response.data),
            fileUploadBlock: false,
            isModalOpen: true
          }
          )
        }
        )

    }
    );
    this.setState({ fileUploadBlock: true });
  }

  uploadErrorModalAlert(error) {
    return (
      <div>
        <Modal isOpen={this.state.isUploadErrorModalOpen}>
          <ModalHeader toggle={this.toggleUploadErrorModal.bind(this)}>Error uploading file</ModalHeader>
          <ModalBody>
            Some of your inputs may not be correct. Please check the file format before uploading again.
            <p /><span className="text-muted">Error = {error}</span>
          </ModalBody>
        </Modal>
      </div>
    )
  }

  toggleUploadErrorModal() {
    this.setState((oldState) => {
      return { isModalOpen: !oldState.isUploadErrorModalOpen }
    });
  }

  // ON GENERATE ===========================
  handleStartGeneration = (event) => {
    event.preventDefault();
    const data = new FormData(event.target);

    axios
      .post("/startGeneration", data)
      .then(res => this.getProgress());
  }

  // FINISHED ===============================
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
        <main>
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
          <CSSTransition transitionName="rpt-generation">
            {this.renderGenerating()}
          </CSSTransition>
          <CardDeck className="px-5">
            {this.state.companies
              .map((c, i) => {
                return (
                  <Card key={c.id} body outline color={c.status == "PRELOADED" ? "warning" : "default"}>
                    <CardHeader>{c.company}</CardHeader>
                    <CardBody>
                      <Form className="form" onSubmit={this.handleStartGeneration}>
                        <Container>
                          <Row>
                            <Col xs="12" lg="10">
                              <Container>
                                <Input key={c.id + "-id"} type="hidden" name="id" value={c.id} />
                                <Input key={c.id + "-filename"} type="hidden" name="filename" value={c.filename} />
                                <Input key={c.id + "-company"} type="hidden" name="company" value={c.company} />
                                <Input key={c.id + "-submittedBy"} type="hidden" name="submittedBy" value={c.submittedBy} />
                                <FormGroup row>
                                  <Label sm={3} for="referredBy">Referrer
                          <span style={{ "display": c.status == "PRELOADED" ? "block" : "none" }} className="text-muted">(Optional)</span></Label>
                                  {this.renderReferredBy(c)}
                                </FormGroup>
                                <FormGroup row>
                                  <Label sm={3} for="status">Status</Label>
                                  <Col sm={9}>
                                    <Input key={c.generationTime + "-status"}
                                      className="input-text-borderless" type="text" disabled name="status" id="status" value={c.status} />
                                  </Col>
                                </FormGroup>
                                <FormGroup row>
                                  <Label sm={3} for="generationTime">Generation time</Label>
                                  {this.renderGenerationTime(c)}
                                </FormGroup>
                              </Container>
                            </Col>
                            <Col xs="2">
                              {this.renderButton(c)}
                            </Col>
                          </Row>
                        </Container>
                      </Form>
                    </CardBody>
                  </Card>

                )
              })}
          </CardDeck>
        </main>
        <Container className="footer text-center">
          <span className="text-muted"> © Ketone Maniac @ 2020</span>          
        </Container>


        {this.uploadErrorModalAlert(this.state.uploadError)}
      </div>
    );
  }


  renderGenerating() {
    if (this.state.companies.filter(c => (c.status == "PENDING" || c.status == "GENERATING")).length > 0) {
      return (
        <Alert color="warning" className="px-5">
          A report generation is under progress. Please click to refresh
          <Button color="warning" className="mx-2" onClick={this.getProgress.bind(this)}>
            <FontAwesomeIcon icon={faRedo} /></Button>
        </Alert>
      )
    }
    return <span />;
  }

  renderReferredBy(company) {
    switch (company.status) {
      case "PRELOADED":
        return (
          <Col sm={9}>
            <Input key={company.id + "-referredBy"}
              type="text" name="referredBy" id="referredBy" placeholder="The referrer's name to appear in email" />
          </Col>
        )
      default:
        return (
          <Col sm={9}>
            <Input key={company.id + "-referredBy"} className="input-text-borderless"
              disabled="disabled"
              type="text" name="referredBy" id="referredBy"
              value={company.referredBy} />
          </Col>
        )
    }
  }

  renderGenerationTime(company) {
    moment.relativeTimeThreshold('ss', 0);
    switch (company.status) {
      case "PENDING":
      case "GENERATING":
        return (
          <Col sm={9}>
            <Input key={company.id + "-generationTime"}
              className="input-text-borderless" type="text" disabled name="generationTime" id="generationTime"
              value={company.generationTime + " (Elapsed " + moment(company.generationTime).fromNow(true) + ")"} />
          </Col>
        )
      default:
        return (
          <Col sm={9}>
            <Input key={company.id + "-generationTime"}
              className="input-text-borderless" type="text" disabled name="generationTime" id="generationTime"
              value={company.generationTime} />
          </Col>
        )
    }
  }


  renderButton(company) {
    switch (company.status) {
      case "PRELOADED":
        return (
          <Button
            type="submit" color="success" className="generate-button mr-2">Generate</Button>
        )
      case "EMAIL_SENT":
        return (
          <Button onClick={this.handleDownload.bind(this, company)}
            color="primary" className="generate-button mr-2">Download</Button>
        )
      case "PENDING":
        return (
          <Button loading="true"
            disabled color="secondary" className="generate-button mr-2">Pending</Button>
        )
      case "GENERATING":
        return (
          <Button loading="true"
            disabled color="secondary" className="generate-button mr-2">Generating</Button>
        )

    }
  }
}


export default App;
