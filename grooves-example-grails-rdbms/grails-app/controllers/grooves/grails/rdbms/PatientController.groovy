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
        def retval = new PatientAccountQuery().computeSnapshot(patient, params.int('version') ?: Long.MAX_VALUE).get()
        println retval
        respond retval
    }

    def health(Patient patient) {
        def retval = new PatientHealthQuery().computeSnapshot(patient, params.int('version') ?: Long.MAX_VALUE).get()
        println retval
        JSON.use('deep') {
            render retval as JSON
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
