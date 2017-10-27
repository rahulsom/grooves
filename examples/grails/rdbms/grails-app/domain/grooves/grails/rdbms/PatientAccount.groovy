package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.snapshots.JavaSnapshot
import groovy.transform.EqualsAndHashCode
import org.reactivestreams.Publisher

import static rx.Observable.*
import static rx.RxReactiveStreams.toPublisher

/**
 * Represents the accounts of a Patient
 *
 * @author Rahul Somasunderam
 */
@EqualsAndHashCode(includes = ['aggregateId', 'lastEventPosition'])
@SuppressWarnings(['DuplicateNumberLiteral'])
class PatientAccount implements JavaSnapshot<Long, Patient, Long, Long, PatientEvent> {

    Long lastEventPosition
    Date lastEventTimestamp
    Patient deprecatedBy
    Set<Patient> deprecates

    Long aggregateId

    @Override
    Publisher<Patient> getAggregateObservable() {
        toPublisher(aggregateId ? defer { just(Patient.get(aggregateId)) } : empty())
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
        deprecatedBy nullable: true
    }

    @Override String toString() { "PatientAccount($id, $aggregateId, $lastEventPosition)" }

    @Override Publisher<Patient> getDeprecatedByObservable() {
        toPublisher(deprecatedBy ? just(deprecatedBy) : empty())
    }

    @Override Publisher<Patient> getDeprecatesObservable() {
        toPublisher(deprecates ? from(deprecates) : empty())
    }
}
