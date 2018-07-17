package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.snapshots.Snapshot
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.reactivestreams.Publisher

import static rx.Observable.*
import static rx.RxReactiveStreams.toPublisher

/**
 * Represents the summary of all patients' health in a zipcode
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['DuplicateStringLiteral'])
@EqualsAndHashCode(includes = ['aggregateId', 'lastEventPosition',])
@ToString(includes = ['id', 'aggregateId', 'lastEventPosition', 'name',])
class ZipcodeSummary implements Snapshot<Zipcode, Long, Long, ZipcodeEvent> {

    static hasMany = [
            procedureCounts: ProcedureCount,
            deprecatesIds  : Long,
    ]

    static constraints = {
        deprecatedById nullable: true
        name maxSize: 100
    }

    static embedded = ['procedures', 'processingErrors',]
    static transients = ['aggregate', 'deprecatedBy', 'deprecates',]

    Long id
    long lastEventPosition
    Date lastEventTimestamp
    Set<String> processingErrors = []

    @Override
    Publisher<Zipcode> getAggregateObservable() {
        toPublisher(aggregateId ? defer { just aggregate } : empty())
    }
    Long aggregateId
    Zipcode getAggregate() { Zipcode.get(aggregateId) }
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

    String name

}

/**
 * Represents the number of times a procedure has been performed in a zipcode
 *
 * @author Rahul Somasunderam
 */
@EqualsAndHashCode(includes = ['code', 'id'])
@ToString(includes = ['id', 'code', 'count'])
@SuppressWarnings(['GrailsDomainReservedSqlKeywordName', 'DuplicateStringLiteral',])
class ProcedureCount {

    static constraints = {
        code maxSize: 100
    }

    String code
    int count = 0
}
