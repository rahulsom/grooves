package grooves.example.javaee.domain;
//tag::documented[]

import com.github.rahulsom.grooves.api.events.RevertEvent;
import lombok.Getter;

public class PatientEventReverted
        extends PatientEvent // <1>
        implements RevertEvent<Patient, Long, PatientEvent> { // <2>
    @Getter private Long revertedEventId; // <3>
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