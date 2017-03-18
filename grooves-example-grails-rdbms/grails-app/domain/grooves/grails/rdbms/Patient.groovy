package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.*
import com.github.rahulsom.grooves.transformations.*

@Aggregate
class Patient implements AggregateType<Long> {
    String uniqueId
    static constraints = {
    }
}
