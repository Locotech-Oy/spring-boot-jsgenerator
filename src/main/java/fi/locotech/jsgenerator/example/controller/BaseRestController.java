package fi.locotech.jsgenerator.example.controller;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

@RestController
public class BaseRestController <T, K extends Serializable> {

  protected JpaRepository<T, K> getRepository() {
    return null;
  }

  @RequestMapping(value = "/one", method = RequestMethod.GET)
  public T getOne(@RequestParam("id") K id) {
    T t = getRepository().findOne(id);
    return t;
  }

  @RequestMapping(value = "/save", method = RequestMethod.POST)
  public T save(@RequestBody T item) {
    if (item == null)
      return null;
    return getRepository().save(item);
  }

  @RequestMapping(value = "/all", method = RequestMethod.GET)
  public List<T> all() {
    return getRepository().findAll();
  }

  @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
  public void delete(@RequestParam("id") K id) {
    getRepository().delete(id);
  }

  @RequestMapping(value = "/count", method = RequestMethod.GET)
  public long count() {
    return getRepository().count();
  }


}
