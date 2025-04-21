import * as React from 'react';
import '@/styles/login/Login.scss';
import { Container, TextField, Button, Box, Typography, Paper } from "@mui/material";
// @ts-ignore
import backgroundImage from "@/assets/background.jpg";

const Login = (props: any) => {

    return (
    <Box sx={{
        backgroundImage: `url(${backgroundImage})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
      }}>
    <Container maxWidth="xs">
        <Paper elevation={3} sx={{ p: 4, mt: 8, textAlign: "center" }}>
          <Typography variant="h5" gutterBottom sx={{ fontFamily: 'Roboto, sans-serif', fontWeight: 'bold', letterSpacing: 1 }}>
            Accounting Report Generator
          </Typography>
          <Box component="form" action="perform_login" method="POST">
            <TextField
              fullWidth
              label="Username"
              name="username"
              margin="normal"
              required
            />
            <TextField
              fullWidth
              label="Password"
              type="password"
              name="password"
              margin="normal"
              required
            />
            <Button type="submit" variant="contained" color="primary" fullWidth sx={{ mt: 2 }}>
              Login
            </Button>
          </Box>
        </Paper>
      </Container>
      </Box>)
}

export default Login;
