package grooves.grails.mongo

import com.github.rahulsom.grooves.api.snapshots.Snapshot

class ZipcodeSummary implements Snapshot<Zipcode, String,Long, ZipcodeEvent> {

    static mapWith = "mongo"

    String id
    Long lastEventPosition
    Date lastEventTimestamp
    Set<String> processingErrors = []

    Long aggregateId
    Zipcode getAggregate() { Zipcode.get(aggregateId) }
    void setAggregate(Zipcode aggregate) { this.aggregateId = aggregate.id }

    Long deprecatedById
    Zipcode getDeprecatedBy() { Zipcode.get(deprecatedById) }
    void setDeprecatedBy(Zipcode aggregate) { deprecatedById = aggregate.id }

    Set<Long> deprecatesIds
    Set<Zipcode> getDeprecates() { deprecatesIds.collect { Zipcode.get(it) }.toSet() }
    void setDeprecates(Set<Zipcode> deprecates) { deprecatesIds = deprecates*.id }

    String name

    static hasMany = [
            procedureCounts   : ProcedureCount,
            deprecatesIds: Long
    ]

    static constraints = {
        deprecatedById nullable: true
    }

    static embedded = ['procedures', 'processingErrors']
    static transients = ['aggregate', 'deprecatedBy', 'deprecates']

}

class ProcedureCount {
    String code
    int count = 0
}
