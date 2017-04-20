package grooves.boot.jpa.queries

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.queries.QuerySupport
import com.github.rahulsom.grooves.transformations.Query
import grooves.boot.jpa.domain.*
import grooves.boot.jpa.repositories.PatientEventRepository
import grooves.boot.jpa.repositories.PatientHealthRepository
import grooves.boot.jpa.util.VariableDepthCopier
import org.hibernate.engine.spi.SessionImplementor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import rx.Observable

import javax.persistence.EntityManager

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE

@Transactional
@Component
@Query(aggregate = Patient, snapshot = PatientHealth)
class PatientHealthQuery implements QuerySupport<Patient, Long, PatientEvent, Long, PatientHealth> {

    @Autowired EntityManager entityManager
    @Autowired PatientHealthRepository patientHealthRepository
    @Autowired PatientEventRepository patientEventRepository

    @Override
    PatientHealth createEmptySnapshot() {
        new PatientHealth(deprecates: [], procedures: [])
    }

    @Override
    Observable<PatientHealth> getSnapshot(long maxPosition, Patient aggregate) {
        def snapshots = maxPosition == Long.MAX_VALUE ?
                patientHealthRepository.findAllByAggregateId(aggregate.id) :
                patientHealthRepository.findAllByAggregateIdAndLastEventPositionLessThan(aggregate.id, maxPosition)
        snapshots ? Observable.just(snapshots[0]) : Observable.empty()
    }

    @Override
    Observable<PatientHealth> getSnapshot(Date maxTimestamp, Patient aggregate) {
        def snapshots = maxTimestamp == null ?
                patientHealthRepository.findAllByAggregateId(aggregate.id) :
                patientHealthRepository.findAllByAggregateIdAndLastEventTimestampLessThan(aggregate.id, maxTimestamp)
        snapshots ? Observable.just(snapshots[0]) : Observable.empty()
    }

    @Override
    void detachSnapshot(PatientHealth snapshot) {
        new VariableDepthCopier<PatientHealth>().copy(snapshot)
    }

    @Override
    Observable<PatientEvent> getUncomputedEvents(Patient patient, PatientHealth lastSnapshot, long version) {
        Observable.from (patientEventRepository.getUncomputedEventsByVersion(patient, lastSnapshot?.lastEventPosition ?: 0L, version))
    }

    @Override
    Observable<PatientEvent> getUncomputedEvents(Patient patient, PatientHealth lastSnapshot, Date snapshotTime) {
        Observable.from(
                lastSnapshot?.lastEventTimestamp ?
                        patientEventRepository.getUncomputedEventsByDateRange(patient, lastSnapshot.lastEventTimestamp, snapshotTime) :
                        patientEventRepository.getUncomputedEventsUntilDate(patient, snapshotTime))
    }

    @Override
    boolean shouldEventsBeApplied(PatientHealth snapshot) {
        return true
    }

    @Override
    Observable<PatientEvent> findEventsForAggregates(List<Patient> aggregates) {
        Observable.from patientEventRepository.findAllByAggregateIn(aggregates)
    }

    @Override
    void addToDeprecates(PatientHealth snapshot, Patient deprecatedAggregate) {
        snapshot.deprecates << deprecatedAggregate
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
        snapshot.procedures << new Procedure(code: event.code, date: event.timestamp)
        CONTINUE
    }

    EventApplyOutcome applyPaymentMade(PaymentMade event, PatientHealth snapshot) {
        // Ignore payments
        CONTINUE
    }

}
