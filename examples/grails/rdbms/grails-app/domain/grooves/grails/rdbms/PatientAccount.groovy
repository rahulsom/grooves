package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.snapshots.Snapshot
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
// tag::documented[]
class PatientAccount implements Snapshot<Patient, Long, Long, PatientEvent> { // <1>

    Long id
    long lastEventPosition // <2>
    Date lastEventTimestamp // <3>
    Patient deprecatedBy
    Set<Patient> deprecates

    Long aggregateId

    @Override
    Publisher<Patient> getAggregateObservable() { // <4>
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

    @Override Publisher<Patient> getDeprecatedByObservable() { // <5>
        toPublisher(deprecatedBy ? just(deprecatedBy) : empty())
    }

    @Override Publisher<Patient> getDeprecatesObservable() { // <6>
        toPublisher(deprecates ? from(deprecates) : empty())
    }
}
// end::documented[]
