package grooves.grails.mongo

import com.github.rahulsom.grooves.api.snapshots.JavaSnapshot
import groovy.transform.EqualsAndHashCode
import rx.Observable

import static rx.Observable.*

/**
 * Represents Accounts of a Patient
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['DuplicateNumberLiteral', 'DuplicateStringLiteral',])
@EqualsAndHashCode(includes = ['aggregateId', 'lastEventPosition'])
// tag::documented[]
class PatientAccount implements JavaSnapshot<Long, Patient, String, Long, PatientEvent> { // <1>

    static mapWith = 'mongo'

    String id
    Long lastEventPosition // <2>
    Date lastEventTimestamp // <3>
    Set<String> processingErrors = []

    Long aggregateId

    Patient getAggregate() { Patient.get(aggregateId) }

    @Override
    Observable<Patient> getAggregateObservable() { // <4>
        aggregateId ? defer { just aggregate } : empty()
    }

    void setAggregate(Patient aggregate) { this.aggregateId = aggregate.id }

    @Override
    Observable<Patient> getDeprecatedByObservable() { // <5>
        deprecatedBy ? just(deprecatedBy) : empty()
    }
    Long deprecatedById

    Patient getDeprecatedBy() { Patient.get(deprecatedById) }

    void setDeprecatedBy(Patient aggregate) { deprecatedById = aggregate.id }

    @Override
    Observable<Patient> getDeprecatesObservable() { // <6>
        deprecatesIds ? from(deprecatesIds).flatMap { Patient.get it } : empty()
    }
    Set<Long> deprecatesIds

    Set<Patient> getDeprecates() { deprecatesIds.collect { Patient.get(it) }.toSet() }

    void setDeprecates(Set<Patient> deprecates) { deprecatesIds = deprecates*.id }

    BigDecimal balance = 0.0
    BigDecimal moneyMade = 0.0

    String name

    static hasMany = [
            deprecatesIds: Long,
    ]

    static embedded = ['deprecates', 'processingErrors',]
    static transients = ['aggregate', 'deprecatedBy', 'deprecates',]

    static constraints = {
        deprecatedById nullable: true
    }

    @Override String toString() {
        "PatientAccount($id, $aggregateId, $lastEventPosition, " +
                "B: $balance, M: $moneyMade)"
    }
}
// end::documented[]
