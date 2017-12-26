import React from 'react';
import axios from 'axios';

class CreditComponent extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      value: ''
    };
  }

  render = () => {
      return (
          <div>
            <input type="text" id="addpixinputfield" placeholder="Credit" onKeyPress={e => this.handleKeyPress(e)}/>
          </div>
      );
  };

  handleKeyPress(e) {
    if (e.key === 'Enter') {
          axios.post('/v1/transactions/', {
              actionType: 'Credit',
              changeAmount: e.target.value
            })
            .then(function (response) {
              location.reload();
              console.log(response);
            })
            .catch(function (error) {
              alert(error.response.data)
              console.log(error);
            });
    }
  }
}

export default CreditComponent;
