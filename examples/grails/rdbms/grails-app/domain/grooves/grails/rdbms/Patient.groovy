package grooves.grails.rdbms

import com.github.rahulsom.grooves.grails.GormAggregate
import com.github.rahulsom.grooves.groovy.transformations.Aggregate
// tag::documented[]
import groovy.transform.EqualsAndHashCode

// end::documented[]
/**
 * Represents a Patient
 */
// tag::documented[]
@Aggregate // <1>
@EqualsAndHashCode(includes = ['uniqueId'])
class Patient implements GormAggregate<Long> { //<2>
    Long id // <3>
    String uniqueId
    static constraints = {
    }

    @Override String toString() { "Patient($id, $uniqueId)" }
}
// end::documented[]
