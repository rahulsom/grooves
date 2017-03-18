package grooves.grails.mongo

import com.github.rahulsom.grooves.transformations.Event
import com.github.rahulsom.grooves.api.events.DisjoinEvent
import com.github.rahulsom.grooves.api.events.JoinEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.api.events.BaseEvent
import groovy.json.JsonBuilder

abstract class ZipcodeEvent implements BaseEvent<Zipcode, Long, ZipcodeEvent> {

    RevertEvent<Zipcode, Long, ZipcodeEvent> revertedBy
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

@Event(Zipcode)
class ZipcodeGotPatient extends ZipcodeEvent implements JoinEvent<Zipcode, Long, ZipcodeEvent, Patient> {
    Patient joinAggregate

    @Override String getAudit() { new JsonBuilder([patientId: joinAggregate?.id]).toString() }
}

@Event(Zipcode)
class ZipcodeLostPatient extends ZipcodeEvent implements DisjoinEvent<Zipcode, Long, ZipcodeEvent, Patient> {
    Patient joinAggregate

    @Override String getAudit() { new JsonBuilder([patientId: joinAggregate?.id]).toString() }
}
