package grooves.grails.mongo

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class ZipcodeSummaryQuery {

    Optional<ZipcodeSummary> computeSnapshot(Zipcode aggregate, Date moment) {
        def retval = new ZipcodePatientsQuery().computeSnapshot(aggregate, moment).
                map { ZipcodePatients zipcodePatients ->
                    zipcodePatients.joinedIds.
                            inject(createEmptySnapshot()) {  snapshot, patientId ->
                                new PatientHealthQuery().computeSnapshot(Patient.get(patientId), moment).
                                        ifPresent { PatientHealth health -> addHealthToSnapshot(health, snapshot) }
                                snapshot
                            }
                }
        retval as Optional<ZipcodeSummary>
    }

    private List<Procedure> addHealthToSnapshot(PatientHealth health, ZipcodeSummary snapshot) {
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
