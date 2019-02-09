

import superagent from 'superagent';

export class Request {

  constructor(method, params, path) {
    this.method = method;
    this.params = params;
    this.path = path;
  }


  get path() {
    return this._path;
  }

  set path(path) {
    this._path = path;
  }

  set method(method) {
    this._method = method;
  }

  get method() {
    return this._method;
  }

  set params(params) {
    this._params = params;
  }

  get params() {
    if (this._params == null)
      return {};
    if(this._params.toJSON instanceof Function)
      return this._params.toJSON();
    return this._params;
  }

  get url() {
    //FIXME
    return this.path;
  }

  doRequest() {
    switch (this.method) {
      case 'GET': {
        if (this.params != null) {
          return new Promise((resolve, reject) => {
            superagent
              .get(this.url)
              .query(this.params)
              .end((err, res) => {
                if (err != null) {
                  reject(err);
                } else if (res != null) {
                  resolve(res);
                }
              });
          });
        }
        else {
          return new Promise((resolve, reject) => {
            superagent
              .get(this.url)
              .end((err, res) => {
                if (err != null) {
                  reject(err);
                } else if (res != null) {
                  resolve(res);
                }
              });
          });
        }
      }
      case 'DELETE': {
        if (this.params != null) {
          return new Promise((resolve, reject) => {
            superagent
              .delete(this.url)
              .query(this.params)
              .end((err, res) => {
                if (err != null) {
                  reject(err);
                } else if (res != null) {
                  resolve(res);
                }
              });
          });
        }
        else {
          return new Promise((resolve, reject) => {
            superagent
              .delete(this.url)
              .end((err, res) => {
                if (err != null) {
                  reject(err);
                } else if (res != null) {
                  resolve(res);
                }
              });
          });
        }
      }
      case 'POST': {
        if (this.params != null) {
          return new Promise((resolve, reject) => {
            superagent
              .post(this.url)
              .send(this.params)
              .end((err, res) => {
                if (err != null) {
                  reject(err);
                } else if (res != null) {
                  resolve(res);
                }
              });
          });
        }
        else {
          return new Promise((resolve, reject) => {
            superagent
              .post(this.url)
              .end((err, res) => {
                if (err != null) {
                  reject(err);
                } else if (res != null) {
                  resolve(res);
                }
              });
          });
        }
      }
      default: return null;
    }
  }
}
