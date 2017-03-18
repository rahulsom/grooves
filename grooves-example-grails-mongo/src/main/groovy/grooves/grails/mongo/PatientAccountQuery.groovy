package grooves.grails.mongo

import com.github.rahulsom.grooves.transformations.Query
import com.github.rahulsom.grooves.api.EventApplyOutcome
import com.github.rahulsom.grooves.grails.GormQuerySupport
import grails.compiler.GrailsCompileStatic
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import static com.github.rahulsom.grooves.api.EventApplyOutcome.CONTINUE

@Query(aggregate = Patient, snapshot = PatientAccount)
@GrailsCompileStatic
class PatientAccountQuery extends GormQuerySupport<Patient, Long, PatientEvent, String, PatientAccount> {

    PatientAccountQuery() {
        super(PatientEvent, PatientAccount)
    }

    @Override
    PatientAccount createEmptySnapshot() { new PatientAccount(deprecates: []) }

    @Override
    boolean shouldEventsBeApplied(PatientAccount snapshot) {
        true
    }

    @Override
    void addToDeprecates(PatientAccount snapshot, Patient otherAggregate) {
        snapshot.addToDeprecatesIds(otherAggregate.id)
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

    EventApplyOutcome applyPatientAddedToZipcode(PatientAddedToZipcode event, PatientAccount snapshot) {
        CONTINUE // Ignore zip change
    }
}
