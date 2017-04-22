package grooves.grails.rdbms

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
class Procedure {
    static belongsTo = [
        patientHealth: PatientHealth
    ]

    static mapping = {
        version false
    }
    String code
    Date date

    @Override
    String toString() {
        "Procedure{id=$id, patientHealth=$patientHealth.id, code='$code', date=$date}"
    }
}
