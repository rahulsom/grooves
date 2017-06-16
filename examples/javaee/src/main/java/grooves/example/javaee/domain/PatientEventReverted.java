package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.events.RevertEvent;

public class PatientEventReverted extends PatientEvent implements
        RevertEvent<Long, Patient, Long, PatientEvent> {
    private Long revertedEventId;

    @Override
    public Long getRevertedEventId() {
        return revertedEventId;
    }

    @Override
    public String getAudit() {
        return "revertedEvent:" + revertedEventId;
    }

    @Override
    public String toString() {
        return String.format("PatientEventReverted{revertedEventId=%d}", revertedEventId);
    }

    public PatientEventReverted(Long revertedEventId) {
        this.revertedEventId = revertedEventId;
    }
}
