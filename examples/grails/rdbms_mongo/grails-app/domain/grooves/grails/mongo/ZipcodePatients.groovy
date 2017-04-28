package grooves.grails.mongo

import com.github.rahulsom.grooves.api.snapshots.Join
import groovy.transform.EqualsAndHashCode
import rx.Observable

import static rx.Observable.empty
import static rx.Observable.just

/**
 * Represents a join between Zipcode and Patient
 *
 * @author Rahul Somasunderam
 */
@EqualsAndHashCode(includes = ['aggregateId', 'lastEventPosition',])
class ZipcodePatients implements Join<Zipcode, String, Long, Long, ZipcodeEvent> {

    static mapWith = 'mongo'

    String id
    Long lastEventPosition
    Date lastEventTimestamp
    Set<String> processingErrors = []

    Long aggregateId
    Zipcode getAggregate() { Zipcode.get(aggregateId) }
    void setAggregate(Zipcode aggregate) { this.aggregateId = aggregate.id }

    @Override
    Observable<Zipcode> getDeprecatedByObservable() {
        deprecatedBy ? just(deprecatedBy) : empty()
    }
    Long deprecatedById
    Zipcode getDeprecatedBy() { Zipcode.get(deprecatedById) }
    void setDeprecatedBy(Zipcode aggregate) { deprecatedById = aggregate.id }

    Set<Long> deprecatesIds
    Set<Zipcode> getDeprecates() { deprecatesIds.collect { Zipcode.get(it) }.toSet() }
    void setDeprecates(Set<Zipcode> deprecates) { deprecatesIds = deprecates*.id }

    List<Long> joinedIds

    static hasMany = [
            deprecatesIds: Long,
    ]

    static constraints = {
        deprecatedById nullable: true
    }

    static embedded = ['procedures', 'processingErrors',]
    static transients = ['aggregate', 'deprecatedBy', 'deprecates',]

    @Override
    String toString() {
        "ZipcodePatients{id=$id, lastEvent=($lastEventPosition, $lastEventTimestamp), " +
                "aggregateId=$aggregateId}"
    }
}
