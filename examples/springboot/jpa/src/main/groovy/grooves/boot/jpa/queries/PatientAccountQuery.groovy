package grooves.boot.jpa.queries

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.queries.QuerySupport
import com.github.rahulsom.grooves.groovy.transformations.Query
import grooves.boot.jpa.domain.*
import grooves.boot.jpa.repositories.PatientAccountRepository
import grooves.boot.jpa.util.VariableDepthCopier
import org.hibernate.engine.spi.SessionImplementor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import rx.Observable

import javax.persistence.EntityManager

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static rx.Observable.just

/**
 * Query for Patient Account
 *
 * @author Rahul Somasunderam
 */
@Transactional
@Component
@Query(aggregate = Patient, snapshot = PatientAccount)
class PatientAccountQuery implements
        QuerySupport<Patient, Long, PatientEvent, Long, PatientAccount> {

    public static final String AGGREGATE = 'aggregate'
    public static final String POSITION = 'position'
    public static final String TIMESTAMP = 'timestamp'
    public static final String FROM = 'from'
    public static final String UNTIL = 'until'
    @Autowired
    EntityManager entityManager
    @Autowired
    PatientAccountRepository patientAccountRepository

    @Override
    PatientAccount createEmptySnapshot() {
        new PatientAccount(deprecates: [])
    }

    @Override
    Observable<PatientAccount> getSnapshot(long maxPosition, Patient aggregate) {
        def snapshots = maxPosition == Long.MAX_VALUE ?
                patientAccountRepository.findAllByAggregateId(aggregate.id) :
                patientAccountRepository.findAllByAggregateIdAndLastEventPositionLessThan(
                        aggregate.id, maxPosition)
        snapshots ? Observable.just(snapshots[0]) : Observable.empty()
    }

    @Override
    Observable<PatientAccount> getSnapshot(Date maxTimestamp, Patient aggregate) {
        def snapshots = maxTimestamp == null ?
                patientAccountRepository.findAllByAggregateId(aggregate.id) :
                patientAccountRepository.findAllByAggregateIdAndLastEventTimestampLessThan(
                        aggregate.id, maxTimestamp)
        snapshots ? Observable.just(snapshots[0]) : Observable.empty()
    }

    @Override
    void detachSnapshot(PatientAccount snapshot) {
        new VariableDepthCopier<PatientAccount>().copy(snapshot)
    }

    @Override
    Observable<PatientEvent> getUncomputedEvents(
            Patient patient, PatientAccount lastSnapshot, long version) {
        def cb = entityManager.criteriaBuilder
        def q = cb.createQuery(PatientEvent)
        def root = q.from(PatientEvent)
        def criteria = q.select(root).where(
                cb.equal(root.get(AGGREGATE), cb.parameter(Patient, AGGREGATE)),
                cb.gt(root.get(POSITION), lastSnapshot?.lastEventPosition ?: 0L),
                cb.le(root.get(POSITION), version),
        )
        Observable.from(
                entityManager
                        .createQuery(criteria)
                        .setParameter(AGGREGATE, patient)
                        .resultList
        )
    }

    @Override
    Observable<PatientEvent> getUncomputedEvents(
            Patient aggregate, PatientAccount lastSnapshot, Date snapshotTime) {
        def cb = entityManager.criteriaBuilder
        def q = cb.createQuery(PatientEvent)
        def root = q.from(PatientEvent)
        def criteria = lastSnapshot?.lastEventTimestamp ?
                q.select(root).where(
                        cb.equal(root.get(AGGREGATE), cb.parameter(Patient, AGGREGATE)),
                        cb.gt(root.get(TIMESTAMP), cb.parameter(Date, FROM)),
                        cb.le(root.get(TIMESTAMP), cb.parameter(Date, UNTIL)),
                ) :
                q.select(root).where(
                        cb.equal(root.get(AGGREGATE), cb.parameter(Patient, AGGREGATE)),
                        cb.le(root.get(TIMESTAMP), cb.parameter(Date, UNTIL)),
                )

        def query = entityManager.createQuery(criteria)
        query.setParameter(AGGREGATE, aggregate).setParameter(UNTIL, snapshotTime)
        if (lastSnapshot?.lastEventTimestamp) {
            query.setParameter(FROM, lastSnapshot.lastEventTimestamp)
        }

        Observable.from(query.resultList)
    }

    @Override
    boolean shouldEventsBeApplied(PatientAccount snapshot) {
        true
    }

    @Override
    Observable<PatientEvent> findEventsForAggregates(List<Patient> aggregates) {
        def cb = entityManager.criteriaBuilder
        def q = cb.createQuery(PatientEvent)
        def root = q.from(PatientEvent)
        def criteria = q.select(root).where(root.get(AGGREGATE).in(aggregates))
        Observable.from entityManager.createQuery(criteria).resultList
    }

    @Override
    void addToDeprecates(PatientAccount snapshot, Patient deprecatedAggregate) {
        snapshot.deprecates << deprecatedAggregate
    }

    @Override
    PatientEvent unwrapIfProxy(PatientEvent event) {
        entityManager.
                unwrap(SessionImplementor).
                persistenceContext.
                unproxy(event) as PatientEvent
    }

    @Override
    Observable<EventApplyOutcome> onException(
            Exception e, PatientAccount snapshot, PatientEvent event) {
        snapshot.processingErrors++
        just CONTINUE
    }

    Observable<EventApplyOutcome> applyPatientCreated(
            PatientCreated event, PatientAccount snapshot) {
        snapshot.name = snapshot.name ?: event.name
        just CONTINUE
    }

    Observable<EventApplyOutcome> applyProcedurePerformed(
            ProcedurePerformed event, PatientAccount snapshot) {
        snapshot.balance += event.cost
        just CONTINUE
    }

    Observable<EventApplyOutcome> applyPaymentMade(
            PaymentMade event, PatientAccount snapshot) {
        snapshot.balance -= event.amount
        snapshot.moneyMade += event.amount
        just CONTINUE
    }

}
