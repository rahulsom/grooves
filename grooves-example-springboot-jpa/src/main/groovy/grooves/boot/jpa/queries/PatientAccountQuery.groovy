package grooves.boot.jpa.queries

import com.github.rahulsom.grooves.transformations.Query
import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.queries.QuerySupport
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

@Transactional
@Component
@Query(aggregate = Patient, snapshot = PatientAccount)
class PatientAccountQuery implements QuerySupport<Patient, Long, PatientEvent, Long, PatientAccount> {

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
                patientAccountRepository.findAllByAggregateIdAndLastEventPositionLessThan(aggregate.id, maxPosition)
        snapshots ? Observable.just(snapshots[0]) : Observable.empty()
    }

    @Override
    Observable<PatientAccount> getSnapshot(Date maxTimestamp, Patient aggregate) {
        def snapshots = maxTimestamp == null ?
                patientAccountRepository.findAllByAggregateId(aggregate.id) :
                patientAccountRepository.findAllByAggregateIdAndLastEventTimestampLessThan(aggregate.id, maxTimestamp)
        snapshots ? Observable.just(snapshots[0]) : Observable.empty()
    }

    @Override
    void detachSnapshot(PatientAccount snapshot) {
        new VariableDepthCopier<PatientAccount>().copy(snapshot)
    }

    @Override
    Observable<PatientEvent> getUncomputedEvents(Patient patient, PatientAccount lastSnapshot, long version) {
        def cb = entityManager.criteriaBuilder
        def q = cb.createQuery(PatientEvent)
        def root = q.from(PatientEvent)
        def criteria = q.select(root).where(
                cb.equal(root.get('aggregate'), cb.parameter(Patient, 'aggregate')),
                cb.gt(root.get('position'), lastSnapshot?.lastEventPosition ?: 0L),
                cb.le(root.get('position'), version),
        )
        Observable.from (entityManager.createQuery(criteria).setParameter('aggregate', patient).resultList)
    }

    @Override
    Observable<PatientEvent> getUncomputedEvents(Patient aggregate, PatientAccount lastSnapshot, Date snapshotTime) {
        def cb = entityManager.criteriaBuilder
        def q = cb.createQuery(PatientEvent)
        def root = q.from(PatientEvent)
        def criteria = lastSnapshot?.lastEventTimestamp ?
                q.select(root).where(
                        cb.equal(root.get('aggregate'), cb.parameter(Patient, 'aggregate')),
                        cb.gt(root.get('timestamp'), cb.parameter(Date, 'from')),
                        cb.le(root.get('timestamp'), cb.parameter(Date, 'until')),
                ) :
                q.select(root).where(
                        cb.equal(root.get('aggregate'), cb.parameter(Patient, 'aggregate')),
                        cb.le(root.get('timestamp'), cb.parameter(Date, 'until')),
                )

        def query = entityManager.createQuery(criteria)
        query.setParameter('aggregate', aggregate).setParameter('until', snapshotTime)
        if (lastSnapshot?.lastEventTimestamp) {
            query.setParameter('from', lastSnapshot.lastEventTimestamp)
        }

        Observable.from(query.resultList)
    }

    @Override
    boolean shouldEventsBeApplied(PatientAccount snapshot) {
        return true
    }

    @Override
    Observable<PatientEvent> findEventsForAggregates(List<Patient> aggregates) {
        def cb = entityManager.criteriaBuilder
        def q = cb.createQuery(PatientEvent)
        def root = q.from(PatientEvent)
        def criteria = q.select(root).where(root.get('aggregate').in(aggregates))
        Observable.from entityManager.createQuery(criteria).resultList
    }

    @Override
    void addToDeprecates(PatientAccount snapshot, Patient deprecatedAggregate) {
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
