import React, { Component } from 'react';
import 'react-circular-progressbar/dist/styles.css';
import CircularProgressbar from 'react-circular-progressbar';
import { Container, Row, Col, Media, Button } from 'reactstrap';
import Moment from 'react-moment';

class Progress extends Component {

    render() {
        const percentage = 66;
        var progresses = this.props.companies.map(company => 
            <Media className="mt-1">
              <Media left className="mr-1" style={{width: "100px"}}>
                <CircularProgressbar
                                percentage={percentage}
                                text={company["status"]}
                                styles={{
                                  text: { fontSize: '12px' },
                                }}
                              />  
              </Media>
              <Media body>
                <Media heading className="h6">{company["company"]}</Media>
                <Container>
                <Row>
                  <Col md="4">Started:</Col><Col md="8">{company["generationTime"]}</Col>
                </Row>
                <Row>
                  <Col md="4">Elapsed:</Col><Col md="8"><Moment duration={company["generationTime"]} date={new Date()} /></Col>
                </Row>
                <Row>
                  <Col md="4">Job Id:</Col><Col md="8">{company["handleName"]}</Col>
                </Row>
                <Row>
                  <Col md="12"><Button color="danger">Cancel Job</Button></Col>
                </Row>
               </Container>

                
              </Media>
            </Media>
        );
    
        return <ul>
          {progresses}
        </ul>;
      }

}

export default Progress;