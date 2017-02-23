package grooves.grails.rdbms

import com.github.rahulsom.grooves.annotations.Query
import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.api.QueryUtil
import grails.compiler.GrailsCompileStatic
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE

@Query(aggregate = Patient, snapshot = PatientHealth)
@GrailsCompileStatic
class PatientHealthQuery implements QueryUtil<Patient, PatientEvent, PatientHealth> {
    public static final Map LATEST = [sort: 'lastEvent', order: 'desc', offset: 0, max: 1]
    public static final Map INCREMENTAL = [sort: 'position', order: 'asc']

    @Override
    PatientHealth createEmptySnapshot() { new PatientHealth(deprecates: []) }

    @Override
    Optional<PatientHealth> getSnapshot(long startWithEvent, Patient aggregate) {
        def snapshots = startWithEvent == Long.MAX_VALUE ?
                PatientHealth.findAllByAggregate(aggregate, LATEST) :
                PatientHealth.findAllByAggregateAndLastEventLessThan(aggregate, startWithEvent, LATEST)

        (snapshots ? Optional.of(snapshots[0]) : Optional.empty()) as Optional<PatientHealth>
    }

    @Override
    void detachSnapshot(PatientHealth retval) {
        if (retval.isAttached()) {
            retval.discard()
            retval.id = null
        }
    }

    @Override
    List<PatientEvent> getUncomputedEvents(Patient aggregate, PatientHealth lastSnapshot, long lastEvent) {
        PatientEvent.
                findAllByAggregateAndPositionGreaterThanAndPositionLessThanEquals(
                        aggregate, lastSnapshot?.lastEvent ?: 0L, lastEvent, INCREMENTAL)
    }

    @Override
    boolean shouldEventsBeApplied(PatientHealth snapshot) {
        true
    }

    @Override
    List<PatientEvent> findEventsForAggregates(List<Patient> aggregates) {
        PatientEvent.findAllByAggregateInList(aggregates, INCREMENTAL) as List<? extends PatientEvent>
    }

    @Override
    void addToDeprecates(PatientHealth snapshot, Patient otherAggregate) {
        snapshot.addToDeprecates(otherAggregate)
    }

    @Override
    PatientEvent unwrapIfProxy(PatientEvent event) {
        GrailsHibernateUtil.unwrapIfProxy(event) as PatientEvent
    }

    EventApplyOutcome applyPatientCreated(PatientCreated event, PatientHealth snapshot) {
        snapshot.name = event.name
        CONTINUE
    }

    EventApplyOutcome applyProcedurePerformed(ProcedurePerformed event, PatientHealth snapshot) {
        snapshot.addToProcedures(code: event.code, date: event.date)
        CONTINUE
    }

    EventApplyOutcome applyPaymentMade(PaymentMade event, PatientHealth snapshot) {
        // Ignore payments
        CONTINUE
    }

}
