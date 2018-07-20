package grooves.example.javaee.domain;
//tag::documented[]

import com.github.rahulsom.grooves.api.events.RevertEvent;

public class PatientEventReverted extends PatientEvent // <1>
        implements RevertEvent<Patient, Long, PatientEvent> { // <2>
    private final Long revertedEventId; // <3>
    //end::documented[]

    public Long getRevertedEventId() {
        return revertedEventId;
    }

    @Override
    public String toString() {
        return String.format("PatientEventReverted{revertedEventId=%d}", revertedEventId);
    }

    public PatientEventReverted(Long revertedEventId) {
        this.revertedEventId = revertedEventId;
    }
    //tag::documented[]
}
//end::documented[]