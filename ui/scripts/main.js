import React from 'react';
import ReactDOM from 'react-dom';
import TransactionsComponent from './transactions-component.jsx';
import DebitComponent from './debit-component.jsx';
import CreditComponent from './credit-component.jsx';

ReactDOM.render(<TransactionsComponent />, document.getElementById('transactions'));
ReactDOM.render(<DebitComponent />, document.getElementById('debit'));
ReactDOM.render(<CreditComponent />, document.getElementById('credit'));
