package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.snapshots.Snapshot
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.reactivestreams.Publisher

import static rx.Observable.*
import static rx.RxReactiveStreams.toPublisher

/**
 * Represents a patient's health
 *
 * @author Rahul Somasunderam
 */
@ToString
@SuppressWarnings(['DuplicateStringLiteral'])
@EqualsAndHashCode(includes = ['aggregate', 'lastEventPosition'])
class PatientHealth implements Snapshot<Patient, Long, Long, PatientEvent> {

    static hasMany = [
            deprecates: Patient,
            procedures: Procedure,
    ]

    static transients = ['aggregate',]

    static constraints = {
        deprecatedBy nullable: true
        name maxSize: 100
    }

    Long id
    long lastEventPosition
    Date lastEventTimestamp
    Patient deprecatedBy
    Set<Patient> deprecates

    Long aggregateId

    Patient getAggregate() { Patient.get(aggregateId) }

    @Override Publisher<Patient> getAggregateObservable() {
        toPublisher(aggregate ? just(aggregate) : empty())
    }

    void setAggregate(Patient aggregate) { aggregateId = aggregate.id }
    List<Procedure> procedures

    String name

    @Override Publisher<Patient> getDeprecatedByObservable() {
        toPublisher(deprecatedBy ? just(deprecatedBy) : empty())
    }

    @Override
    Publisher<Patient> getDeprecatesObservable() {
        toPublisher(deprecates ? from(deprecates.toList()) : empty())
    }
}

