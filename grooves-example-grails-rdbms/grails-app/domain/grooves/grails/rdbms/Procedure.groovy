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
    public String toString() {
        return "Procedure{id=$id, patientHealth=$patientHealth.id, code='$code', date=$date}";
    }
}
