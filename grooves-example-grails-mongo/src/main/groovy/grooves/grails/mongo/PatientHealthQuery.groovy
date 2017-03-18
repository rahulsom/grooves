package grooves.grails.mongo

import com.github.rahulsom.grooves.transformations.Query
import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.grails.GormQuerySupport
import grails.compiler.GrailsCompileStatic
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE

@Query(aggregate = Patient, snapshot = PatientHealth)
@GrailsCompileStatic
class PatientHealthQuery extends GormQuerySupport<Patient, Long, PatientEvent, String, PatientHealth> {

    PatientHealthQuery() {
        super(PatientEvent, PatientHealth)
    }

    @Override
    PatientHealth createEmptySnapshot() { new PatientHealth(deprecates: []) }

    @Override
    boolean shouldEventsBeApplied(PatientHealth snapshot) {
        true
    }

    @Override
    void addToDeprecates(PatientHealth snapshot, Patient otherAggregate) {
        snapshot.addToDeprecatesIds(otherAggregate.id)
    }

    @Override
    PatientEvent unwrapIfProxy(PatientEvent event) {
        GrailsHibernateUtil.unwrapIfProxy(event) as PatientEvent
    }

    @Override
    EventApplyOutcome onException(Exception e, PatientHealth snapshot, PatientEvent event) {
        snapshot.processingErrors << e.message
        CONTINUE
    }

    EventApplyOutcome applyPatientCreated(PatientCreated event, PatientHealth snapshot) {
        snapshot.name = event.name
        CONTINUE
    }

    EventApplyOutcome applyProcedurePerformed(ProcedurePerformed event, PatientHealth snapshot) {
        snapshot.addToProcedures(code: event.code, date: event.timestamp)
        CONTINUE
    }

    EventApplyOutcome applyPaymentMade(PaymentMade event, PatientHealth snapshot) {
        CONTINUE // Ignore payments
    }

    EventApplyOutcome applyPatientAddedToZipcode(PatientAddedToZipcode event, PatientHealth snapshot) {
        CONTINUE // Ignore zip change
    }

}
