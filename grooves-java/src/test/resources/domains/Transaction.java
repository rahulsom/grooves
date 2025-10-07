package domains;

import static rx.Observable.empty;
import static rx.Observable.just;
import static rx.RxReactiveStreams.toPublisher;

import com.github.rahulsom.grooves.api.events.BaseEvent;
import com.github.rahulsom.grooves.api.events.RevertEvent;
import java.util.Date;
import org.reactivestreams.Publisher;

public abstract class Transaction implements BaseEvent<Account, Long, Transaction> {
    private Account aggregate;
    private RevertEvent<Account, Long, Transaction> revertedBy;
    private Long id;
    private long position;
    private Date timestamp;

    @Override
    public void setPosition(long position) {
        this.position = position;
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setRevertedBy(RevertEvent<Account, Long, Transaction> revertedBy) {
        this.revertedBy = revertedBy;
    }

    @Override
    public RevertEvent<Account, Long, Transaction> getRevertedBy() {
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
