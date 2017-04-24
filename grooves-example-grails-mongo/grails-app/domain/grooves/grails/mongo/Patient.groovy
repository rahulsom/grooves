package grooves.grails.mongo

import com.github.rahulsom.grooves.transformations.Aggregate
import com.github.rahulsom.grooves.api.AggregateType
import groovy.transform.EqualsAndHashCode

/**
 * Represents a Patient
 *
 * @author Rahul Somasunderam
 */
@Aggregate
@EqualsAndHashCode(includes = ['uniqueId'])
class Patient implements AggregateType<Long> {

    String uniqueId
    static constraints = {
    }

    @Override String toString() { "Patient($id, $uniqueId)" }

}
