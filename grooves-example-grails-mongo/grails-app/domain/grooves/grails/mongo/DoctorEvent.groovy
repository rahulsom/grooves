package grooves.grails.mongo

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.DisjoinEvent
import com.github.rahulsom.grooves.api.events.JoinEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import com.github.rahulsom.grooves.transformations.Event
import groovy.json.JsonBuilder

abstract class DoctorEvent implements BaseEvent<Doctor, Long, DoctorEvent> {

    RevertEvent<Doctor, Long, DoctorEvent> revertedBy
    String createdBy
    Date timestamp
    Long position
    Doctor aggregate

    static transients = ['revertedBy']

    static constraints = {
    }
}

@Event(Doctor)
class DoctorCreated extends DoctorEvent {
    String name

    @Override String getAudit() { new JsonBuilder([name: name]).toString() }
}

@Event(Doctor)
class DoctorGotPatient extends DoctorEvent implements JoinEvent<Doctor, Long, DoctorEvent, Patient> {
    Patient patient
    @Override Patient getJoinAggregate() { patient}
    @Override void setJoinAggregate(Patient rollupAggregate) { patient= rollupAggregate}

    @Override String getAudit() { new JsonBuilder([patientId: joinAggregate?.id]).toString() }
}

@Event(Doctor)
class DoctorLostPatient extends DoctorEvent implements DisjoinEvent<Doctor, Long, DoctorEvent, Patient> {
    Patient patient
    @Override Patient getJoinAggregate() { patient}
    @Override void setJoinAggregate(Patient rollupAggregate) { patient= rollupAggregate}

    @Override String getAudit() { new JsonBuilder([patientId: joinAggregate?.id]).toString() }
}

class DoctorAddedToZipcode extends DoctorEvent implements JoinEvent<Doctor, Long, DoctorEvent, Zipcode>{
    Zipcode zipcode
    @Override Zipcode getJoinAggregate() { zipcode }
    @Override void setJoinAggregate(Zipcode rollupAggregate) { zipcode = rollupAggregate}

    @Override String getAudit() { new JsonBuilder([zipcodeId: joinAggregate?.id]).toString() }

    static transients = ['joinAggregate']
}

class DoctorRemovedFromZipcode extends DoctorEvent implements DisjoinEvent<Doctor, Long, DoctorEvent, Zipcode> {
    Zipcode zipcode
    @Override Zipcode getJoinAggregate() { zipcode }
    @Override void setJoinAggregate(Zipcode rollupAggregate) { zipcode = rollupAggregate}

    @Override String getAudit() { new JsonBuilder([zipcodeId: joinAggregate?.id]).toString() }

    static transients = ['joinAggregate']
}

