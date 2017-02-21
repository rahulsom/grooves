package grooves.example.grails.rdbms

import grooves.grails.rdbms.Patient
import grooves.grails.rdbms.PatientCreated
import grooves.grails.rdbms.PaymentMade
import grooves.grails.rdbms.ProcedurePerformed

class BootStrap {

    def init = { servletContext ->
        def patient = new Patient(uniqueId: '42').save()
        new PatientCreated(aggregate: patient, position: 1, date: new Date(), createdBy: 'rahul', name: 'John Smith').save()
        new ProcedurePerformed(aggregate: patient, position: 2, date: new Date(), createdBy: 'rahul', code: 'FLUSHOT', cost: 32.40).save()
        new ProcedurePerformed(aggregate: patient, position: 3, date: new Date(), createdBy: 'rahul', code: 'GLUCOSETEST', cost: 78.93).save()
        new PaymentMade(aggregate: patient, position: 4, date: new Date(), createdBy: 'rahul', amount: 100.25).save(flush: true)
    }
    def destroy = {
    }
}
