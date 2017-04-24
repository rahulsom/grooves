package grooves.grails.mongo

import com.github.rahulsom.grooves.api.snapshots.Join
import groovy.transform.EqualsAndHashCode

/**
 * Joins Doctor with Patients
 *
 * @author Rahul Somasunderam
 */
@EqualsAndHashCode(includes = ['aggregateId', 'lastEventPosition', ])
class DoctorPatients implements Join<Doctor, String, Long, Long, DoctorEvent> {

    static mapWith = 'mongo'

    String id
    Long lastEventPosition
    Date lastEventTimestamp
    Set<String> processingErrors = []

    Long aggregateId
    Doctor getAggregate() { Doctor.get(aggregateId) }
    void setAggregate(Doctor aggregate) { this.aggregateId = aggregate.id }

    Long deprecatedById
    Doctor getDeprecatedBy() { Doctor.get(deprecatedById) }
    void setDeprecatedBy(Doctor aggregate) { deprecatedById = aggregate.id }

    Set<Long> deprecatesIds
    Set<Doctor> getDeprecates() { deprecatesIds.collect { Doctor.get(it) }.toSet() }
    void setDeprecates(Set<Doctor> deprecates) { deprecatesIds = deprecates*.id }

    List<Long> joinedIds

    static hasMany = [
            deprecatesIds: Long,
    ]

    static constraints = {
        deprecatedById nullable: true
    }

    static embedded = ['procedures', 'processingErrors', ]
    static transients = ['aggregate', 'deprecatedBy', 'deprecates', ]

    @Override String toString() { "DoctorPatients($id, $aggregateId, $lastEventPosition)" }
}
