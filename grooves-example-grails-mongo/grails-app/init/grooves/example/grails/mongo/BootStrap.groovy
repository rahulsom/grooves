package grooves.example.grails.mongo

import grooves.grails.mongo.Patient
import grooves.grails.mongo.PatientAccountQuery
import grooves.grails.mongo.PatientCreated
import grooves.grails.mongo.PatientHealthQuery
import grooves.grails.mongo.PaymentMade
import grooves.grails.mongo.ProcedurePerformed

class BootStrap {

    def init = { servletContext ->
        def patient = new Patient(uniqueId: '42').save()
        new PatientCreated(aggregate: patient, position: 1, date: new Date(), createdBy: 'rahul', name: 'John Smith').save()
        new ProcedurePerformed(aggregate: patient, position: 2, date: new Date(), createdBy: 'rahul', code: 'FLUSHOT', cost: 32.40).save()
        new ProcedurePerformed(aggregate: patient, position: 3, date: new Date(), createdBy: 'rahul', code: 'GLUCOSETEST', cost: 78.93).save()
        new PaymentMade(aggregate: patient, position: 4, date: new Date(), createdBy: 'rahul', amount: 100.25).save(flush: true)

        def snapshot = new PatientAccountQuery().computeSnapshot(patient, Long.MAX_VALUE).get()
        snapshot.save(flush: true, failOnError: true)

        def snapshot1 = new PatientHealthQuery().computeSnapshot(patient, Long.MAX_VALUE).get()
        snapshot1.save(flush: true, failOnError: true)

        new ProcedurePerformed(aggregate: patient, position: 5, date: new Date(), createdBy: 'rahul', code: 'ANNUALPHYSICAL', cost: 170.00).save()
        new PaymentMade(aggregate: patient, position: 4, date: new Date(), createdBy: 'rahul', amount: 180.00).save(flush: true)
    }
    def destroy = {
    }
}
