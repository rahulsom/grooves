package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.grails.GormQuerySupport
import com.github.rahulsom.grooves.groovy.transformations.Query
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import groovy.json.JsonSlurper
import org.reactivestreams.Publisher

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static rx.Observable.just
import static rx.RxReactiveStreams.toPublisher

/**
 * Performs a query for Patient Account information
 *
 * @author Rahul Somasunderam
 */
@GrailsCompileStatic
//tag::documented[]
@Query(aggregate = Patient, snapshot = PatientAccount) // <5>
class PatientAccountQuery implements
        GormQuerySupport<Long, Patient, Long, PatientEvent, Long, PatientAccount> { // <6>

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
        snapshot.addToDeprecates(deprecatedAggregate)
    }

    @Override
    Publisher<EventApplyOutcome> onException( // <11>
            Exception e, PatientAccount snapshot, PatientEvent event) {
        // ignore for now
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

    @Override PatientAccount detachSnapshot(PatientAccount snapshot) {
        new JsonSlurper().parseText((snapshot as JSON).toString()) as PatientAccount
    }
    //tag::documented[]
}
//end::documented[]
