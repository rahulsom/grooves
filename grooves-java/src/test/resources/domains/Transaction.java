package domains;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import org.reactivestreams.Publisher;

import java.util.Date;

import static rx.Observable.empty;
import static rx.Observable.just;
import static rx.RxReactiveStreams.toPublisher;

public abstract class Transaction implements BaseEvent<Long, Account, Long, Transaction> {
    Account aggregate;
    RevertEvent<Long, Account, Long, Transaction> revertedBy;
    Long id, position;
    Date timestamp;

    @Override
    public void setPosition(Long position) {
        this.position = position;
    }

    @Override
    public Long getPosition() {
        return position;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setRevertedBy(RevertEvent<Long, Account, Long, Transaction> revertedBy) {
        this.revertedBy = revertedBy;
    }

    @Override
    public RevertEvent<Long, Account, Long, Transaction> getRevertedBy() {
        return revertedBy;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public void setAggregate(Account aggregate) {
        this.aggregate = aggregate;
    }

    @Override
    public Account getAggregate() {
        return aggregate;
    }

    public Publisher<Account> getAggregateObservable() {
        return toPublisher(aggregate != null ? just(aggregate) : empty());
    }
}