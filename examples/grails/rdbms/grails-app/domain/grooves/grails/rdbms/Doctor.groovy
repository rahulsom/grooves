package grooves.grails.rdbms

import com.github.rahulsom.grooves.grails.GormAggregate
import com.github.rahulsom.grooves.groovy.transformations.Aggregate
import groovy.transform.EqualsAndHashCode

/**
 * Represents a Doctor who a patient might be associated with
 *
 * @author Rahul Somasunderam
 */
@Aggregate
@EqualsAndHashCode(includes = ['uniqueId'])
class Doctor implements GormAggregate<Long> {

    Long id
    String uniqueId
    static constraints = {
    }

    @Override
    String toString() { "Doctor($id, $uniqueId)" }
}
