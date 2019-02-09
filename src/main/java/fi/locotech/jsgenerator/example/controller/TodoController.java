package fi.locotech.jsgenerator.example.controller;

import fi.locotech.jsgenerator.example.representation.Todo;
import fi.locotech.jsgenerator.jsgenerator.JSController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@JSController(Todo.class)
@RestController
@RequestMapping("/todo")
public class TodoController extends BaseRestController<Todo, Long> {
}
