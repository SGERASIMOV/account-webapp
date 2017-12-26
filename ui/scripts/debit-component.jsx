import React from 'react';
import axios from 'axios';

class DebitComponent extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      value: ''
    };
  }

  render = () => {
      return (
          <div>
            <input type="text" id="addpixinputfield" placeholder="Debit" onKeyPress={e => this.handleKeyPress(e)}/>
          </div>
      );
  };

  handleKeyPress(e) {
    if (e.key === 'Enter') {
          axios.post('/v1/transactions/', {
              actionType: 'Debit',
              changeAmount: e.target.value
            })
            .then(function (response) {
              location.reload();
              console.log(response);
            })
            .catch(function (error) {
              console.log(error);
            });
    }
  }
}

export default DebitComponent;
