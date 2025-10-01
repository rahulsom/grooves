package grooves.boot.jpa.queries

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.groovy.transformations.Query
import com.github.rahulsom.grooves.queries.QuerySupport
import grooves.boot.jpa.domain.*
import grooves.boot.jpa.repositories.PatientAccountRepository
import org.jetbrains.annotations.NotNull
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import jakarta.persistence.EntityManager

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static io.reactivex.Flowable.fromIterable
import static io.reactivex.Flowable.just

/**
 * Query for Patient Account
 *
 * @author Rahul Somasunderam
 */
@Transactional
@Component
// tag::documented[]
@Query(aggregate = Patient, snapshot = PatientAccount) // <1>
class PatientAccountQuery implements
        QuerySupport<Patient, Long, PatientEvent, Long, PatientAccount> { // <2>

    // end::documented[]
    public static final String AGGREGATE = 'aggregate'
    public static final String POSITION = 'position'
    public static final String TIMESTAMP = 'timestamp'
    public static final String FROM = 'from'
    public static final String UNTIL = 'until'

    @Autowired EntityManager entityManager
    @Autowired PatientAccountRepository patientAccountRepository

    // tag::documented[]
    @NotNull
    @Override
    Publisher<PatientAccount> getSnapshot(long maxPosition, @NotNull Patient aggregate) { // <3>
        // end::documented[]
        def snapshots = maxPosition == Long.MAX_VALUE ?
                patientAccountRepository.findAllByAggregateId(aggregate.id) :
                patientAccountRepository.findAllByAggregateIdAndLastEventPositionLessThan(
                        aggregate.id, maxPosition)

        fromIterable(snapshots).firstElement().toFlowable()
        // tag::documented[]
    }

    @NotNull
    @Override
    Publisher<PatientAccount> getSnapshot(Date maxTimestamp, @NotNull Patient aggregate) { // <4>
        // end::documented[]
        def snapshots = maxTimestamp == null ?
                patientAccountRepository.findAllByAggregateId(aggregate.id) :
                patientAccountRepository.findAllByAggregateIdAndLastEventTimestampLessThan(
                        aggregate.id, maxTimestamp)

        fromIterable(snapshots).firstElement().toFlowable()
        // tag::documented[]
    }

    @Override
    boolean shouldEventsBeApplied(@NotNull PatientAccount snapshot) { // <5>
        true
    }

    @NotNull
    @Override
    Publisher<EventApplyOutcome> onException(
            @NotNull Exception e, @NotNull PatientAccount snapshot, @NotNull PatientEvent event) { // <6>
        snapshot.processingErrors++
        just(CONTINUE)
    }

    @NotNull
    @Override
    Publisher<PatientEvent> getUncomputedEvents(
            @NotNull Patient patient, PatientAccount lastSnapshot, long version) { // <7>
        // end::documented[]
        def cb = entityManager.criteriaBuilder
        def q = cb.createQuery(PatientEvent)
        def root = q.from(PatientEvent)
        def criteria = q.select(root).where(
                cb.equal(root.get(AGGREGATE), cb.parameter(Patient, AGGREGATE)),
                cb.gt(root.get(POSITION), lastSnapshot?.lastEventPosition ?: 0L),
                cb.le(root.get(POSITION), version),
        )
        fromIterable(entityManager
                        .createQuery(criteria)
                        .setParameter(AGGREGATE, patient)
                        .resultList)
        // tag::documented[]
    }

    @NotNull
    @Override Publisher<PatientEvent> getUncomputedEvents(
            @NotNull Patient aggregate, PatientAccount lastSnapshot, @NotNull Date snapshotTime) { // <8>
        // end::documented[]
        def cb = entityManager.criteriaBuilder
        def q = cb.createQuery(PatientEvent)
        def root = q.from(PatientEvent)
        def criteria = lastSnapshot?.lastEventTimestamp ?
                q.select(root).where(
                        cb.equal(root.get(AGGREGATE), cb.parameter(Patient, AGGREGATE)),
                        cb.greaterThan(root.get(TIMESTAMP), cb.parameter(Date, FROM)),
                        cb.lessThanOrEqualTo(root.get(TIMESTAMP), cb.parameter(Date, UNTIL)),
                ) :
                q.select(root).where(
                        cb.equal(root.get(AGGREGATE), cb.parameter(Patient, AGGREGATE)),
                        cb.lessThanOrEqualTo(root.get(TIMESTAMP), cb.parameter(Date, UNTIL)),
                )

        def query = entityManager.createQuery(criteria)
        query.setParameter(AGGREGATE, aggregate).setParameter(UNTIL, snapshotTime)
        if (lastSnapshot?.lastEventTimestamp) {
            query.setParameter(FROM, lastSnapshot.lastEventTimestamp)
        }

        fromIterable(query.resultList)
        // tag::documented[]
    }

    @NotNull
    @Override PatientAccount createEmptySnapshot() { // <9>
        new PatientAccount(deprecates: [])
    }

    @Override
    void addToDeprecates(@NotNull PatientAccount snapshot, @NotNull Patient deprecatedAggregate) {
        snapshot.deprecates << deprecatedAggregate
    }

    Publisher<EventApplyOutcome> applyPatientCreated(
            PatientCreated event, PatientAccount snapshot) { // <10>
        if (snapshot.aggregate == event.aggregate) {
            snapshot.name = event.name
        }
        just(CONTINUE) // <11>
    }

    Publisher<EventApplyOutcome> applyProcedurePerformed(
            ProcedurePerformed event, PatientAccount snapshot) {
        snapshot.balance += event.cost
        just(CONTINUE)
    }

    Publisher<EventApplyOutcome> applyPaymentMade(
            PaymentMade event, PatientAccount snapshot) {
        snapshot.balance -= event.amount
        snapshot.moneyMade += event.amount
        just(CONTINUE)
    }

    // end::documented[]
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
    // tag::documented[]
}
// end::documented[]
