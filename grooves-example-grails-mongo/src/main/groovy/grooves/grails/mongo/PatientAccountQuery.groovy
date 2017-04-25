package grooves.grails.mongo

import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.grails.GormQuerySupport
import com.github.rahulsom.grooves.groovy.transformations.Query
import grails.compiler.GrailsCompileStatic
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE

/**
 * Queries for the PatientAccount
 */
@Query(aggregate = Patient, snapshot = PatientAccount)
@GrailsCompileStatic
class PatientAccountQuery implements
        GormQuerySupport<Patient, Long, PatientEvent, String, PatientAccount> {

    final Class<PatientAccount> snapshotClass = PatientAccount
    final Class<PatientEvent> eventClass = PatientEvent

    @Override
    PatientAccount createEmptySnapshot() { new PatientAccount(deprecates: []) }

    @Override
    boolean shouldEventsBeApplied(PatientAccount snapshot) {
        true
    }

    @Override
    void addToDeprecates(PatientAccount snapshot, Patient deprecatedAggregate) {
        snapshot.addToDeprecatesIds(deprecatedAggregate.id)
    }

    @Override
    PatientEvent unwrapIfProxy(PatientEvent event) {
        GrailsHibernateUtil.unwrapIfProxy(event) as PatientEvent
    }

    @Override
    EventApplyOutcome onException(Exception e, PatientAccount snapshot, PatientEvent event) {
        snapshot.processingErrors << e.message
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

    @SuppressWarnings(['DuplicateStringLiteral', 'UnusedMethodParameter', ])
    EventApplyOutcome applyPatientAddedToZipcode(
            PatientAddedToZipcode event, PatientAccount snapshot) {
        CONTINUE // Ignore zip change
    }
}
