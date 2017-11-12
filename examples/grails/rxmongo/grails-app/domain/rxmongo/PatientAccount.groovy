package rxmongo

import com.github.rahulsom.grooves.api.snapshots.Snapshot
import grails.gorm.rx.mongodb.RxMongoEntity
import groovy.transform.EqualsAndHashCode
import org.reactivestreams.Publisher

import static rx.Observable.empty
import static rx.Observable.from
import static rx.RxReactiveStreams.toPublisher

/**
 * Represents the accounts of a Patient
 *
 * @author Rahul Somasunderam
 */
@EqualsAndHashCode(includes = ['aggregateId', 'lastEventPosition'])
@SuppressWarnings(['DuplicateNumberLiteral'])
class PatientAccount implements RxMongoEntity<PatientAccount>,
        Snapshot<String, Patient, String, String, PatientEvent> {

    String id
    long lastEventPosition
    Date lastEventTimestamp
    String deprecatedById
    Set<Patient> deprecates

    String aggregateId

    @Override
    Publisher<Patient> getAggregateObservable() {
        toPublisher(aggregateId ?  Patient.get(aggregateId) : empty())
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

    @Override Publisher<Patient> getDeprecatedByObservable() {
        toPublisher(deprecatedById ? Patient.get(deprecatedById) : empty())
    }
    void setDeprecatedBy(Patient deprecator) {
        deprecatedById = deprecator.id
    }

    @Override Publisher<Patient> getDeprecatesObservable() {
        toPublisher(deprecates ? from(deprecates) : empty())
    }
}
