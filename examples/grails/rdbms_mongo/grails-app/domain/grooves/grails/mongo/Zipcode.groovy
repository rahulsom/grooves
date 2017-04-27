package grooves.grails.mongo

import com.github.rahulsom.grooves.groovy.transformations.Aggregate
import com.github.rahulsom.grooves.api.AggregateType
import groovy.transform.EqualsAndHashCode

/**
 * Represents a zipcode
 *
 * @author Rahul Somasunderam
 */
@Aggregate
@EqualsAndHashCode(includes = ['uniqueId'])
class Zipcode implements AggregateType<Long> {

    String uniqueId
    static constraints = {
    }

    @Override String toString() { "Zipcode($id, $uniqueId)" }
}
