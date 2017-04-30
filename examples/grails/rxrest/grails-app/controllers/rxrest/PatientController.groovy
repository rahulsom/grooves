package rxrest

import grails.rx.web.RxController
import grooves.example.rxrest.ObjectRequest
import grooves.example.rxrest.Patient
import grooves.example.rxrest.PatientAccountQuery

import static org.springframework.http.HttpStatus.NOT_FOUND

@SuppressWarnings(['DuplicateStringLiteral'])
class PatientController implements RxController {

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Patient.list(params), model: [patientCount: Patient.count()]
    }

    def show(Long id) {
        Patient.get(id).map {
            rx.respond it
        }
    }

    def account(ObjectRequest objectRequest) {
        def query = new PatientAccountQuery()
        Patient.get(objectRequest.id).
                flatMap { patient ->
                    println patient
                    objectRequest.version ?
                            query.computeSnapshot(patient, objectRequest.version) :
                            objectRequest.date ?
                                    query.computeSnapshot(patient, objectRequest.date) :
                                    query.computeSnapshot(patient, Long.MAX_VALUE)
                }.
                map { patientAccount ->
                    patientAccount.toString().length()
                    rx.respond patientAccount
                }
    }

//    def health(Patient patient) {
//        def query = new PatientHealthQuery()
//        def snapshot = params.version ?
//                query.computeSnapshot(patient, params.long('version')) :
//                params['date'] ?
//                        query.computeSnapshot(patient, params.date('date')) :
//                        query.computeSnapshot(patient, Long.MAX_VALUE)
//
//        snapshot.map { s ->
//            s.toString().length()
//            JSON.use('deep') {
//                rx.render(s as JSON)
//            }
//        }
//    }

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
