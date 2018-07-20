package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static rx.Observable.*;
import static rx.RxReactiveStreams.toPublisher;

// tag::documented[]
public class PatientAccount implements Snapshot<Patient, Long, Long, PatientEvent>, // <1>
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
