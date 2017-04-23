package grooves.grails.mongo

import grails.converters.JSON
import grails.rx.web.RxController
import grails.transaction.Transactional

@Transactional(readOnly = true)
class ZipcodeController implements RxController {

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Zipcode.list(params), model: [zipcodeCount: Zipcode.count()]
    }

    def show(Zipcode Zipcode) {
        respond Zipcode
    }

    def summary(Zipcode Zipcode) {
        def snapshot = params['date'] ?
                new ZipcodeSummaryQuery().computeSnapshot(Zipcode, params.date('date')) :
                new ZipcodeSummaryQuery().computeSnapshot(Zipcode, new Date())

        snapshot.map { s ->
            JSON.use('deep') {
                rx.render(s as JSON)
            }
        }
    }

    def patients(Zipcode Zipcode) {
        def snapshot = params['version'] ?
                new ZipcodePatientsQuery().computeSnapshot(Zipcode, params.long('version')) :
                params['date'] ?
                        new ZipcodePatientsQuery().computeSnapshot(Zipcode, params.date('date')) :
                        new ZipcodePatientsQuery().computeSnapshot(Zipcode, new Date())
        snapshot.map { s ->
            JSON.use('deep') {
                rx.render(s as JSON)
            }
        }
    }

}
