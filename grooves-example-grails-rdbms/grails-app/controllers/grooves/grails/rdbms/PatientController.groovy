package grooves.grails.rdbms

import grails.converters.JSON
import grails.rx.web.RxController
import grails.transaction.Transactional

import static org.springframework.http.HttpStatus.NOT_FOUND

@Transactional(readOnly = true)
class PatientController implements RxController {

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Patient.list(params), model: [patientCount: Patient.count()]
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

        snapshot.
                map { patientAccount ->
                    println patientAccount.toString().length()
                    rx.respond patientAccount
                }
    }

    def health(Patient patient) {
        def snapshot = params.version ?
                new PatientHealthQuery().computeSnapshot(patient, params.long('version')) :
                params['date'] ?
                        new PatientHealthQuery().computeSnapshot(patient, params.date('date')) :
                        new PatientHealthQuery().computeSnapshot(patient, Long.MAX_VALUE)

        snapshot.map { s ->
            println s.toString().length()
            JSON.use('deep') {
                rx.render(s as JSON)
            }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'patient.label', default: 'Patient'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
