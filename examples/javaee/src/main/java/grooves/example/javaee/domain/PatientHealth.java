package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static rx.Observable.*;
import static rx.RxReactiveStreams.toPublisher;

@Data
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

    @Data
    public static class Procedure implements Serializable {
        private final String code;
        private final Date date;
    }
}
