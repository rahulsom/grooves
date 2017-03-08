package grooves.grails.mongo

import com.github.rahulsom.grooves.api.Snapshot

class PatientAccount implements Snapshot<Patient, String> {

    static mapWith = "mongo"

    String id
    Long lastEvent
    Set<String> processingErrors = []
    Long aggregateId
    @Override Patient getAggregate() { Patient.get(aggregateId) }
    @Override void setAggregate(Patient aggregate) { this.aggregateId = aggregate.id }
    Long deprecatedById
    @Override Patient getDeprecatedBy() { Patient.get(deprecatedById) }
    @Override void setDeprecatedBy(Patient aggregate) { deprecatedById = aggregate.id }
    Set<Long> deprecatesIds
    Set<Patient> getDeprecates() { deprecatesIds.collect {Patient.get(it)}.toSet() }
    void setDeprecates(Set<Patient> deprecates) { deprecatesIds = deprecates*.id }

    BigDecimal balance = 0.0
    BigDecimal moneyMade = 0.0

    String name

    static hasMany = [
            deprecatesIds: Long
    ]

    static embedded = ['deprecates', 'processingErrors']
    static transients = ['aggregate', 'deprecatedBy', 'deprecates']

    static constraints = {
        deprecatedById nullable: true
    }
}
