package rxmongo

import com.github.rahulsom.grooves.api.snapshots.JavaSnapshot
import grails.gorm.rx.mongodb.RxMongoEntity
import groovy.transform.EqualsAndHashCode
import rx.Observable

import static rx.Observable.*

/**
 * Represents a patient's health
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['DuplicateStringLiteral',])
@EqualsAndHashCode(includes = ['aggregateId', 'lastEventPosition',])
class PatientHealth implements RxMongoEntity<PatientHealth>,
        JavaSnapshot<String, Patient, String, String, PatientEvent> {

    String id
    Long lastEventPosition
    Date lastEventTimestamp
    Set<String> processingErrors = []
    Set<Patient> deprecates

    String aggregateId

    @Override
    Observable<Patient> getAggregateObservable() {
        aggregateId ? Patient.get(aggregateId) : empty()
    }

    void setAggregate(Patient aggregate) { this.aggregateId = aggregate.id }

    String deprecatedById
    void setDeprecatedBy(Patient deprecator) {
        deprecatedById = deprecator.id
    }
    @Override Observable<Patient> getDeprecatedByObservable() {
        deprecatedById ? Patient.get(deprecatedById) : empty()
    }

    @Override Observable<Patient> getDeprecatesObservable() {
        deprecates ? from(deprecates) : empty()
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
