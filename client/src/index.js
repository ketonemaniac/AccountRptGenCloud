import React from 'react';
import { createRoot } from 'react-dom/client'
import './styles/index.css';
import Routing from './components/Routing';
// import * as serviceWorker from './utils/serviceWorker';
createRoot(document.getElementById('root')).render(<Routing />);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: http://bit.ly/CRA-PWA
// serviceWorker.unregister();
