package grooves.example.rxrest

import com.github.rahulsom.grooves.api.snapshots.Snapshot
import groovy.transform.EqualsAndHashCode
import rx.Observable

import static rx.Observable.empty
import static rx.Observable.from

/**
 * Represents a patient's health
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['DuplicateStringLiteral',])
@EqualsAndHashCode(includes = ['aggregateId', 'lastEventPosition',])
class PatientHealth implements Snapshot<Patient, String, Long, PatientEvent> {

    static mapWith = 'mongo'

    String id
    Long lastEventPosition
    Date lastEventTimestamp
    Set<String> processingErrors = []

    Long aggregateId

    @Override
    Observable<Patient> getAggregateObservable() {
        aggregateId ? Patient.get(aggregateId) : empty()
    }

    void setAggregate(Patient aggregate) { this.aggregateId = aggregate.id }

    @Override Observable<Patient> getDeprecatedByObservable() {
        deprecatedById ? Patient.get(deprecatedById) : empty()
    }

    Long deprecatedById

    void setDeprecatedBy(Patient aggregate) { deprecatedById = aggregate.id }

    @Override
    Observable<Patient> getDeprecatesObservable() {
        deprecatesIds ? from(deprecatesIds).flatMap { Patient.get(it) } : empty()
    }
    Set<Long> deprecatesIds

    String name

    List<Procedure> procedures = []

    static hasMany = [
            procedures   : Procedure,
            deprecatesIds: Long,
    ]

    static constraints = {
        deprecatedById nullable: true
    }

    static embedded = ['procedures', 'processingErrors']
    static transients = ['aggregate', 'deprecatedBy', 'deprecates']

    @Override String toString() { "PatientHealth($id, $aggregateId, $lastEventPosition)" }
}

@EqualsAndHashCode
@SuppressWarnings(['GrailsDomainReservedSqlKeywordName'])
class Procedure {
    String code
    Date date

    @Override String toString() { "Procedure($code, $date)" }
}
