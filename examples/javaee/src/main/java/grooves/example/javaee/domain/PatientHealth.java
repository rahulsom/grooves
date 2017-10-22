package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.snapshots.JavaSnapshot;
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

import static io.reactivex.Flowable.fromIterable;
import static io.reactivex.Flowable.just;

public class PatientHealth
        implements JavaSnapshot<Long, Patient, Long, Long, PatientEvent>, Serializable {
    @Getter @Setter private List<Procedure> procedures = new ArrayList<>();
    @Getter @Setter private Long id;
    @Getter @Setter private Patient aggregate;
    @Getter @Setter private Patient deprecatedBy;
    @Getter @Setter private List<Patient> deprecates = new ArrayList<>();
    @Getter @Setter private Long lastEventPosition;
    @Getter @Setter private Date lastEventTimestamp;

    @Getter @Setter private String name;

    @NotNull
    @Override
    @XmlTransient
    public Publisher<Patient> getAggregateObservable() {
        return just(aggregate);
    }

    @NotNull
    @Override
    @XmlTransient
    public Publisher<Patient> getDeprecatedByObservable() {
        return just(deprecatedBy);
    }

    @NotNull
    @Override
    @XmlTransient
    public Publisher<Patient> getDeprecatesObservable() {
        return fromIterable(deprecates);
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
