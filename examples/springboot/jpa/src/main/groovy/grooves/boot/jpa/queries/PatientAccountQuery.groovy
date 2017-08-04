package grooves.boot.jpa.queries

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.groovy.transformations.Query
import com.github.rahulsom.grooves.queries.QuerySupport
import grooves.boot.jpa.domain.*
import grooves.boot.jpa.repositories.PatientAccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import rx.Observable

import javax.persistence.EntityManager

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static rx.Observable.empty
import static rx.Observable.from
import static rx.Observable.just

/**
 * Query for Patient Account
 *
 * @author Rahul Somasunderam
 */
@Transactional
@Component
//tag::documented[]
@Query(aggregate = Patient, snapshot = PatientAccount) // <1>
class PatientAccountQuery implements
        QuerySupport<Long, Patient, Long, PatientEvent, Long, PatientAccount,
                PatientAccountQuery> { // <2>

    //end::documented[]
    public static final String AGGREGATE = 'aggregate'
    public static final String POSITION = 'position'
    public static final String TIMESTAMP = 'timestamp'
    public static final String FROM = 'from'
    public static final String UNTIL = 'until'

    @Autowired EntityManager entityManager
    @Autowired PatientAccountRepository patientAccountRepository

    //tag::documented[]
    @Override
    Observable<PatientAccount> getSnapshot(long maxPosition, Patient aggregate) { // <3>
        //end::documented[]
        def snapshots = maxPosition == Long.MAX_VALUE ?
                patientAccountRepository.findAllByAggregateId(aggregate.id) :
                patientAccountRepository.findAllByAggregateIdAndLastEventPositionLessThan(
                        aggregate.id, maxPosition)
        snapshots ? just(detachSnapshot(snapshots[0])) : empty()
        //tag::documented[]
    }

    @Override
    Observable<PatientAccount> getSnapshot(Date maxTimestamp, Patient aggregate) { // <4>
        //end::documented[]
        def snapshots = maxTimestamp == null ?
                patientAccountRepository.findAllByAggregateId(aggregate.id) :
                patientAccountRepository.findAllByAggregateIdAndLastEventTimestampLessThan(
                        aggregate.id, maxTimestamp)
        snapshots ? just(detachSnapshot(snapshots[0])) : empty()
        //tag::documented[]
    }

    @Override
    boolean shouldEventsBeApplied(PatientAccount snapshot) { // <5>
        true
    }

    @Override
    Observable<EventApplyOutcome> onException(
            Exception e, PatientAccount snapshot, PatientEvent event) { // <6>
        snapshot.processingErrors++
        just CONTINUE
    }

    @Override
    Observable<PatientEvent> getUncomputedEvents(
            Patient patient, PatientAccount lastSnapshot, long version) { // <7>
        //end::documented[]
        def cb = entityManager.criteriaBuilder
        def q = cb.createQuery(PatientEvent)
        def root = q.from(PatientEvent)
        def criteria = q.select(root).where(
                cb.equal(root.get(AGGREGATE), cb.parameter(Patient, AGGREGATE)),
                cb.gt(root.get(POSITION), lastSnapshot?.lastEventPosition ?: 0L),
                cb.le(root.get(POSITION), version),
        )
        from(entityManager
                        .createQuery(criteria)
                        .setParameter(AGGREGATE, patient)
                        .resultList)
        //tag::documented[]
    }

    @Override
    Observable<PatientEvent> getUncomputedEvents(
            Patient aggregate, PatientAccount lastSnapshot, Date snapshotTime) { // <8>
        //end::documented[]
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

        from query.resultList
        //tag::documented[]
    }

    @Override
    PatientAccount createEmptySnapshot() { // <9>
        new PatientAccount(deprecates: [])
    }

    @Override
    void addToDeprecates(PatientAccount snapshot, Patient deprecatedAggregate) {
        snapshot.deprecates << deprecatedAggregate
    }

    Observable<EventApplyOutcome> applyPatientCreated(
            PatientCreated event, PatientAccount snapshot) { // <10>
        if (snapshot.aggregate == event.aggregate) {
            snapshot.name = event.name
        }
        just CONTINUE // <11>
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

    //end::documented[]
    PatientAccount detachSnapshot(PatientAccount snapshot) {
        def retval = new PatientAccount(
                lastEventPosition: snapshot.lastEventPosition,
                lastEventTimestamp: snapshot.lastEventTimestamp,
                deprecatedBy: snapshot.deprecatedBy,
                aggregate: snapshot.aggregate,
                processingErrors: snapshot.processingErrors,
                name: snapshot.name,
                balance: snapshot.balance,
                moneyMade: snapshot.moneyMade,
        )
        snapshot.deprecates.each { retval.deprecates.add it }
        retval
    }
    //tag::documented[]
}
//end::documented[]
