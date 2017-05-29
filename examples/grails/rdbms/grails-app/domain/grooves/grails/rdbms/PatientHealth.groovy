package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.snapshots.JavaSnapshot
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import rx.Observable

import static rx.Observable.*

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

    @Override Observable<Patient> getAggregateObservable() {
        just aggregate
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

    @Override Observable<Patient> getDeprecatedByObservable() {
        deprecatedBy ? just(deprecatedBy) : empty()
    }

    @Override
    Observable<Patient> getDeprecatesObservable() {
        deprecates ? from(deprecates.toList()) : empty()
    }
}

