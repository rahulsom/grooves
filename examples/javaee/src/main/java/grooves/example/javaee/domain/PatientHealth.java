package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import rx.Observable;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static rx.Observable.from;
import static rx.Observable.just;

public class PatientHealth implements Snapshot<Patient, Long, Long, PatientEvent>, Serializable {
    List<Procedure> procedures = new ArrayList<>();
    private Long id;
    private Patient aggregate;
    private Patient deprecatedBy;
    private List<Patient> deprecates = new ArrayList<>();
    private Long lastEventPosition;
    private Date lastEventTimestamp;

    private String name;

    @Override
    @XmlTransient
    public Observable<Patient> getAggregateObservable() {
        return just(aggregate);
    }

    @Override
    @XmlTransient
    public Observable<Patient> getDeprecatedByObservable() {
        return just(deprecatedBy);
    }

    @Override
    @XmlTransient
    public Observable<Patient> getDeprecatesObservable() {
        return from(deprecates);
    }

    @Override
    public String toString() {
        return String.format(
                "PatientHealth{id=%d, aggregate=%s, lastEventPosition=%d, lastEventTimestamp=%s}",
                id, aggregate, lastEventPosition, lastEventTimestamp);
    }

    public int getObjectId() {
        return System.identityHashCode(this);
    }

    public static class Procedure implements Serializable {
        private String code;
        private Date date;

        public Procedure(String code, Date date) {
            this.code = code;
            this.date = date;
        }

        public String getCode() {
            return code;
        }

        public Date getDate() {
            return date;
        }
    }

    public List<Procedure> getProcedures() {
        return procedures;
    }

    public void setProcedures(List<Procedure> procedures) {
        this.procedures = procedures;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Patient getAggregate() {
        return aggregate;
    }

    @Override
    public void setAggregate(Patient aggregate) {
        this.aggregate = aggregate;
    }

    public Patient getDeprecatedBy() {
        return deprecatedBy;
    }

    @Override
    public void setDeprecatedBy(Patient deprecatedBy) {
        this.deprecatedBy = deprecatedBy;
    }

    public List<Patient> getDeprecates() {
        return deprecates;
    }

    public void setDeprecates(List<Patient> deprecates) {
        this.deprecates = deprecates;
    }

    @Override
    public Long getLastEventPosition() {
        return lastEventPosition;
    }

    @Override
    public void setLastEventPosition(Long lastEventPosition) {
        this.lastEventPosition = lastEventPosition;
    }

    @Override
    public Date getLastEventTimestamp() {
        return lastEventTimestamp;
    }

    @Override
    public void setLastEventTimestamp(Date lastEventTimestamp) {
        this.lastEventTimestamp = lastEventTimestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
