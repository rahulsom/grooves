package grooves.grails.rdbms

import grails.compiler.GrailsCompileStatic
import rx.Observable

import static rx.Observable.from
import static rx.RxReactiveStreams.toObservable

/**
 * Summarizes the patients' health within a zipcode
 *
 * @author Rahul Somasunderam
 */
@GrailsCompileStatic
class ZipcodeSummaryQuery {

    Observable<ZipcodeSummary> computeSnapshot(Zipcode aggregate, Date moment) {
        toObservable(new ZipcodePatientsQuery().computeSnapshot(aggregate, moment)).
                flatMap {
                    from(it.joinedIds).
                            flatMap {
                                def healthQuery = new PatientHealthQuery()
                                healthQuery.computeSnapshot(Patient.get(it), moment)
                            }.
                            reduce(createEmptySnapshot()) {
                                ZipcodeSummary snapshot, PatientHealth health ->
                                    addHealthToSnapshot(health, snapshot)
                                    snapshot
                            }
                }
    }

    @SuppressWarnings(['FactoryMethodName'])
    ZipcodeSummary createEmptySnapshot() {
        new ZipcodeSummary(deprecatesIds: [], procedureCounts: [])
    }

    private static void addHealthToSnapshot(PatientHealth health, ZipcodeSummary snapshot) {
        health.procedures.each { patientProcedure ->
            def procedure = snapshot.procedureCounts.find {
                it.code == patientProcedure.code
            } as ProcedureCount
            if (!procedure) {
                procedure = new ProcedureCount(code: patientProcedure.code)
                snapshot.procedureCounts << procedure
            }
            procedure.count++
        }
    }

}
