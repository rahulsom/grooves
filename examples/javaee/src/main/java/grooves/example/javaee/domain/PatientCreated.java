package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.java.Event;

//tag::documented[]
@Event(Patient.class) // <1>
public class PatientCreated extends PatientEvent { // <2>
    private String name;
    //end::documented[]

    @Override
    public String toString() {
        return String.format("PatientCreated{name='%s'}", name);
    }

    public String getName() {
        return name;
    }
    //tag::documented[]
    
    public PatientCreated(String name) {
        this.name = name;
    }
}
//end::documented[]