
import {Request} from './../../api/Request';
import {Todo} from './../representation/Todo';


export class TodoController {
  /**
  * @returns {Promise<number>}
  */
  static count(){
    const path = '/todo/count';
    const method = 'GET';
    let request = new Request(method, null, path);
    return new Promise((resolve, reject) => {
      request.doRequest().then((res) => {
        resolve(res.body);
      }).catch((err) => {
        reject(err);
      })
    });
  }

  /**
  * @param {number} arg0
  * @returns {Promise<void>}
  */
  static delete(arg0){
    const path = '/todo/delete';
    const method = 'DELETE';
    let request = new Request(method, {id:arg0}, path);
    return new Promise((resolve, reject) => {
      request.doRequest().then((res) => {
        resolve(true);
      }).catch((err) => {
        reject(err);
      })
    });
  }

  /**
  * @param {Todo} arg0
  * @returns {Promise<Todo>}
  */
  static save(arg0){
    const path = '/todo/save';
    const method = 'POST';
    let request = new Request(method, arg0, path);
    return new Promise((resolve, reject) => {
      request.doRequest().then((res) => {
        if(res.body == null)
          resolve(null);
        else
          resolve(new Todo(res.body));
      }).catch((err) => {
        reject(err);
      })
    });
  }

  /**
  * @returns {Promise<Array<Todo>>}
  */
  static all(){
    const path = '/todo/all';
    const method = 'GET';
    let request = new Request(method, null, path);
    return new Promise((resolve, reject) => {
      request.doRequest().then((res) => {
        let data = new Array();
        for(let row of res.body) {
          data.push(new Todo(row));
        }
        resolve(data);
      }).catch((err) => {
        reject(err);
      })
    });
  }

  /**
  * @param {number} arg0
  * @returns {Promise<Todo>}
  */
  static getOne(arg0){
    const path = '/todo/one';
    const method = 'GET';
    let request = new Request(method, {id:arg0}, path);
    return new Promise((resolve, reject) => {
      request.doRequest().then((res) => {
        if(res.body == null)
          resolve(null);
        else
          resolve(new Todo(res.body));
      }).catch((err) => {
        reject(err);
      })
    });
  }





  // --- ### --- ### Custom code START ### --- ### ---

  // ### --- ### --- Custom code END --- ### --- ###

}
