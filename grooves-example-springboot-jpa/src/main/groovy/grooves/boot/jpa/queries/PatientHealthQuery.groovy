package grooves.boot.jpa.queries

import com.github.rahulsom.grooves.annotations.Query
import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.api.QueryUtil
import grooves.boot.jpa.domain.*
import grooves.boot.jpa.repositories.PatientEventRepository
import grooves.boot.jpa.repositories.PatientHealthRepository
import com.github.rahulsom.grooves.util.VariableDepthCopier
import grooves.boot.jpa.util.VariableDepthCopier
import org.hibernate.engine.spi.SessionImplementor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE

@Transactional
@Component
@Query(aggregate = Patient, snapshot = PatientHealth)
class PatientHealthQuery implements QueryUtil<Patient, PatientEvent, PatientHealth> {

    @Autowired EntityManager entityManager
    @Autowired PatientHealthRepository patientHealthRepository
    @Autowired PatientEventRepository patientEventRepository

    @Override
    PatientHealth createEmptySnapshot() {
        new PatientHealth(deprecates: [], procedures: [])
    }

    @Override
    Optional<PatientAccount> getSnapshot(long startWithEvent, Patient aggregate) {
        def snapshots = startWithEvent == Long.MAX_VALUE ?
                patientHealthRepository.findAllByAggregateId(aggregate.id) :
                patientHealthRepository.findAllByAggregateIdAndLastEventLessThan(aggregate.id, startWithEvent)
        (snapshots ? Optional.of(snapshots[0]) : Optional.empty()) as Optional<PatientAccount>
    }

    @Override
    void detachSnapshot(PatientHealth retval) {
        new VariableDepthCopier<PatientHealth>().copy(retval)
    }

    @Override
    List<PatientEvent> getUncomputedEvents(Patient patient, PatientHealth lastSnapshot, long lastEvent) {
        patientEventRepository.getUncomputedEvents patient, lastSnapshot?.lastEvent ?: 0L, lastEvent
    }

    @Override
    boolean shouldEventsBeApplied(PatientHealth snapshot) {
        return true
    }

    @Override
    List<PatientEvent> findEventsForAggregates(List<Patient> aggregates) {
        patientEventRepository.findAllByAggregateIn(aggregates)
    }

    @Override
    void addToDeprecates(PatientHealth snapshot, Patient otherAggregate) {
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
    EventApplyOutcome onException(Exception e, PatientHealth snapshot, PatientEvent event) {
        snapshot.processingErrors++
        CONTINUE
    }

    EventApplyOutcome applyPatientCreated(PatientCreated event, PatientHealth snapshot) {
        snapshot.name = event.name
        CONTINUE
    }

    EventApplyOutcome applyProcedurePerformed(ProcedurePerformed event, PatientHealth snapshot) {
        snapshot.procedures << new Procedure(code: event.code, date: event.date)
        CONTINUE
    }

    EventApplyOutcome applyPaymentMade(PaymentMade event, PatientHealth snapshot) {
        // Ignore payments
        CONTINUE
    }

}
