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
    /**
     * The procedures performed on this patient.
     */
    private List<Procedure> procedures = new ArrayList<>();
    private Long id;
    /**
     * The patient corresponding to this snapshot.
     */
    private Patient aggregate;
    /**
     * The patient that deprecated the patient of this snapshot.
     */
    private Patient deprecatedBy;
    private List<Patient> deprecates = new ArrayList<>();
    /**
     * The position of the last event in the snapshot.
     */
    private long lastEventPosition;
    /**
     * The timestamp of the last event in the snapshot.
     */
    private Date lastEventTimestamp;

    /**
     * The name of the patient.
     */
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
        /**
         * The code for the procedure.
         */
        private final String code;
        /**
         * The date of the procedure.
         */
        private final Date date;
    }
}
