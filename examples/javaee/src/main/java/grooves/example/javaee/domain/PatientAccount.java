package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.snapshots.JavaSnapshot;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import rx.Observable;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static rx.Observable.from;
import static rx.Observable.just;

// tag::documented[]
public class PatientAccount
        implements JavaSnapshot<Long, Patient, Long, Long, PatientEvent>, // <1>
        Serializable {
    @Getter @Setter private Long id;
    @Getter @Setter private Patient aggregate;
    @Getter @Setter private Patient deprecatedBy;
    @Getter @Setter private List<Patient> deprecates = new ArrayList<>();
    @Getter @Setter private Long lastEventPosition; // <2>
    @Getter @Setter private Date lastEventTimestamp; // <3>

    @Getter @Setter private String name;
    @Getter @Setter private BigDecimal balance = new BigDecimal(0);
    @Getter @Setter private BigDecimal moneyMade = new BigDecimal(0);

    // end::documented[]
    @NotNull
    @Override
    @XmlTransient
    // tag::documented[]
    public Observable<Patient> getAggregateObservable() { // <4>
        return just(aggregate);
    }

    // end::documented[]
    @NotNull
    @Override
    @XmlTransient
    // tag::documented[]
    public Observable<Patient> getDeprecatedByObservable() { // <5>
        return just(deprecatedBy);
    }

    // end::documented[]
    @NotNull
    @Override
    @XmlTransient
    // tag::documented[]
    public Observable<Patient> getDeprecatesObservable() { // <6>
        return from(deprecates);
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
