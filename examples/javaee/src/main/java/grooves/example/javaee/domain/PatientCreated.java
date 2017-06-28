package grooves.example.javaee.domain;

//tag::documented[]
public class PatientCreated extends PatientEvent { // <1>
    private String name;

    @Override
    public String getAudit() { // <2>
        return "name: " + name;
    }
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