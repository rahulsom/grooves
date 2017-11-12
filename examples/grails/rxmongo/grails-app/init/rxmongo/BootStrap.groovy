package rxmongo

import com.github.rahulsom.grooves.test.GroovyEventsDsl
import com.github.rahulsom.grooves.test.OnSpec

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
        setupRingoStarr()
        setupPaulMcCartney()
        setupFreddieMercury()
        setupTinaFeyAndSarahPalin()
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

    private Patient setupRingoStarr() {
        def patient = new Patient(uniqueId: '43').save(flush: true, failOnError: true)
                .toBlocking().single()

        on(patient) {
            apply new PatientCreated(name: 'Ringo Starr')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery

            apply new ProcedurePerformed(code: 'FLUSHOT', cost: 32.40)
            apply new PaymentMade(amount: 180.00)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }
    }

    private Patient setupPaulMcCartney() {
        def patient = new Patient(uniqueId: '44').save(flush: true, failOnError: true)
                .toBlocking().single()

        on(patient) {
            apply new PatientCreated(name: 'Paul McCartney')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            def gluc = apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)
            apply new PatientEventReverted(revertedEventId: gluc.id)
            def pmt = apply new PaymentMade(amount: 30.00)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery

            apply new PatientEventReverted(revertedEventId: pmt.id)
            apply new PaymentMade(amount: 60.00)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery

            apply new PaymentMade(amount: 60.00)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }

    }

    private Patient setupFreddieMercury() {
        def patient = new Patient(uniqueId: '45').save(flush: true, failOnError: true)
                .toBlocking().single()
        def patient2 = new Patient(uniqueId: '46').save(flush: true, failOnError: true)
                .toBlocking().single()

        on(patient) {
            apply new PatientCreated(name: 'Farrokh Bulsara')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }

        on(patient2) {
            apply new PatientCreated(name: 'Freddie Mercury')
            apply new PaymentMade(amount: 100.25)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }

        currDate += 1
        merge(patient, patient2)
        patient
    }

    private Patient setupTinaFeyAndSarahPalin() {
        def patient = new Patient(uniqueId: '47').save(flush: true, failOnError: true)
                .toBlocking().single()
        def patient2 = new Patient(uniqueId: '48').save(flush: true, failOnError: true)
                .toBlocking().single()

        on(patient) {
            apply new PatientCreated(name: 'Tina Fey')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }

        on(patient2) {
            apply new PatientCreated(name: 'Sarah Palin')
            apply new PaymentMade(amount: 100.25)

            snapshotWith patientAccountQuery
            snapshotWith patientHealthQuery
        }

        currDate += 1
        def mergeEvent = merge(patient, patient2)
        on(mergeEvent.aggregate) {
            apply new PatientEventReverted(revertedEventId: mergeEvent.id)
        }
        on(patient2) {
            apply new PatientEventReverted(revertedEventId: mergeEvent.converse.id)
        }
        patient
    }

    /**
     *
     * @param self The aggregate to be deprecated
     * @param into The aggregate to survive
     * @return
     */
    private PatientDeprecatedBy merge(Patient self, Patient into) {
        def e1 = new PatientDeprecatedBy(aggregate: self, deprecator: into, timestamp: currDate,
                position: PatientEvent.countByAggregate(self).toBlocking().single() + 1, )
        def e2 = new PatientDeprecates(aggregate: into, deprecated: self, timestamp: currDate,
                converse: e1,
                position: PatientEvent.countByAggregate(into).toBlocking().single() + 1, )
        e1.converse = e2
        e2.save(flush: true, failOnError: true).toBlocking().single()
        e2.converse
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
