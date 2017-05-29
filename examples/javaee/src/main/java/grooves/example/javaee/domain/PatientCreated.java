package grooves.example.javaee.domain;

public class PatientCreated extends PatientEvent {
    private String name;

    @Override
    public String getAudit() {
        return "name: " + name;
    }

    @Override
    public String toString() {
        return String.format("PatientCreated{name='%s'}", name);
    }

    public String getName() {
        return name;
    }

    public PatientCreated(String name) {
        this.name = name;
    }
}
