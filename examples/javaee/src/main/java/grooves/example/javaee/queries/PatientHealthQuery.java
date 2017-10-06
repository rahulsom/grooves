package grooves.example.javaee.queries;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.java.Query;
import grooves.example.javaee.Database;
import grooves.example.javaee.domain.*;
import lombok.Getter;
import rx.Observable;

import javax.inject.Inject;

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE;
import static rx.Observable.just;

@Query(aggregate = Patient.class, snapshot = PatientHealth.class)
public class PatientHealthQuery implements CustomQuerySupport<PatientHealth, PatientHealthQuery> {

    @Inject @Getter
    private Database database;

    @Override
    public Class<PatientHealth> getSnapshotClass() {
        return PatientHealth.class;
    }

    @Override
    public PatientHealth createEmptySnapshot() {
        return new PatientHealth();
    }

    @Override
    public void addToDeprecates(PatientHealth snapshot, Patient deprecatedAggregate) {
        snapshot.getDeprecates().add(deprecatedAggregate);
    }

    /**
     * Applies patient created.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply
     */
    public Observable<EventApplyOutcome> applyPatientCreated(
            PatientCreated event, PatientHealth snapshot) {
        if (snapshot.getAggregate() == event.getAggregate()) {
            snapshot.setName(event.getName());
        }
        return just(CONTINUE);
    }

    /**
     * Applies procedure performed.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply
     */
    public Observable<EventApplyOutcome> applyProcedurePerformed(
            ProcedurePerformed event, PatientHealth snapshot) {
        snapshot.getProcedures().add(
                new PatientHealth.Procedure(event.getCode(), event.getTimestamp()));
        return just(CONTINUE);
    }

    /**
     * Applies Payment made.
     * @param event the event.
     * @param snapshot The snapshot.
     * @return the result of apply
     */
    public Observable<EventApplyOutcome> applyPaymentMade(
            PaymentMade event, PatientHealth snapshot) {
        // Ignore payments
        return just(CONTINUE);
    }

}
