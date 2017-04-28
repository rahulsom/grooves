package grooves.grails.mongo

import com.github.rahulsom.grooves.api.snapshots.Snapshot
import groovy.transform.EqualsAndHashCode
import rx.Observable

import static rx.Observable.empty
import static rx.Observable.just

/**
 * Represents Accounts of a Patient
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['DuplicateNumberLiteral', 'DuplicateStringLiteral',])
@EqualsAndHashCode(includes = ['aggregateId', 'lastEventPosition'])
class PatientAccount implements Snapshot<Patient, String, Long, PatientEvent> {

    static mapWith = 'mongo'

    String id
    Long lastEventPosition
    Date lastEventTimestamp
    Set<String> processingErrors = []

    Long aggregateId
    Patient getAggregate() { Patient.get(aggregateId) }
    void setAggregate(Patient aggregate) { this.aggregateId = aggregate.id }

    @Override
    Observable<Patient> getDeprecatedByObservable() {
        deprecatedBy ? just(deprecatedBy) : empty()
    }
    Long deprecatedById
    Patient getDeprecatedBy() { Patient.get(deprecatedById) }
    void setDeprecatedBy(Patient aggregate) { deprecatedById = aggregate.id }

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

    @Override String toString() { "PatientAccount($id, $aggregateId, $lastEventPosition, " +
            "B: $balance, M: $moneyMade)" }
}
