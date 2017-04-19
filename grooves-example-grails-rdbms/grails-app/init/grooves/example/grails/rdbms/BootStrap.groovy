package grooves.example.grails.rdbms

import com.github.rahulsom.grooves.api.EventsDsl
import grooves.grails.rdbms.*

import java.util.function.Consumer

class BootStrap {

    def init = { servletContext ->
        createJohnLennon()
        createRingoStarr()
        createPaulMcCartney()

    }

    private Patient createJohnLennon() {
        def patient = new Patient(uniqueId: '42').save(flush: true, failOnError: true)

        on(patient) {
            apply new PatientCreated(name: 'John Lennon')
            apply new ProcedurePerformed(code: 'FLUSHOT', cost: 32.40)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()

            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new PaymentMade(amount: 180.00)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()
        }
    }

    private Patient createRingoStarr() {
        def patient = new Patient(uniqueId: '43').save(flush: true, failOnError: true)

        on(patient) {
            apply new PatientCreated(name: 'Ringo Starr')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()

            apply new ProcedurePerformed(code: 'FLUSHOT', cost: 32.40)
            apply new PaymentMade(amount: 180.00)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()
        }
    }

    private Patient createPaulMcCartney() {
        def patient = new Patient(uniqueId: '44').save(flush: true, failOnError: true)

        on(patient) {
            apply new PatientCreated(name: 'Paul McCartney')
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            def gluc = apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)
            apply new PatientEventReverted(revertedEventId: gluc.id)
            def pmt = apply new PaymentMade(amount: 30.00)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()

            apply new PatientEventReverted(revertedEventId: pmt.id)
            apply new PaymentMade(amount: 60.00)


            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()

            apply new PaymentMade(amount: 60.00)

            snapshotWith new PatientAccountQuery()
            snapshotWith new PatientHealthQuery()
        }

    }

    Date currDate = Date.parse('yyyy-MM-dd', '2016-01-01')

    Patient on(Patient patient, @DelegatesTo(EventsDsl.OnSpec) Closure closure) {
        def eventSaver = { it.save(flush: true, failOnError: true) } as Consumer<PatientEvent>
        def positionSupplier = { PatientEvent.countByAggregate(patient) + 1 }
        def userSupplier = {'anonymous'}
        def dateSupplier = {currDate+=1; currDate}
        new EventsDsl<Patient, Long, PatientEvent>().on(patient, eventSaver, positionSupplier, userSupplier, dateSupplier, closure)
    }

    def destroy = {
    }
}
