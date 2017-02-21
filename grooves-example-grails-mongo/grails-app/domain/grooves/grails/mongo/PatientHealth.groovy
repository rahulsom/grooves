package grooves.grails.mongo

import com.github.rahulsom.grooves.api.Snapshot

class PatientHealth implements Snapshot<Patient> {

    static mapWith = "mongo"

    Long lastEvent
    Patient deprecatedBy
    Set<Patient> deprecates
    Patient aggregate

    String name

    List<Procedure> procedures = []

    static hasMany = [
        procedures: Procedure,
            deprecates: Patient
    ]

    static constraints = {
    }
}

class Procedure {
    String code
    Date date
}
