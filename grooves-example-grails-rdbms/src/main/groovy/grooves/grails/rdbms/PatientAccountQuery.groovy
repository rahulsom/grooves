package grooves.grails.rdbms

import com.github.rahulsom.grooves.groovy.transformations.Query
import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.grails.GormQuerySupport
import grails.compiler.GrailsCompileStatic
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE

/**
 * Performs a query for Patient Account information
 *
 * @author Rahul Somasunderam
 */
@Query(aggregate = Patient, snapshot = PatientAccount)
@GrailsCompileStatic
class PatientAccountQuery implements
        GormQuerySupport<Patient, Long, PatientEvent, Long, PatientAccount> {

    @Override
    PatientAccount createEmptySnapshot() { new PatientAccount(deprecates: []) }

    @Override
    boolean shouldEventsBeApplied(PatientAccount snapshot) {
        true
    }

    @Override
    void addToDeprecates(PatientAccount snapshot, Patient deprecatedAggregate) {
        snapshot.addToDeprecates(deprecatedAggregate)
    }

    @Override
    PatientEvent unwrapIfProxy(PatientEvent event) {
        GrailsHibernateUtil.unwrapIfProxy(event) as PatientEvent
    }

    @Override
    EventApplyOutcome onException(Exception e, PatientAccount snapshot, PatientEvent event) {
        // ignore exceptions. Look at the mongo equivalent to see one possible way to handle
        // exceptions
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

    final Class<PatientAccount> snapshotClass = PatientAccount
    final Class<PatientEvent> eventClass = PatientEvent
}
