package grooves.grails.mongo

import com.github.rahulsom.grooves.transformations.Aggregate
import com.github.rahulsom.grooves.api.AggregateType

@Aggregate
class Zipcode implements AggregateType<Long> {

    String uniqueId
    static constraints = {
    }

    @Override String toString() { "Zipcode($id, $uniqueId)" }
}
