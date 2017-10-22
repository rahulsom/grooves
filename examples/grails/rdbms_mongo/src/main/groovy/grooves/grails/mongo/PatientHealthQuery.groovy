package grooves.grails.mongo

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.grails.GormQuerySupport
import com.github.rahulsom.grooves.groovy.transformations.Query
import grails.compiler.GrailsCompileStatic
import org.reactivestreams.Publisher

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static rx.Observable.just

/**
 * Queries for PatientHealth data
 */
@Query(aggregate = Patient, snapshot = PatientHealth)
@GrailsCompileStatic
@SuppressWarnings(['DuplicateStringLiteral'])
class PatientHealthQuery implements
        GormQuerySupport<Long, Patient, Long, PatientEvent, String, PatientHealth,
                PatientHealthQuery> {

    final Class<PatientHealth> snapshotClass = PatientHealth
    final Class<PatientEvent> eventClass = PatientEvent

    @Override
    PatientHealth createEmptySnapshot() { new PatientHealth(deprecates: []) }

    @Override
    boolean shouldEventsBeApplied(PatientHealth snapshot) {
        true
    }

    @Override
    void addToDeprecates(PatientHealth snapshot, Patient deprecatedAggregate) {
        snapshot.addToDeprecatesIds(deprecatedAggregate.id)
    }

    @Override
    Publisher<EventApplyOutcome> onException(
            Exception e, PatientHealth snapshot, PatientEvent event) {
        snapshot.processingErrors << e.message
        just CONTINUE toPublisher()
    }

    Publisher<EventApplyOutcome> applyPatientCreated(
            PatientCreated event, PatientHealth snapshot) {
        if (snapshot.aggregateId == event.aggregateId) {
            snapshot.name = event.name
        }
        just CONTINUE toPublisher()
    }

    Publisher<EventApplyOutcome> applyProcedurePerformed(
            ProcedurePerformed event, PatientHealth snapshot) {
        snapshot.addToProcedures(code: event.code, date: event.timestamp)
        just CONTINUE toPublisher()
    }

    @SuppressWarnings(['UnusedMethodParameter'])
    Publisher<EventApplyOutcome> applyPaymentMade(
            PaymentMade event, PatientHealth snapshot) {
        just CONTINUE toPublisher()// Ignore payments
    }

    @SuppressWarnings(['UnusedMethodParameter'])
    Publisher<EventApplyOutcome> applyPatientAddedToZipcode(
            PatientAddedToZipcode event, PatientHealth snapshot) {
        just CONTINUE toPublisher()// Ignore zip change
    }

    @Override
    PatientHealth detachSnapshot(PatientHealth snapshot) {
        if (snapshot.isAttached()) {
            snapshot.discard()
            snapshot.id = null
        }
        snapshot
    }
}
