package grooves.grails.rxmongo


import com.github.rahulsom.grooves.api.snapshots.Snapshot
import grails.gorm.rx.mongodb.RxMongoEntity
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
class PatientHealth implements RxMongoEntity, Snapshot<Patient, String, String, PatientEvent> {

    String id
    Long lastEventPosition
    Date lastEventTimestamp
    Patient deprecatedBy
    Set<Patient> deprecates

    String aggregateId

    @Override Observable<Patient> getAggregateObservable() { Patient.get(aggregateId) }
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

    @Override Observable<Patient> getDeprecatedByObservable() { Patient.get(deprecatedById) }
    @Override Observable<Patient> getDeprecatesObservable() { from(deprecates.toList()) }
}

@EqualsAndHashCode
@SuppressWarnings(['GrailsDomainReservedSqlKeywordName'])
class Procedure {
    String code
    Date date

    @Override String toString() { "Procedure($code, $date)" }
}
