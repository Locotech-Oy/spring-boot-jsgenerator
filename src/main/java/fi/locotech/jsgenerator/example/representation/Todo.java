package fi.locotech.jsgenerator.example.representation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.locotech.jsgenerator.jsgenerator.JSClass;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@JSClass
@Entity
@Data
public class Todo {

    @Id
    @GeneratedValue
    private Long id;

    private String note;

    public Todo(){}
    public Todo(String note){
        this.note = note;
    }
}
