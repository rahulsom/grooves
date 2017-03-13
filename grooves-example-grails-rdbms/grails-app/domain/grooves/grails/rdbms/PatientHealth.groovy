package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.Snapshot
import groovy.transform.ToString

@ToString
class PatientHealth implements Snapshot<Patient, Long> {

    Long lastEventPosition
    Date lastEventTimestamp
    Patient deprecatedBy
    Set<Patient> deprecates

    Long aggregateId
    Patient getAggregate() { Patient.get(aggregateId) }
    void setAggregate(Patient aggregate) { aggregateId = aggregate.id }
    List<Procedure> procedures

    String name

    static hasMany = [
            deprecates: Patient,
            procedures: Procedure
    ]

    static transients = ['aggregate']

    static constraints = {
        deprecatedBy nullable: true
    }
}

