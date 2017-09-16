package domains;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rx.Observable;

import java.util.Date;

import static rx.Observable.just;

public abstract class Transaction implements BaseEvent<Long, Account, Long, Transaction> {
    Account aggregate;
    RevertEvent<Long, Account, Long, Transaction> revertedBy;
    Long id, position;
    Date timestamp;
    String createdBy, audit;

    @NotNull
    @Override
    public String getAudit() {
        return audit;
    }

    @Override
    public void setPosition(Long position) {
        this.position = position;
    }

    @Nullable
    @Override
    public Long getPosition() {
        return position;
    }

    @Nullable
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setRevertedBy(RevertEvent<Long, Account, Long, Transaction> revertedBy) {
        this.revertedBy = revertedBy;
    }

    @Nullable
    @Override
    public RevertEvent<Long, Account, Long, Transaction> getRevertedBy() {
        return revertedBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Nullable
    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Nullable
    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public void setAggregate(Account aggregate) {
        this.aggregate = aggregate;
    }

    @Nullable
    @Override
    public Account getAggregate() {
        return aggregate;
    }

    public Observable<Account> getAggregateObservable() {
        return just(aggregate);
    }
}