package grooves.grails.rdbms

import com.github.rahulsom.grooves.grails.GormAggregate
import com.github.rahulsom.grooves.groovy.transformations.Aggregate
import groovy.transform.EqualsAndHashCode

/**
 * Represents a zipcode
 *
 * @author Rahul Somasunderam
 */
@Aggregate
@EqualsAndHashCode(includes = ['uniqueId'])
class Zipcode implements GormAggregate<Long> {

    String uniqueId
    static constraints = {
    }

    @Override String toString() { "Zipcode($id, $uniqueId)" }
}
