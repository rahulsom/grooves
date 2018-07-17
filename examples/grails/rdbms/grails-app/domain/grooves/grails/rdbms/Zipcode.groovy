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
    static constraints = {
        uniqueId maxSize: 100
    }

    Long id
    String uniqueId

    @Override String toString() { "Zipcode($id, $uniqueId)" }
}
