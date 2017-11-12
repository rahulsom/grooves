package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
        implements Snapshot<Long, Patient, Long, Long, PatientEvent>, Serializable {
    @Getter @Setter private List<Procedure> procedures = new ArrayList<>();
    @Getter @Setter private Long id;
    @Getter @Setter private Patient aggregate;
    @Getter @Setter private Patient deprecatedBy;
    @Getter @Setter private List<Patient> deprecates = new ArrayList<>();
    @Getter @Setter private long lastEventPosition;
    @Getter @Setter private Date lastEventTimestamp;

    @Getter @Setter private String name;

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

    @AllArgsConstructor
    public static class Procedure implements Serializable {
        @Getter private String code;
        @Getter private Date date;
    }
}
