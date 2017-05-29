package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import rx.Observable;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;

import static rx.Observable.just;

public abstract class PatientEvent implements BaseEvent<Patient, Long, PatientEvent> {
    private Patient aggregate;
    private Long id;
    private String createdBy;
    private RevertEvent<Patient, Long, PatientEvent> revertedBy;
    private Date timestamp;
    private Long position;

    @Override
    @XmlTransient
    public Observable<Patient> getAggregateObservable() {
        return just(aggregate);
    }

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

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }
}
