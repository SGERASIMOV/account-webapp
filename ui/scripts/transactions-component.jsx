import React from 'react';
import axios from 'axios';
import ReactTable from 'react-table'

class TransactionsComponent extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      data: []
    };
  }
  componentDidMount = () => {
    axios.get('/v1/transactions/').then((response) => {
      this.setState({
        data: response.data
      });
    })
  };
  render = () => {
    const { data } = this.state;
    return (
            <div>
                <ReactTable
                  getTrProps={(state, rowInfo, column, instance) => {
                    if (!rowInfo) return {}
                    return {
                      style: {
                        background: rowInfo.row.actionType === 'Debit' ? 'lightblue' : 'salmon'
                      }
                    }
                  }}
                  data={data}
                  columns={[
                        {
                          Header: "Transaction Id",
                          accessor: "id",
                          headerStyle: {background: 'lightyellow'}
                        },
                        {
                          Header: "Action Type",
                          accessor: "actionType",
                          headerStyle: {background: 'lightyellow'}
                        },
                        {
                          Header: "Amount",
                          accessor: "changeAmount",
                          headerStyle: {background: 'lightyellow'}
                        },
                        {
                          Header: "Created Date",
                          accessor: "createdDate",
                          headerStyle: {background: 'lightyellow'}
                        }
                          ]}
                  defaultPageSize={10}
                  className="-striped -highlight"
                />
            </div>
           );
  }
}

export default TransactionsComponent;
