package grooves.grails.rdbms

import grails.converters.JSON
import grails.rx.web.RxController
import grails.transaction.Transactional

import static org.springframework.http.HttpStatus.NOT_FOUND
import static rx.RxReactiveStreams.toObservable

/**
 * Allows access to Patient information over HTTP
 *
 * @author Rahul Somasunderam
 */
@Transactional(readOnly = true)
@SuppressWarnings(['DuplicateStringLiteral'])
class PatientController implements RxController {

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Patient.list(params), model: [patientCount: Patient.count()]
    }

    def show(Patient patient) {
        respond patient
    }

    def account(Patient patient) {
        def query = new PatientAccountQuery()
        def snapshot = params.version ?
                query.computeSnapshot(patient, params.long('version')) :
                params['date'] ?
                        query.computeSnapshot(patient, params.date('date')) :
                        query.computeSnapshot(patient, Long.MAX_VALUE)

        toObservable(snapshot).
                map { patientAccount ->
                    patientAccount.toString().length()
                    rx.respond patientAccount
                }
    }

    def health(Patient patient) {
        def query = new PatientHealthQuery()
        def snapshot = params.version ?
                query.computeSnapshot(patient, params.long('version')) :
                params['date'] ?
                        query.computeSnapshot(patient, params.date('date')) :
                        query.computeSnapshot(patient, Long.MAX_VALUE)

        toObservable(snapshot).map { s ->
            s.toString().length()
            JSON.use('deep') {
                rx.render(s as JSON)
            }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message',
                        args: [message(code: 'patient.label', default: 'Patient'), params.id])
                redirect action: 'index', method: 'GET'
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
