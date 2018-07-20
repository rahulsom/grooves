package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static rx.Observable.*;
import static rx.RxReactiveStreams.toPublisher;

public class PatientHealth
        implements Snapshot<Patient, Long, Long, PatientEvent>, Serializable {
    private List<Procedure> procedures = new ArrayList<>();
    private Long id;
    private Patient aggregate;
    private Patient deprecatedBy;
    private List<Patient> deprecates = new ArrayList<>();
    private long lastEventPosition;
    private Date lastEventTimestamp;

    private String name;

    public List<Procedure> getProcedures() {
        return procedures;
    }

    public void setProcedures(List<Procedure> procedures) {
        this.procedures = procedures;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Patient getAggregate() {
        return aggregate;
    }

    public void setAggregate(Patient aggregate) {
        this.aggregate = aggregate;
    }

    public Patient getDeprecatedBy() {
        return deprecatedBy;
    }

    public void setDeprecatedBy(Patient deprecatedBy) {
        this.deprecatedBy = deprecatedBy;
    }

    public List<Patient> getDeprecates() {
        return deprecates;
    }

    public void setDeprecates(List<Patient> deprecates) {
        this.deprecates = deprecates;
    }

    public long getLastEventPosition() {
        return lastEventPosition;
    }

    public void setLastEventPosition(long lastEventPosition) {
        this.lastEventPosition = lastEventPosition;
    }

    public Date getLastEventTimestamp() {
        return lastEventTimestamp;
    }

    public void setLastEventTimestamp(Date lastEventTimestamp) {
        this.lastEventTimestamp = lastEventTimestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    @Override
    @XmlTransient
    public Publisher<Patient> getAggregateObservable() {
        return toPublisher(aggregate != null ? just(aggregate) : empty());
    }

    @NotNull
    @Override
    @XmlTransient
    public Publisher<Patient> getDeprecatedByObservable() {
        return toPublisher(deprecatedBy != null ? just(deprecatedBy) : empty());
    }

    @NotNull
    @Override
    @XmlTransient
    public Publisher<Patient> getDeprecatesObservable() {
        return toPublisher(from(deprecates));
    }

    @Override
    public String toString() {
        return String.format(
                "PatientHealth{id=%d, aggregate=%s, lastEventPosition=%d, lastEventTimestamp=%s}",
                id, aggregate, lastEventPosition, lastEventTimestamp);
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
}
