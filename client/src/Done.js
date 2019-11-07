import React, { Component } from 'react';
import 'react-circular-progressbar/dist/styles.css';
import CircularProgressbar from 'react-circular-progressbar';
import { Container, Row, Col, Media, ListGroup, ListGroupItem } from 'reactstrap';
import Moment from 'react-moment';
import { faCheckSquare } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

class Done extends Component {

    render() { 
        var recentlyCompleted = this.props.companies.map( company => 
            <ListGroupItem>
              <Media>
                <Media left className="mr-1">
                  <FontAwesomeIcon icon={faCheckSquare} size="2x" className="text-success" />
                </Media>
                <Media body>
                  <Media heading className="h6">{company["company"]}</Media>
                  <Container>
                    <Row>
                      <Col md="4">Started:</Col><Col md="8">{company["start"]}</Col>
                    </Row>
                    <Row>
                      <Col md="4">Elapsed:</Col><Col md="8"><Moment duration={company["start"]} date={company["end"]} /></Col>
                    </Row>
                   </Container>
                </Media>
              </Media>
            </ListGroupItem>
            );        
        return (
          <ListGroup flush>
            {recentlyCompleted}
          </ListGroup>
        );
      }

}

export default Done;