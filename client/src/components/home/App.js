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
import { fetchEventSource } from "@microsoft/fetch-event-source";
import Bg from '../../assets/background.jpg'


class App extends Component {

  state = {
    isAdmin: false,
    date: new Date(),
    companies: [],
    fileUploadBlock: false,
    uploadError: null,
    isUploadErrorModalOpen: false
  };

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
  }


  setAdmin = (admin) => {
    this.setState({ isAdmin: admin });
  }

  // ON DROP ======================
  onDrop = (acceptedFiles, rejectedFiles) => {
    var randInt = Math.floor(Math.random() * 10000000);
    acceptedFiles.map(file => {
      console.log("acceptedFile=" + file.name + " size=" + file.size);
      const data = new FormData()
      data.append('file', file, file.name)
      data.append('seed', randInt)
      var that = this;
      const fetchData = async () => {
      await fetchEventSource(`/api/accrptgen/file`, {
        method: "POST",
        headers: {
          Accept: "text/event-stream",
        },
        body: data,
        onopen(res) {
          if (res.ok && res.status === 200) {
            console.log("Connection made ", res);
          } else if (
            res.status >= 400 &&
            res.status < 500 &&
            res.status !== 429
          ) {
            console.log("Client side error ", res);
          }
        },
        onmessage(event) {
          console.log(event.data);
          var resData = JSON.parse(event.data)
          that.setState(state => {
            var hasMatch = false;
            state.companies.forEach(c => {
              if(c.id == resData.id) {
                c.company = resData.company
                c.filename = resData.filename
                c.status = resData.status
                c.id = resData.id
                c.period = resData.period
                c.docType = resData.docType
                c.referredBy = resData.referredBy
                c.generationTime = resData.generationTime
                c.errorMsg = resData.errorMsg
                hasMatch = true;
                }
            })
            var companies = state.companies;
            if(!hasMatch) {
              companies = [{
                company: resData.company,
                filename: resData.filename,
                status: resData.status,
                id: resData.id,
                period: resData.period,
                docType: resData.docType,
                referredBy: resData.referredBy,
                generationTime: resData.generationTime,
                errorMsg: resData.errorMsg,
              }, ...state.companies];
            }
            
            return {
              companies: companies,
              fileUploadBlock: false
            };
          });
        },
        onclose() {
          console.log("Connection closed by the server");
        },
        onerror(err) {
          console.log("There was an error from server", err);
        },
      });
    };
    fetchData();
      

      // Endpoints.uploadFile(data)
        // var resData = e.data
        // .then(resData => {
          // this.setState(state => {
          //   const companies = [{
          //     company: resData.company,
          //     filename: resData.filename,
          //     status: resData.status,
          //     id: resData.id,
          //     period: resData.period,
          //     docType: resData.docType,
          //     referredBy: resData.referredBy 
          //   }, ...state.companies];
        //     return {
        //       companies: companies,
        //       fileUploadBlock: false
        //     };
        //   });
        // })
        // .catch(e => {
        //   this.setState({
        //     uploadError: e?.response?.data?.message,
        //     fileUploadBlock: false,
        //     isUploadErrorModalOpen: true
        //   }
        //   )
        // }
        // )

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
            <p /><span className="text-muted">Error: {error}</span>
          </ModalBody>
        </Modal>
      </div>
    )
  }

  toggleUploadErrorModal() {
    this.setState((oldState) => {
      return { isUploadErrorModalOpen: !oldState.isUploadErrorModalOpen }
    });
  }

  // ON GENERATE ===========================
  // handleStartGeneration = (event) => {
  //   event.preventDefault();
  //   const data = new FormData(event.target);

  //   Endpoints.generate(data)
  //     .then(res => this.getProgress());
  // }

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
                <div className="jumbotron" style={{
                  paddingTop: showAddDetail ? "5%" : "15%",
                  paddingBottom: showAddDetail ? "5%" : "15%",
                  backgroundImage: `url(${Bg})`,
                }}
                  fluid="true" {...getRootProps({ onClick: evt => evt.preventDefault() })}>
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
                </div>
              )
            }}
          </Dropzone>
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
                                <Input key={c.id + "-company"} type="hidden" name="company" value={c.company} />
                                <Input key={c.id + "-period"} type="hidden" name="period" value={c.period} />
                                
                                <FormGroup row>
                                      <Label sm={3} for="referredBy">Referrer</Label>
                                        <Col sm={9}>
                                          <Input key={c.id + "-referredBy"} className="input-text-borderless"
                                            disabled="disabled"
                                            type="text" name="referredBy" id="referredBy"
                                            value={c.referredBy} />
                                        </Col>
                                </FormGroup>
                                <FormGroup row>
                                  <Label sm={3} for="status">Status</Label>
                                  <Col sm={9}>
                                    <Input key={c.generationTime + "-status"}
                                      className="input-text-borderless" type="text" disabled name="status" id="status" value={c.status} />
                                      {c?.errorMsg ?
                                      (<Alert color="danger">
                                      <Label>{c?.errorMsg}</Label>
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
          <span className="text-muted"> © Ketone Maniac @ 2024</span>          
        </Container>


        {this.uploadErrorModalAlert(this.state.uploadError)}
      </React.Fragment>
    );
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
