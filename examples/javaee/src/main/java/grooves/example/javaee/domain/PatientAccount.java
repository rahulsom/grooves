package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.snapshots.JavaSnapshot;
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
    private Long id;
    private Patient aggregate;
    private Patient deprecatedBy;
    private List<Patient> deprecates = new ArrayList<>();
    private Long lastEventPosition; // <2>
    private Date lastEventTimestamp; // <3>

    private String name;
    private BigDecimal balance = new BigDecimal(0);
    private BigDecimal moneyMade = new BigDecimal(0);

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
    public void setAggregate(@NotNull Patient aggregate) {
        this.aggregate = aggregate;
    }

    public Patient getDeprecatedBy() {
        return deprecatedBy;
    }

    @Override
    public void setDeprecatedBy(@NotNull Patient deprecatedBy) {
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getMoneyMade() {
        return moneyMade;
    }

    public void setMoneyMade(BigDecimal moneyMade) {
        this.moneyMade = moneyMade;
    }
    // tag::documented[]
}
// end::documented[]
