package grooves.grails.rdbms

import grails.converters.JSON

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class PatientController {

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Patient.list(params), model:[patientCount: Patient.count()]
    }

    def show(Patient patient) {
        respond patient
    }

    def account(Patient patient) {
        def snapshot = params.version ?
                new PatientAccountQuery().computeSnapshot(patient, params.long('version')) :
                params['date'] ?
                        new PatientAccountQuery().computeSnapshot(patient, params.date('date')) :
                        new PatientAccountQuery().computeSnapshot(patient, Long.MAX_VALUE)
        def res = snapshot.toBlocking().first()
        println res.toString().length()
        respond res
    }

    def health(Patient patient) {
        def snapshot = params.version ?
                new PatientHealthQuery().computeSnapshot(patient, params.long('version')) :
                params['date'] ?
                        new PatientHealthQuery().computeSnapshot(patient, params.date('date')) :
                        new PatientHealthQuery().computeSnapshot(patient, Long.MAX_VALUE)
        def res = snapshot.toBlocking().first()
        println res.toString().length()
        JSON.use('deep') {
            render res as JSON
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'patient.label', default: 'Patient'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
