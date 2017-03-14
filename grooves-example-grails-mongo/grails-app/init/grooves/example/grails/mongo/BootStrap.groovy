package grooves.example.grails.mongo

import com.github.rahulsom.genealogy.NameDbUsa
import com.github.rahulsom.grooves.api.EventsDsl
import grooves.grails.mongo.*

import java.util.function.Consumer

class BootStrap {

    def init = { servletContext ->
        def campbell = on(new Zipcode(uniqueId: '95008').save(flush: true, failOnError: true)) {
            apply new ZipcodeCreated(name: 'Campbell, California')
        }
        def santanaRow = on(new Zipcode(uniqueId: '95128').save(flush: true, failOnError: true)) {
            apply new ZipcodeCreated(name: 'Santana Row, San Jose, California')
        }
        johnLennon()
        ringoStarr()
        def names = NameDbUsa.instance
        for (int i = 0; i < 10; i++) {
            def seed = i * 0.05 + 0.12
            def random = new Random((Long.MAX_VALUE * seed) as long)

            currDate = Date.parse('yyyy-MM-dd', '2016-01-01')

            def patient = new Patient(uniqueId: "I${i}").save(flush: true, failOnError: true)
            on(patient) {
                apply new PatientCreated(name: "${names.getFemaleName(seed)} ${names.getLastName(seed)}")
                def numberOfProcedures = random.nextInt(procedures.size())
                for (int p = 0; p < numberOfProcedures; p++) {
                    currDate += random.nextInt(5)
                    def key = procedures.keySet().toList()[p]
                    apply new ProcedurePerformed(code: key, cost: procedures[key])
                }
            }

        }
    }

    def procedures = [
            FLUSHOT       : 32.40,
            ANNUALPHYSICAL: 170.00,
            GLUCOSETEST   : 78.93,
            LIPIDTEST     : 102.55,
            XRAY_WRIST    : 70.42,
            XRAY_BACK     : 104.40,
    ]

    private Patient ringoStarr() {
        def patient2 = new Patient(uniqueId: '43').save(flush: true, failOnError: true)

        on(patient2) {
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
    private Patient johnLennon() {
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

    Date currDate = Date.parse('yyyy-MM-dd', '2016-01-01')

    Patient on(Patient patient, @DelegatesTo(EventsDsl.OnSpec) Closure closure) {
        def eventSaver = { it.save(flush: true, failOnError: true) } as Consumer
        def positionSupplier = { PatientEvent.countByAggregate(patient) + 1 }
        def userSupplier = { 'anonymous' }
        def dateSupplier = { currDate += 1; currDate }
        EventsDsl.on(patient, eventSaver, positionSupplier, userSupplier, dateSupplier, closure)
    }

    Zipcode on(Zipcode zipcode, @DelegatesTo(EventsDsl.OnSpec) Closure closure) {
        def eventSaver = { it.save(flush: true, failOnError: true) } as Consumer
        def positionSupplier = { ZipcodeEvent.countByAggregate(zipcode) + 1 }
        def userSupplier = { 'anonymous' }
        EventsDsl.on(zipcode, eventSaver, positionSupplier, userSupplier, closure)
    }

    def destroy = {
    }
}
