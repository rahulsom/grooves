package grooves.grails.mongo

import com.github.rahulsom.grooves.api.Snapshot

class PatientAccount implements Snapshot<Patient> {

    static mapWith = "mongo"

    Long lastEvent
    Patient deprecatedBy
    Set<Patient> deprecates
    Patient aggregate

    BigDecimal balance = 0.0
    BigDecimal moneyMade = 0.0

    String name

    static hasMany = [
            deprecates: Patient
    ]

    static embedded = ['deprecates']

    static constraints = {
        deprecatedBy nullable: true
    }
}
