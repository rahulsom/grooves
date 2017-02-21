package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.Snapshot

class PatientAccount implements Snapshot<Patient> {

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

    static constraints = {
    }
}
