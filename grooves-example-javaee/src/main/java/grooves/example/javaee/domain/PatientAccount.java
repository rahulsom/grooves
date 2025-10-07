package grooves.example.javaee.domain;

import static rx.Observable.*;
import static rx.RxReactiveStreams.toPublisher;

import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

// tag::documented[]
@Data
public class PatientAccount
        implements Snapshot<Patient, Long, Long, PatientEvent>, // <1>
                Serializable {
    private Long id;
    private Patient aggregate;
    private Patient deprecatedBy;
    private List<Patient> deprecates = new ArrayList<>();
    private long lastEventPosition; // <2>
    private Date lastEventTimestamp; // <3>

    private String name;
    private BigDecimal balance = new BigDecimal(0);
    private BigDecimal moneyMade = new BigDecimal(0);

    // end::documented[]

    @NotNull
    @Override
    @XmlTransient
    // tag::documented[]
    public Publisher<Patient> getAggregateObservable() { // <4>
        return toPublisher(aggregate != null ? just(aggregate) : empty());
    }

    // end::documented[]
    @NotNull
    @Override
    @XmlTransient
    // tag::documented[]
    public Publisher<Patient> getDeprecatedByObservable() { // <5>
        return toPublisher(deprecatedBy != null ? just(deprecatedBy) : empty());
    }

    // end::documented[]
    @NotNull
    @Override
    @XmlTransient
    // tag::documented[]
    public Publisher<Patient> getDeprecatesObservable() { // <6>
        return toPublisher(from(deprecates));
    }
    // end::documented[]

    @Override
    public String toString() {
        return String.format(
                "PatientAccount{id=%d, aggregate=%s, lastEventPosition=%d, lastEventTimestamp=%s}",
                id, aggregate, lastEventPosition, lastEventTimestamp);
    }

    // tag::documented[]
}
// end::documented[]
