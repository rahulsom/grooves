package grooves.grails.mongo

import grails.rx.web.RxController
import grails.transaction.Transactional

import static rx.RxReactiveStreams.toObservable

/**
 * HTTP API for Zipcode
 *
 * @author Rahul Somasunderam
 */
@Transactional(readOnly = true)
@SuppressWarnings(['DuplicateStringLiteral', 'DuplicateMapLiteral'])
class ZipcodeController implements RxController {

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Zipcode.list(params), model: [zipcodeCount: Zipcode.count()]
    }

    def show(Zipcode zipcode) {
        respond zipcode
    }

    def summary(Zipcode zipcode) {
        def snapshot = params['date'] ?
                new ZipcodeSummaryQuery().computeSnapshot(zipcode, params.date('date')) :
                new ZipcodeSummaryQuery().computeSnapshot(zipcode, new Date())

        snapshot.map { s ->
            rx.respond(s)
        }
    }

    def patients(Zipcode zipcode) {
        def snapshot = params['version'] ?
                new ZipcodePatientsQuery().computeSnapshot(zipcode, params.long('version')) :
                params['date'] ?
                        new ZipcodePatientsQuery().computeSnapshot(zipcode, params.date('date')) :
                        new ZipcodePatientsQuery().computeSnapshot(zipcode, new Date())
        toObservable(snapshot).map { s ->
            rx.respond(s)
        }
    }

}
