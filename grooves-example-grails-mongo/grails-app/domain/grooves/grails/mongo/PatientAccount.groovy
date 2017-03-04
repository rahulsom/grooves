package grooves.grails.mongo

import com.github.rahulsom.grooves.api.Snapshot

class PatientAccount implements Snapshot<Patient, String> {

    static mapWith = "mongo"

    String id
    Long lastEvent
    Patient deprecatedBy
    Set<Patient> deprecates
    Set<String> processingErrors = []
    Patient aggregate

    BigDecimal balance = 0.0
    BigDecimal moneyMade = 0.0

    String name

    static hasMany = [
            deprecates: Patient
    ]

    static embedded = ['deprecates', 'processingErrors']

    static constraints = {
        deprecatedBy nullable: true
    }
}
