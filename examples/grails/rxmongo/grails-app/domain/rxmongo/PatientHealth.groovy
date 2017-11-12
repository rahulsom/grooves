package rxmongo

import com.github.rahulsom.grooves.api.snapshots.Snapshot
import grails.gorm.rx.mongodb.RxMongoEntity
import groovy.transform.EqualsAndHashCode
import org.reactivestreams.Publisher

import static rx.Observable.empty
import static rx.Observable.from
import static rx.RxReactiveStreams.toPublisher

/**
 * Represents a patient's health
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['DuplicateStringLiteral',])
@EqualsAndHashCode(includes = ['aggregateId', 'lastEventPosition',])
class PatientHealth implements RxMongoEntity<PatientHealth>,
        Snapshot<String, Patient, String, String, PatientEvent> {

    String id
    long lastEventPosition
    Date lastEventTimestamp
    Set<String> processingErrors = []
    Set<Patient> deprecates

    String aggregateId

    @Override
    Publisher<Patient> getAggregateObservable() {
        toPublisher(aggregateId ? Patient.get(aggregateId) : empty())
    }

    void setAggregate(Patient aggregate) { this.aggregateId = aggregate.id }

    String deprecatedById
    void setDeprecatedBy(Patient deprecator) {
        deprecatedById = deprecator.id
    }
    @Override Publisher<Patient> getDeprecatedByObservable() {
        toPublisher(deprecatedById ? Patient.get(deprecatedById) : empty())
    }

    @Override Publisher<Patient> getDeprecatesObservable() {
        toPublisher(deprecates ? from(deprecates) : empty())
    }

    String name

    List<Procedure> procedures = []

    static hasMany = [
            procedures   : Procedure,
            deprecatesIds: String,
    ]

    static constraints = {
        deprecatedById nullable: true
    }

    static embedded = ['procedures', 'processingErrors']

    @Override String toString() { "PatientHealth($id, $aggregateId, $lastEventPosition)" }
}

@EqualsAndHashCode
@SuppressWarnings(['GrailsDomainReservedSqlKeywordName'])
class Procedure {
    String code
    Date date

    @Override String toString() { "Procedure($code, $date)" }
}
