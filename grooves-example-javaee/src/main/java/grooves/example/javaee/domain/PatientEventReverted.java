package grooves.example.javaee.domain;
//tag::documented[]

import com.github.rahulsom.grooves.api.events.RevertEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PatientEventReverted extends PatientEvent // <1>
        implements RevertEvent<Patient, Long, PatientEvent> { // <2>
    private final Long revertedEventId; // <3>
    //end::documented[]

    @Override
    public String toString() {
        return String.format("PatientEventReverted{revertedEventId=%d}", revertedEventId);
    }

    //tag::documented[]
}
//end::documented[]