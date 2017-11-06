package rxmongo

import com.github.rahulsom.grooves.api.OnSpec
import com.github.rahulsom.grooves.groovy.GroovyEventsDsl

import java.util.function.Consumer

/**
 * Initializes Application with usable data.
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['DuplicateNumberLiteral', 'DuplicateStringLiteral'])
class BootStrap {

    private final PatientAccountQuery patientAccountQuery = new PatientAccountQuery()
    private final PatientHealthQuery patientHealthQuery = new PatientHealthQuery()

    def init = { servletContext ->
        setupJohnLennon()
    }

    private Patient setupJohnLennon() {
        def patient = new Patient(uniqueId: '42').save(flush: true, failOnError: true)
                .toBlocking().single()

        on(patient) {
            apply new PatientCreated(name: 'John Lennon')
            apply new ProcedurePerformed(code: 'FLUSHOT', cost: 32.40)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery

            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new PaymentMade(amount: 180.00)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }
    }

    Date currDate = Date.parse('yyyy-MM-dd', '2016-01-01')

    Patient on(Patient patient, @DelegatesTo(OnSpec) Closure closure) {
        def eventSaver = {
            it.save(flush: true, failOnError: true).toBlocking().single()
        } as Consumer<PatientEvent>
        def positionSupplier = {
            (PatientEvent.countByAggregate(patient).toBlocking().single()?.longValue() ?: 0l) + 1 }
        def dateSupplier = { currDate += 1; currDate }
        new GroovyEventsDsl<String, Patient, String, PatientEvent>().on(
                patient, eventSaver, positionSupplier, dateSupplier, closure)
    }

    def destroy = {
    }
}
