
import {Request} from './Request';

export class RestClient {
  doArray() {
    let request = new Request($$$METHOD, $$$PARAMS, $$$PATH);
    return new Promise((resolve, reject) => {
      request.doRequest().then((res) => {
        let data = new $$$COLLECTION();
        for(let row of res.body) {
          data.push(new $$$OBJECT(row));
        }
        resolve(data);
      }).catch((err) => {
        reject(err);
      })
    });
  }

  doObject() {
    let request = new Request($$$METHOD, $$$PARAMS, $$$PATH);
    return new Promise((resolve, reject) => {
      request.doRequest().then((res) => {
        resolve(new $$$OBJECT(res.data));
      }).catch((err) => {
        reject(err);
      })
    });
  }

  doVoid() {
    let request = new Request($$$METHOD, $$$PARAMS, $$$PATH);
    return new Promise((resolve, reject) => {
      request.doRequest().then((res) => {
        resolve(true);
      }).catch((err) => {
        reject(err);
      })
    });
  }
}