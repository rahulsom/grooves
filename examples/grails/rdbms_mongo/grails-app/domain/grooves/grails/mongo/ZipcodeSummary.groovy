package grooves.grails.mongo

import com.github.rahulsom.grooves.api.snapshots.Snapshot
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import rx.Observable

import static rx.Observable.empty
import static rx.Observable.just

/**
 * Represents the summary of all patients' health in a zipcode
 *
 * @author Rahul Somasunderam
 */
@SuppressWarnings(['DuplicateStringLiteral'])
@EqualsAndHashCode(includes = ['aggregateId', 'lastEventPosition',])
@ToString(includes = ['id', 'aggregateId', 'lastEventPosition', 'name',])
class ZipcodeSummary implements Snapshot<Zipcode, String, Long, ZipcodeEvent> {

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

    String name

    static hasMany = [
            procedureCounts: ProcedureCount,
            deprecatesIds  : Long,
    ]

    static constraints = {
        deprecatedById nullable: true
    }

    static embedded = ['procedures', 'processingErrors',]
    static transients = ['aggregate', 'deprecatedBy', 'deprecates',]

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
    String code
    int count = 0
}
