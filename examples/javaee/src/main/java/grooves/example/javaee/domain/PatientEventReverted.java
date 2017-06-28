package grooves.example.javaee.domain;
//tag::documented[]

import com.github.rahulsom.grooves.api.events.RevertEvent;

public class PatientEventReverted
        extends PatientEvent // <1>
        implements RevertEvent<Long, Patient, Long, PatientEvent> { // <2>
    private Long revertedEventId;

    @Override
    public Long getRevertedEventId() { // <3>
        return revertedEventId;
    }

    @Override
    public String getAudit() {
        return "revertedEvent:" + revertedEventId;
    }
    //end::documented[]

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