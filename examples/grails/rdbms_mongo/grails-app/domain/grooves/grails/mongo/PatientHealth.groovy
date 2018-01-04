package grooves.grails.mongo

import com.github.rahulsom.grooves.api.snapshots.Snapshot
import groovy.transform.EqualsAndHashCode
import org.reactivestreams.Publisher

import static rx.Observable.*
import static rx.RxReactiveStreams.toPublisher

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
    long lastEventPosition
    Date lastEventTimestamp
    Set<String> processingErrors = []

    Long aggregateId

    Patient getAggregate() { Patient.get(aggregateId) }

    @Override
    Publisher<Patient> getAggregateObservable() {
        toPublisher(aggregateId ? defer { just(aggregate) } : empty())
    }

    void setAggregate(Patient aggregate) { this.aggregateId = aggregate.id }

    @Override
    Publisher<Patient> getDeprecatedByObservable() {
        toPublisher(deprecatedById ? defer { just(deprecatedBy) } : empty())
    }
    Long deprecatedById

    Patient getDeprecatedBy() { Patient.get(deprecatedById) }

    void setDeprecatedBy(Patient aggregate) { deprecatedById = aggregate.id }

    @Override
    Publisher<Patient> getDeprecatesObservable() {
        toPublisher(deprecatesIds ?
                from(deprecatesIds.toList()).flatMap { Patient.get(it) } :
                empty())
    }
    Set<Long> deprecatesIds

    Set<Patient> getDeprecates() { deprecatesIds.collect { Patient.get(it) }.toSet() }

    void setDeprecates(Set<Patient> deprecates) { deprecatesIds = deprecates*.id }

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
