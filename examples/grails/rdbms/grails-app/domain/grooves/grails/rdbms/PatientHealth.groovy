package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.snapshots.JavaSnapshot
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
class PatientHealth implements JavaSnapshot<Long, Patient, Long, Long, PatientEvent> {

    Long lastEventPosition
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

    static hasMany = [
            deprecates: Patient,
            procedures: Procedure,
    ]

    static transients = ['aggregate',]

    static constraints = {
        deprecatedBy nullable: true
    }

    @Override Publisher<Patient> getDeprecatedByObservable() {
        toPublisher(deprecatedBy ? just(deprecatedBy) : empty())
    }

    @Override
    Publisher<Patient> getDeprecatesObservable() {
        toPublisher(deprecates ? from(deprecates.toList()) : empty())
    }
}

