import React, { Component, useEffect } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../../styles/home/App.scss';
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
import Dropzone from 'react-dropzone';
import Endpoints from '../../api/Endpoints';
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
    return Endpoints.listFiles()
      .then(data => data.filter(company => company.status != null))
      .then(inProgress => {
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

      Endpoints.uploadFile(data)
        .then(resData => {
          this.setState(state => {
            const companies = [{
              company: resData.company,
              filename: resData.filename,
              status: "PRELOADED",
              id: resData.id,
              period: resData.period
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

    Endpoints.generate(data)
      .then(res => this.getProgress());
  }

  // FINISHED ===============================
  handleDownload(company) {
    console.log("company=" + company.filename);
    Endpoints.downloadGeneratedZip(company.filename)
  }

  render() {
    const dropzoneRef = React.createRef();
    const showAddDetail = this.state.companies.length > 0;
    return (
      <React.Fragment>
        <div className="app">
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
                                <Input key={c.id + "-period"} type="hidden" name="period" value={c.period} />
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
                                      {c?.error?.message ? 
                                      (<Alert color="danger">
                                      <Label>{c?.error?.reason}</Label>
                                      <Label>{c?.error?.message}</Label>
                                      </Alert>
                                      )
                                      : (<span/>)}
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
        </div>
        <Container className="footer text-center">
          <span className="text-muted"> © Ketone Maniac @ 2021</span>          
        </Container>


        {this.uploadErrorModalAlert(this.state.uploadError)}
      </React.Fragment>
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
    switch (company.status) {
      case "PENDING":
      case "GENERATING":
        return (
          <Col sm={9}>
            <Input key={company.id + "-generationTime"}
              className="input-text-borderless" type="text" disabled name="generationTime" id="generationTime"
              value={company.generationTime} />
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
