package domains;

import com.github.rahulsom.grooves.api.snapshots.Snapshot;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import static rx.Observable.*;
import static rx.RxReactiveStreams.toPublisher;

public class Balance implements Snapshot<Long, Account, String, Long, Transaction> {
    private String id;
    private long lastEventPosition;
    private Date lastEventTimestamp;
    private Account aggregate;
    private Account deprecatedBy;
    private Set<Account> deprecates;

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setLastEventPosition(long lastEventPosition) {
        this.lastEventPosition = lastEventPosition;
    }

    @Override
    public void setLastEventTimestamp(Date lastEventTimestamp) {
        this.lastEventTimestamp = lastEventTimestamp;
    }

    @Override
    public long getLastEventPosition() {
        return lastEventPosition;
    }

    @Override
    public Date getLastEventTimestamp() {
        return lastEventTimestamp;
    }

    @Override
    public void setAggregate(Account aggregate) {
        this.aggregate = aggregate;
    }

    @Override
    public void setDeprecatedBy(Account deprecatedBy) {
        this.deprecatedBy = deprecatedBy;
    }

    @Override
    public Publisher<Account> getAggregateObservable() {
        return toPublisher(aggregate != null ? just(aggregate) : empty());
    }

    @Override
    public Publisher<Account> getDeprecatedByObservable() {
        return toPublisher(deprecatedBy != null ? just(deprecatedBy) : empty());
    }

    @Override
    public Publisher<Account> getDeprecatesObservable() {
        return toPublisher(from(new ArrayList(deprecates)));
    }
}
