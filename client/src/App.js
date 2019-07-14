import React, { Component } from 'react';
import logo from './logo.svg';
import 'bootstrap/dist/css/bootstrap.min.css';
import AppHeader from './AppHeader.js';
import Progress from './Progress.js';
import Done from './Done.js';
import './App.css';
import {
  Container, Row, Col,
  Jumbotron, Button,
  Card, CardHeader, CardImg, CardText, CardBody,
  CardTitle, CardSubtitle, Media
} from 'reactstrap';
import { faRedo } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import Moment from 'react-moment';
import Dropzone from 'react-dropzone';
import axios from 'axios';

class App extends Component {

  state = {
    isAdmin: false,
    date: new Date(),
    companies : []
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
          console.log(res.statusText)
        })
    }
    );
  }

  render() {
    const dropzoneRef = React.createRef();
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
                <p className="lead">Drag your input file over this area, or click to select the file from the file picker below</p>
                <p className="lead">
                    <Button color="primary" onClick={() => dropzoneRef.current.open()}>
                      Select File
                    </Button>
                </p>
              </Container>
            </Jumbotron>
          )
          }}
        </Dropzone>
        <Container>
          <Row>
            <Col md="5" xs="12">
              <Card>
                <CardHeader>Recently Completed</CardHeader>
                <CardBody>
                  <Done companies={this.state.companies} />
                </CardBody>
              </Card>
            </Col>
            <Col md="7" xs="12">
              <Card>
                <CardHeader>In Progress 
                  <div className="float-right">
                    <i className="mr-1">Last Updated <Moment interval={1000} diff={this.state.date} unit="seconds"></Moment> secs ago</i>
                    <Button onClick={() => {this.setState({date: new Date(), number : this.state.number + 1} )}}><FontAwesomeIcon icon={faRedo} /> Refresh</Button>
                  </div>
                  </CardHeader>
                <CardBody>
                  <Progress companies={this.state.companies} />
              </CardBody>
              </Card>
            </Col>
          </Row>
        </Container>
      </div>
    );
  }
}


export default App;
