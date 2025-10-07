package grooves.example.javaee.queries;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE;
import static rx.Observable.just;
import static rx.RxReactiveStreams.toPublisher;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.java.Query;
import grooves.example.javaee.Database;
import grooves.example.javaee.domain.*;
import javax.inject.Inject;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

@Getter
@Query(aggregate = Patient.class, snapshot = PatientHealth.class)
public class PatientHealthQuery implements CustomQuerySupport<PatientHealth> {

    @Inject
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
    public void addToDeprecates(@NotNull PatientHealth snapshot, @NotNull Patient deprecatedAggregate) {
        snapshot.getDeprecates().add(deprecatedAggregate);
    }

    /**
     * Applies patient created.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply
     */
    public Publisher<EventApplyOutcome> applyPatientCreated(PatientCreated event, PatientHealth snapshot) {
        if (snapshot.getAggregate() == event.getAggregate()) {
            snapshot.setName(event.getName());
        }
        return toPublisher(just(CONTINUE));
    }

    /**
     * Applies procedure performed.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply
     */
    public Publisher<EventApplyOutcome> applyProcedurePerformed(ProcedurePerformed event, PatientHealth snapshot) {
        snapshot.getProcedures().add(new PatientHealth.Procedure(event.getCode(), event.getTimestamp()));
        return toPublisher(just(CONTINUE));
    }

    /**
     * Applies Payment made.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply
     */
    public Publisher<EventApplyOutcome> applyPaymentMade(PaymentMade event, PatientHealth snapshot) {
        // Ignore payments
        return toPublisher(just(CONTINUE));
    }
}
