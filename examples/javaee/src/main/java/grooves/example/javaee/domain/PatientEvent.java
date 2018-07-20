package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;

import static rx.Observable.empty;
import static rx.Observable.just;
import static rx.RxReactiveStreams.toPublisher;

// tag::documented[]

public abstract class PatientEvent implements BaseEvent<Patient, Long, PatientEvent> { // <1>
    private Patient aggregate;
    private Long id;
    private RevertEvent<Patient, Long, PatientEvent> revertedBy;  // <2>
    private Date timestamp; // <3>
    private long position; // <4>

    // end::documented[]


    public Patient getAggregate() {
        return aggregate;
    }

    public void setAggregate(Patient aggregate) {
        this.aggregate = aggregate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RevertEvent<Patient, Long, PatientEvent> getRevertedBy() {
        return revertedBy;
    }

    public void setRevertedBy(RevertEvent<Patient, Long, PatientEvent> revertedBy) {
        this.revertedBy = revertedBy;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    @XmlTransient
    // tag::documented[]
    @Override
    @NotNull
    public Publisher<Patient> getAggregateObservable() { // <5>
        return toPublisher(aggregate != null ? just(aggregate) : empty());
    }
}
// end::documented[]
