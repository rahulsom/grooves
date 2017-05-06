package grooves.grails.mongo

import com.github.rahulsom.grooves.groovy.transformations.Query
import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.grails.GormQuerySupport
import grails.compiler.GrailsCompileStatic
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import rx.Observable

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE
import static rx.Observable.just

/**
 * Queries for PatientHealth data
 */
@Query(aggregate = Patient, snapshot = PatientHealth)
@GrailsCompileStatic
@SuppressWarnings(['DuplicateStringLiteral'])
class PatientHealthQuery implements
        GormQuerySupport<Patient, Long, PatientEvent, String, PatientHealth> {

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
    PatientEvent unwrapIfProxy(PatientEvent event) {
        GrailsHibernateUtil.unwrapIfProxy(event) as PatientEvent
    }

    @Override
    Observable<EventApplyOutcome> onException(
            Exception e, PatientHealth snapshot, PatientEvent event) {
        snapshot.processingErrors << e.message
        just CONTINUE
    }

    Observable<EventApplyOutcome> applyPatientCreated(
            PatientCreated event, PatientHealth snapshot) {
        snapshot.name = snapshot.name ?: event.name
        just CONTINUE
    }

    Observable<EventApplyOutcome> applyProcedurePerformed(
            ProcedurePerformed event, PatientHealth snapshot) {
        snapshot.addToProcedures(code: event.code, date: event.timestamp)
        just CONTINUE
    }

    @SuppressWarnings(['UnusedMethodParameter'])
    Observable<EventApplyOutcome> applyPaymentMade(
            PaymentMade event, PatientHealth snapshot) {
        just CONTINUE // Ignore payments
    }

    @SuppressWarnings(['UnusedMethodParameter'])
    Observable<EventApplyOutcome> applyPatientAddedToZipcode(
            PatientAddedToZipcode event, PatientHealth snapshot) {
        just CONTINUE // Ignore zip change
    }

}
