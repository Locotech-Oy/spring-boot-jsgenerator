


export class Todo {

  /**
   * @param {number} id
   */
  set id(id) {this._id = id;}
  /**
   * @returns {number}
   */
  get id() { return this._id;}

  /**
   * @param {string} note
   */
  set note(note) {this._note = note;}
  /**
   * @returns {string}
   */
  get note() { return this._note;}


  constructor(props) {
    Object.assign(this, props);
  }

  fromProps(props) {
    let instance = new Todo();
    Object.assign(instance,props);
    return instance;
  }
  toJSON(){
    let rtn = {};
    for(let p in this){
      if(p.charAt(0) == '_'){
        // private member according to convention, test for getter
        let pp = p.substring(1);
        if(typeof this[pp] != 'undefined'){
          rtn[pp] = this[pp];
        }
      }
    }
    return rtn;
  }


  // --- ### --- ### Custom code START ### --- ### ---

  // ### --- ### --- Custom code END --- ### --- ###

}
