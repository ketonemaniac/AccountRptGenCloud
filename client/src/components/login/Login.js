import React from 'react';
import { Container, Form, FormGroup, Label, Input, Button, Alert } from 'reactstrap';
import '../../styles/login/Login.scss';
import { useLocation } from 'react-router-dom';

const Login = (props) => {
    let location = useLocation();
    console.log(location);
    const err = location.search === "?error=true"
    return (<Container className="login">
                <h1 className="logo">Accounting Report Generator</h1>
                <Container className="login-form">
                    {err ? ( <Alert color="danger">
                            Invalid Login. Please try again.
                        </Alert>
                        ) : (<span></span>)}
                    <Form name='f' action="perform_login" method='POST'>
                        <FormGroup className="login-field">
                            <Label>User:</Label>
                            <Input type='text' name='username'/>
                        </FormGroup>
                        <FormGroup className="login-field">
                            <Label>Password:</Label>
                            <Input type='password' name='password' />
                        </FormGroup>
                        <Button className="login-field login-submit">Login</Button>
                    </Form>
                </Container>
            </Container>)
}

export default Login;
