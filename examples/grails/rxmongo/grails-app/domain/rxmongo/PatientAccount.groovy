package rxmongo

import com.github.rahulsom.grooves.api.snapshots.Snapshot
import grails.gorm.rx.mongodb.RxMongoEntity
import groovy.transform.EqualsAndHashCode
import rx.Observable

import static rx.Observable.*

/**
 * Represents the accounts of a Patient
 *
 * @author Rahul Somasunderam
 */
@EqualsAndHashCode(includes = ['aggregateId', 'lastEventPosition'])
@SuppressWarnings(['DuplicateNumberLiteral'])
class PatientAccount implements RxMongoEntity<PatientAccount>,
        Snapshot<Patient, String, String, PatientEvent> {

    String id
    Long lastEventPosition
    Date lastEventTimestamp
    String deprecatedById
    Set<Patient> deprecates

    String aggregateId

    @Override
    Observable<Patient> getAggregateObservable() {
        aggregateId ?  Patient.get(aggregateId) : empty()
    }

    void setAggregate(Patient aggregate) { aggregateId = aggregate.id }

    BigDecimal balance = 0.0
    BigDecimal moneyMade = 0.0

    String name

    static hasMany = [
            deprecates: Patient,
    ]

    static transients = ['aggregate',]

    static constraints = {
        deprecatedById nullable: true
    }

    @Override String toString() { "PatientAccount($id, $aggregateId, $lastEventPosition)" }

    @Override Observable<Patient> getDeprecatedByObservable() {
        deprecatedById ? Patient.get(deprecatedById) : empty()
    }
    void setDeprecatedBy(Patient deprecator) {
        deprecatedById = deprecator.id
    }

    @Override Observable<Patient> getDeprecatesObservable() {
        deprecates ? from(deprecates) : empty()
    }
}
