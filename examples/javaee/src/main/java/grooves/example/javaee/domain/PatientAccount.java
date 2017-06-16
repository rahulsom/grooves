package grooves.example.javaee.domain;

import com.github.rahulsom.grooves.api.snapshots.JavaSnapshot;
import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import rx.Observable;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static rx.Observable.from;
import static rx.Observable.just;

public class PatientAccount implements
        JavaSnapshot<Long, Patient, Long, Long, PatientEvent>, Serializable {
    private Long id;
    private Patient aggregate;
    private Patient deprecatedBy;
    private List<Patient> deprecates = new ArrayList<>();
    private Long lastEventPosition;
    private Date lastEventTimestamp;

    private String name;
    private BigDecimal balance = new BigDecimal(0);
    private BigDecimal moneyMade = new BigDecimal(0);

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
}
