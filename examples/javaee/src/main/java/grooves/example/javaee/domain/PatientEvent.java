package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import org.jetbrains.annotations.NotNull;
import rx.Observable;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;

// tag::documented[]
import static rx.Observable.empty;
import static rx.Observable.just;

public abstract class PatientEvent implements BaseEvent<Long, Patient, Long, PatientEvent> { // <1>
    private Patient aggregate;
    private Long id;
    private String createdBy;
    private RevertEvent<Long, Patient, Long, PatientEvent> revertedBy;  // <2>
    private Date timestamp; // <3>
    private Long position; // <4>

    // end::documented[]
    @XmlTransient
    // tag::documented[]
    @Override
    @NotNull
    public Observable<Patient> getAggregateObservable() { // <5>
        return aggregate != null ? just(aggregate) : empty();
    }
    // end::documented[]

    public int getObjectId() {
        return System.identityHashCode(this);
    }

    public String getType() {
        return this.getClass().getSimpleName();
    }

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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public RevertEvent<Long, Patient, Long, PatientEvent> getRevertedBy() {
        return revertedBy;
    }

    public void setRevertedBy(RevertEvent<Long, Patient, Long, PatientEvent> revertedBy) {
        this.revertedBy = revertedBy;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }
    // tag::documented[]
}
// end::documented[]
