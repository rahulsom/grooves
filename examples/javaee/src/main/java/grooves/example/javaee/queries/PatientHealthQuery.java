package grooves.example.javaee.queries;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.java.Query;
import grooves.example.javaee.Database;
import grooves.example.javaee.domain.Patient;
import grooves.example.javaee.domain.PatientCreated;
import grooves.example.javaee.domain.PatientHealth;
import grooves.example.javaee.domain.ProcedurePerformed;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import javax.inject.Inject;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.*;
import static rx.Observable.*;
import static rx.RxReactiveStreams.*;

@Query(aggregate = Patient.class, snapshot = PatientHealth.class)
public class PatientHealthQuery implements CustomQuerySupport<PatientHealth> {

    @Inject @Getter
    private Database database;

    @Override
    public Class<PatientHealth> getSnapshotClass() {
        return PatientHealth.class;
    }

    @NotNull
    @Override
    public PatientHealth createEmptySnapshot() {
        return new PatientHealth();
    }

    @Override
    public void addToDeprecates(
            @NotNull PatientHealth snapshot, @NotNull Patient deprecatedAggregate) {
        snapshot.getDeprecates().add(deprecatedAggregate);
    }

    @Override
    public Publisher<EventApplyOutcome> applyPatientCreated(
            PatientCreated event, PatientHealth snapshot) { // <12>
        if (snapshot.getAggregate() == event.getAggregate()) {
            snapshot.setName(event.getName());
        }
        return toPublisher(just(CONTINUE)); // <13>
    }


    @Override
    public Publisher<EventApplyOutcome> applyProcedurePerformed(
            ProcedurePerformed event, PatientHealth snapshot) {
        snapshot.getProcedures().add(
                new PatientHealth.Procedure(event.getCode(), event.getTimestamp()));
        return toPublisher(just(CONTINUE));
    }

}
