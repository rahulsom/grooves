package grooves.grails.mongo

import grails.compiler.GrailsCompileStatic
import rx.Observable

@GrailsCompileStatic
class ZipcodeSummaryQuery {

    Observable<ZipcodeSummary> computeSnapshot(Zipcode aggregate, Date moment) {
        new ZipcodePatientsQuery().computeSnapshot(aggregate, moment).
                flatMap {
                    Observable.from(it.joinedIds).
                            flatMap { new PatientHealthQuery().computeSnapshot(Patient.get(it), moment) }.
                            reduce(createEmptySnapshot()) { ZipcodeSummary snapshot, PatientHealth health ->
                                addHealthToSnapshot(health, snapshot)
                                snapshot
                            }
                }
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

    ZipcodeSummary createEmptySnapshot() {
        new ZipcodeSummary(deprecatesIds: [], procedureCounts: [])
    }

}
