package grooves.grails.mongo

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.grails.GormQuerySupport
import com.github.rahulsom.grooves.groovy.transformations.Query
import grails.compiler.GrailsCompileStatic
import org.reactivestreams.Publisher

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static rx.Observable.just
import static rx.RxReactiveStreams.toPublisher

/**
 * Queries for the PatientAccount
 */
@GrailsCompileStatic
//tag::documented[]
@Query(aggregate = Patient, snapshot = PatientAccount) // <5>
class PatientAccountQuery
        implements GormQuerySupport<Long, Patient, Long, PatientEvent,
                String, PatientAccount> { // <6>

    final Class<PatientEvent> eventClass = PatientEvent // <7>
    final Class<PatientAccount> snapshotClass = PatientAccount // <8>

    @Override
    PatientAccount createEmptySnapshot() { new PatientAccount(deprecates: []) } // <9>

    @Override
    boolean shouldEventsBeApplied(PatientAccount snapshot) { // <10>
        true
    }

    @Override
    void addToDeprecates(PatientAccount snapshot, Patient deprecatedAggregate) {
        snapshot.addToDeprecatesIds(deprecatedAggregate.id)
    }

    @Override
    Publisher<EventApplyOutcome> onException( // <11>
            Exception e, PatientAccount snapshot, PatientEvent event) {
        snapshot.processingErrors << e.message
        toPublisher(just(CONTINUE))
    }

    Publisher<EventApplyOutcome> applyPatientCreated( // <12>
            PatientCreated event, PatientAccount snapshot) {
        if (snapshot.aggregateId == event.aggregate.id) {
            snapshot.name = event.name
        }
        toPublisher(just(CONTINUE)) // <13>
    }

    //end::documented[]
    Publisher<EventApplyOutcome> applyProcedurePerformed(
            ProcedurePerformed event, PatientAccount snapshot) {
        snapshot.balance += event.cost
        toPublisher(just(CONTINUE))
    }

    Publisher<EventApplyOutcome> applyPaymentMade(
            PaymentMade event, PatientAccount snapshot) {
        snapshot.balance -= event.amount
        snapshot.moneyMade += event.amount
        toPublisher(just(CONTINUE))
    }

    @Override
    PatientAccount detachSnapshot(PatientAccount snapshot) {
        if (snapshot.isAttached()) {
            snapshot.discard()
            snapshot.id = null
        }
        snapshot
    }
    //tag::documented[]
}
//end::documented[]
