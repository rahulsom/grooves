package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.java.Event;
import lombok.Getter;

//tag::documented[]
@Event(Patient.class) // <1>
public class PatientCreated extends PatientEvent { // <2>
    @Getter private final String name;
    //end::documented[]

    @Override
    public String toString() {
        return String.format("PatientCreated{name='%s'}", name);
    }
    //tag::documented[]
    
    public PatientCreated(String name) {
        this.name = name;
    }
}
//end::documented[]