package grooves.grails.mongo

// tag::documented[]
import com.github.rahulsom.grooves.groovy.transformations.Aggregate
import com.github.rahulsom.grooves.api.AggregateType
import groovy.transform.EqualsAndHashCode

// end::documented[]
/**
 * Represents a Patient
 *
 * @author Rahul Somasunderam
 */
// tag::documented[]
@Aggregate // <1>
@EqualsAndHashCode(includes = ['uniqueId'])
class Patient implements AggregateType<Long> { //<2>

    // Long id // <3>
    String uniqueId
    static constraints = {
    }

    @Override String toString() { "Patient($id, $uniqueId)" }

}
// end::documented[]
