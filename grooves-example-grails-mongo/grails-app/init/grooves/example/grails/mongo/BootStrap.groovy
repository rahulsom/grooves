package grooves.example.grails.mongo

import com.github.rahulsom.grooves.api.EventsDsl
import grooves.grails.mongo.Patient
import grooves.grails.mongo.PatientAccountQuery
import grooves.grails.mongo.PatientCreated
import grooves.grails.mongo.PatientEvent
import grooves.grails.mongo.PatientHealthQuery
import grooves.grails.mongo.PaymentMade
import grooves.grails.mongo.ProcedurePerformed

import java.util.function.Consumer

class BootStrap {

    def init = { servletContext ->
        def patient = new Patient(uniqueId: '42').save(flush: true, failOnError: true)

        on(patient) {
            apply new PatientCreated(name: 'John Smith')
            apply new ProcedurePerformed(code: 'FLUSHOT', cost: 32.40)
            apply new ProcedurePerformed(code: 'GLUCOSETEST', cost: 78.93)
            apply new PaymentMade(amount: 100.25)
        }

        new PatientAccountQuery().computeSnapshot(patient, Long.MAX_VALUE).get().save(flush: true, failOnError: true)
        new PatientHealthQuery().computeSnapshot(patient, Long.MAX_VALUE).get().save(flush: true, failOnError: true)

        on(patient) {
            apply new ProcedurePerformed(code: 'ANNUALPHYSICAL', cost: 170.00)
            apply new PaymentMade(amount: 180.00)
        }
    }

    void on(Patient patient, @DelegatesTo(EventsDsl.OnSpec) Closure closure) {
        def eventSaver = { it.save(flush: true, failOnError: true) } as Consumer<PatientEvent>
        def positionSupplier = { PatientEvent.countByAggregateId(patient.id) + 1 }
        EventsDsl.on(patient, eventSaver, positionSupplier, closure)
    }

    def destroy = {
    }
}
