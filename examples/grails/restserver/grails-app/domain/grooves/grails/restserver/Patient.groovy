package grooves.grails.restserver

import grails.rest.Resource
import groovy.transform.EqualsAndHashCode

/**
 * Represents a Patient
 */
@Resource
@EqualsAndHashCode(includes = ['uniqueId'])
class Patient {
    String uniqueId
    static constraints = {
    }

    @Override String toString() { "Patient($id, $uniqueId)" }
}
