package grooves.grails.mongo

import com.github.rahulsom.grooves.annotations.Event
import com.github.rahulsom.grooves.api.RevertEvent
import com.github.rahulsom.grooves.api.internal.BaseEvent
import groovy.json.JsonBuilder

abstract class ZipcodeEvent implements BaseEvent<Zipcode, ZipcodeEvent> {

    RevertEvent<Zipcode, ZipcodeEvent> revertedBy
    String createdBy
    Date timestamp
    Long position
    Zipcode aggregate

    static transients = ['revertedBy']

    static constraints = {
    }
}

@Event(Zipcode)
class ZipcodeCreated extends ZipcodeEvent {
    String name

    @Override String getAudit() { new JsonBuilder([name: name]).toString() }
}
