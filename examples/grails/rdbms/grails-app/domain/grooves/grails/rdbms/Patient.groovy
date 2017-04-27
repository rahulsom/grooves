package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.groovy.transformations.Aggregate
import groovy.transform.EqualsAndHashCode

/**
 * Represents a Patient
 */
@Aggregate
@EqualsAndHashCode(includes = ['uniqueId'])
class Patient implements AggregateType<Long> {
    String uniqueId
    static constraints = {
    }

    @Override String toString() { "Patient($id, $uniqueId)" }
}
