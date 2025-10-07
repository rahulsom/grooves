package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.java.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;

// tag::documented[]
@Data
@EqualsAndHashCode(callSuper = true)
@Event(Patient.class) // <1>
public class PatientCreated extends PatientEvent { // <2>
    private final String name;
}
// end::documented[]
