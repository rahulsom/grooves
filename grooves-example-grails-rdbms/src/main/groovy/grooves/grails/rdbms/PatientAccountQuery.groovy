package grooves.grails.rdbms

import com.github.rahulsom.grooves.annotations.Query
import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.api.QueryUtil
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE

@Query(aggregate = Patient, snapshot = PatientAccount)
class PatientAccountQuery implements QueryUtil<Patient, PatientEvent, PatientAccount> {

    public static final Map LATEST = [sort: 'lastEvent', order: 'desc', offset: 0, max: 1]
    public static final Map INCREMENTAL = [sort: 'position', order: 'asc']

    @Override
    PatientAccount createEmptySnapshot() { new PatientAccount(deprecates: []) }

    @Override
    Optional<PatientAccount> getSnapshot(long startWithEvent, Patient aggregate) {
        def snapshots = startWithEvent == Long.MAX_VALUE ?
                PatientAccount.findAllByAggregate(aggregate, LATEST) :
                PatientAccount.findAllByAggregateAndLastEventLessThan(aggregate, startWithEvent, LATEST)

        (snapshots ? Optional.of(snapshots[0]) : Optional.empty()) as Optional<PatientAccount>
    }

    @Override
    void detachSnapshot(PatientAccount retval) {
        if (retval.isAttached()) {
            retval.discard()
            retval.id = null
        }
    }

    @Override
    List<PatientEvent> getUncomputedEvents(Patient aggregate, PatientAccount lastSnapshot, long lastEvent) {
        PatientEvent.
                findAllByAggregateAndPositionGreaterThanAndPositionLessThanEquals(
                        aggregate, lastSnapshot?.lastEvent ?: 0L, lastEvent, INCREMENTAL)
    }

    @Override
    boolean shouldEventsBeApplied(PatientAccount snapshot) {
        true
    }

    @Override
    List<PatientEvent> findEventsForAggregates(List<Patient> aggregates) {
        PatientEvent.findAllByAggregateInList(aggregates, INCREMENTAL) as List<? extends PatientEvent>
    }

    @Override
    void addToDeprecates(PatientAccount snapshot, Patient otherAggregate) {
        snapshot.addToDeprecates(otherAggregate)
    }

    @Override
    PatientEvent unwrapIfProxy(PatientEvent event) {
        GrailsHibernateUtil.unwrapIfProxy(event) as PatientEvent
    }

    EventApplyOutcome applyPatientCreated(PatientCreated event, PatientAccount snapshot) {
        snapshot.name = event.name
        CONTINUE
    }

    EventApplyOutcome applyProcedurePerformed(ProcedurePerformed event, PatientAccount snapshot) {
        snapshot.balance += event.cost
        CONTINUE
    }

    EventApplyOutcome applyPaymentMade(PaymentMade event, PatientAccount snapshot) {
        snapshot.balance -= event.amount
        snapshot.moneyMade += event.amount
        CONTINUE
    }

}
