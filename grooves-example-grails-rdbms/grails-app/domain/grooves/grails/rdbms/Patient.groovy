package grooves.grails.rdbms

import com.github.rahulsom.grooves.api.*
import com.github.rahulsom.grooves.annotations.*

@Aggregate
class Patient implements AggregateType<Long> {
    String uniqueId
    static constraints = {
    }
}
