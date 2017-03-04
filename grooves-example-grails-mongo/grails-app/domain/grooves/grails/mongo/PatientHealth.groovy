package grooves.grails.mongo

import com.github.rahulsom.grooves.api.Snapshot

class PatientHealth implements Snapshot<Patient, String> {

    static mapWith = "mongo"

    String id
    Long lastEvent
    Patient deprecatedBy
    Set<Patient> deprecates
    Set<String> processingErrors = []
    Patient aggregate

    String name

    List<Procedure> procedures = []

    static hasMany = [
            procedures: Procedure,
            deprecates: Patient
    ]

    static constraints = {
        deprecatedBy nullable: true
    }

    static embedded = ['procedures', 'processingErrors']
}

class Procedure {
    String code
    Date date
}
