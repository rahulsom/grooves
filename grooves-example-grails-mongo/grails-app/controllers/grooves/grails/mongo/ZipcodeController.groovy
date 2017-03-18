package grooves.grails.mongo

import grails.converters.JSON
import grails.transaction.Transactional

@Transactional(readOnly = true)
class ZipcodeController {

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
        JSON.use('deep') {
            render snapshot.get() as JSON
        }
    }

    def patients(Zipcode Zipcode) {
        def snapshot = params['version'] ?
                new ZipcodePatientsQuery().computeSnapshot(Zipcode, params.long('version')) :
                params['date'] ?
                        new ZipcodePatientsQuery().computeSnapshot(Zipcode, params.date('date')) :
                        new ZipcodePatientsQuery().computeSnapshot(Zipcode, new Date())
        JSON.use('deep') {
            render snapshot.get() as JSON
        }
    }

}
