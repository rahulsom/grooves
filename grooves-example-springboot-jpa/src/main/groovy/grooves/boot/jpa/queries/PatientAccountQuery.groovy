package grooves.boot.jpa.queries

import com.github.rahulsom.grooves.annotations.Query
import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.api.QueryUtil
import grooves.boot.jpa.domain.*
import grooves.boot.jpa.repositories.PatientAccountRepository
import grooves.boot.jpa.repositories.PatientEventRepository
import org.hibernate.engine.spi.SessionImplementor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE

@Transactional
@Component
@Query(aggregate = Patient, snapshot = PatientAccount)
class PatientAccountQuery implements QueryUtil<Patient, PatientEvent, PatientAccount> {

    @Autowired EntityManager entityManager
    @Autowired PatientAccountRepository patientAccountRepository
    @Autowired PatientEventRepository patientEventRepository

    @Override
    PatientAccount createEmptySnapshot() {
        new PatientAccount(deprecates: [])
    }

    @Override
    Optional<PatientAccount> getSnapshot(long startWithEvent, Patient aggregate) {
        def snapshots = startWithEvent == Long.MAX_VALUE ?
                patientAccountRepository.findAllByAggregateId(aggregate.id) :
                patientAccountRepository.findAllByAggregateIdAndLastEventLessThan(aggregate.id, startWithEvent)
        (snapshots ? Optional.of(snapshots[0]) : Optional.empty()) as Optional<PatientAccount>
    }

    @Override
    void detachSnapshot(PatientAccount retval) {
        if (entityManager.contains(retval)) {
            entityManager.detach(retval)
            retval.id = null
        }
    }

    @Override
    List<PatientEvent> getUncomputedEvents(Patient patient, PatientAccount lastSnapshot, long lastEvent) {
        patientEventRepository.getUncomputedEvents patient, lastSnapshot?.lastEvent ?: 0L, lastEvent
    }

    @Override
    boolean shouldEventsBeApplied(PatientAccount snapshot) {
        return true
    }

    @Override
    List<PatientEvent> findEventsForAggregates(List<Patient> aggregates) {
        patientEventRepository.findAllByAggregateIn(aggregates)
    }

    @Override
    void addToDeprecates(PatientAccount snapshot, Patient otherAggregate) {
        snapshot.deprecates << otherAggregate
    }

    @Override
    PatientEvent unwrapIfProxy(PatientEvent event) {
        entityManager.
                unwrap(SessionImplementor.class).
                persistenceContext.
                unproxy(event) as PatientEvent
    }

    @Override
    EventApplyOutcome onException(Exception e, PatientAccount snapshot, PatientEvent event) {
        snapshot.processingErrors++
        CONTINUE
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
