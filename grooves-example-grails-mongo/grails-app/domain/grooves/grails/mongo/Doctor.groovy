package grooves.grails.mongo

import com.github.rahulsom.grooves.api.AggregateType
import com.github.rahulsom.grooves.transformations.Aggregate

@Aggregate
class Doctor implements AggregateType<Long> {

    String uniqueId
    static constraints = {
    }

    @Override String toString() { "Doctor($id, $uniqueId)" }

}

