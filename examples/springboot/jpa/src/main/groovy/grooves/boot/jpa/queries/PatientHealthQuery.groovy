package grooves.boot.jpa.queries

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.groovy.transformations.Query
import com.github.rahulsom.grooves.queries.QuerySupport
import grooves.boot.jpa.domain.*
import grooves.boot.jpa.repositories.PatientEventRepository
import grooves.boot.jpa.repositories.PatientHealthRepository
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static io.reactivex.Flowable.*

/**
 * Query for Patient Health
 *
 * @author Rahul Somasunderam
 */
@Transactional
@Component
@Query(aggregate = Patient, snapshot = PatientHealth)
class PatientHealthQuery implements
        QuerySupport<Long, Patient, Long, PatientEvent, Long, PatientHealth, PatientHealthQuery> {

    @Autowired EntityManager entityManager
    @Autowired PatientHealthRepository patientHealthRepository
    @Autowired PatientEventRepository patientEventRepository

    @Override
    PatientHealth createEmptySnapshot() {
        new PatientHealth(deprecates: [], procedures: [])
    }

    @Override
    Publisher<PatientHealth> getSnapshot(long maxPosition, Patient aggregate) {
        def snapshots = maxPosition == Long.MAX_VALUE ?
                patientHealthRepository.findAllByAggregateId(aggregate.id) :
                patientHealthRepository.findAllByAggregateIdAndLastEventPositionLessThan(
                        aggregate.id, maxPosition)
        snapshots ? just(detachSnapshot(snapshots[0])) : empty()
    }

    @Override
    Publisher<PatientHealth> getSnapshot(Date maxTimestamp, Patient aggregate) {
        def snapshots = maxTimestamp == null ?
                patientHealthRepository.findAllByAggregateId(aggregate.id) :
                patientHealthRepository.findAllByAggregateIdAndLastEventTimestampLessThan(
                        aggregate.id, maxTimestamp)
        snapshots ? just(detachSnapshot(snapshots[0])) : empty()
    }

    @Override
    Publisher<PatientEvent> getUncomputedEvents(
            Patient patient, PatientHealth lastSnapshot, long version) {
        fromIterable(patientEventRepository.getUncomputedEventsByVersion(
                patient, lastSnapshot?.lastEventPosition ?: 0L, version))
    }

    @Override
    Publisher<PatientEvent> getUncomputedEvents(
            Patient patient, PatientHealth lastSnapshot, Date snapshotTime) {
        fromIterable(lastSnapshot?.lastEventTimestamp ?
                patientEventRepository.getUncomputedEventsByDateRange(
                        patient, lastSnapshot.lastEventTimestamp, snapshotTime) :
                patientEventRepository.getUncomputedEventsUntilDate(
                        patient, snapshotTime))
    }

    @Override
    boolean shouldEventsBeApplied(PatientHealth snapshot) {
        true
    }

    @Override
    void addToDeprecates(PatientHealth snapshot, Patient deprecatedAggregate) {
        snapshot.deprecates << deprecatedAggregate
    }

    @Override
    Publisher<EventApplyOutcome> onException(
            Exception e, PatientHealth snapshot, PatientEvent event) {
        snapshot.processingErrors++
        just(CONTINUE)
    }

    Publisher<EventApplyOutcome> applyPatientCreated(
            PatientCreated event, PatientHealth snapshot) {
        if (snapshot.aggregate == event.aggregate) {
            snapshot.name = event.name
        }
        just(CONTINUE)
    }

    Publisher<EventApplyOutcome> applyProcedurePerformed(
            ProcedurePerformed event, PatientHealth snapshot) {
        snapshot.procedures << new Procedure(code: event.code, date: event.timestamp)
        just(CONTINUE)
    }

    @SuppressWarnings('UnusedMethodParameter')
    Publisher<EventApplyOutcome> applyPaymentMade(
            PaymentMade event, PatientHealth snapshot) {
        // Ignore payments
        just(CONTINUE)
    }

    PatientHealth detachSnapshot(PatientHealth snapshot) {
        def retval = new PatientHealth(
                lastEventPosition: snapshot.lastEventPosition,
                lastEventTimestamp: snapshot.lastEventTimestamp,
                deprecatedBy: snapshot.deprecatedBy,
                aggregate: snapshot.aggregate,
                processingErrors: snapshot.processingErrors,
                name: snapshot.name,
                procedures: [],
                deprecates: [],
        )
        snapshot.deprecates.each { retval.deprecates.add it }
        snapshot.procedures.each {
            retval.procedures.add(new Procedure(code: it.code, date: it.date))
        }
        retval
    }
}
