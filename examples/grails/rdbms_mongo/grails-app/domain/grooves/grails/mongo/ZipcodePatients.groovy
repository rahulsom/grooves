package grooves.grails.mongo

import com.github.rahulsom.grooves.api.snapshots.Join
import groovy.transform.EqualsAndHashCode
import org.reactivestreams.Publisher

import static rx.Observable.*
import static rx.RxReactiveStreams.toPublisher

/**
 * Represents a join between Zipcode and Patient
 *
 * @author Rahul Somasunderam
 */
@EqualsAndHashCode(includes = ['aggregateId', 'lastEventPosition',])
class ZipcodePatients implements Join<Long, Zipcode, String, Long, Long, ZipcodeEvent> {

    static mapWith = 'mongo'

    String id
    long lastEventPosition
    Date lastEventTimestamp
    Set<String> processingErrors = []

    Long aggregateId

    Zipcode getAggregate() { Zipcode.get(aggregateId) }

    @Override
    Publisher<Zipcode> getAggregateObservable() {
        toPublisher(aggregateId ? defer { just aggregate } : empty())
    }

    void setAggregate(Zipcode aggregate) { this.aggregateId = aggregate.id }

    @Override
    Publisher<Zipcode> getDeprecatedByObservable() {
        toPublisher(deprecatedBy ? just(deprecatedBy) : empty())
    }
    Long deprecatedById

    Zipcode getDeprecatedBy() { Zipcode.get(deprecatedById) }

    void setDeprecatedBy(Zipcode aggregate) { deprecatedById = aggregate.id }

    @Override
    Publisher<Zipcode> getDeprecatesObservable() {
        toPublisher(deprecatesIds ? from(deprecatesIds).flatMap { Zipcode.get it } : empty())
    }
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

    @Override
    void setJoinedIds(List<? extends Long> list) {
        this.joinedIds = list
    }
}
