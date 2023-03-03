package grooves.example.push

import com.github.rahulsom.grooves.api.events.BaseEvent
import com.github.rahulsom.grooves.api.events.RevertEvent
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import java.util.Date

sealed class Transaction(
    override val id: String,
    override var aggregate: Account?,
    override var timestamp: Date,
    override var position: Long
) :
    BaseEvent<Account, String, Transaction> {

    override fun getAggregateObservable(): Publisher<Account> =
        Flowable.fromIterable(listOfNotNull(aggregate))

    override var revertedBy: RevertEvent<Account, String, Transaction>?
        get() = revertedBy
        set(value) {
            revertedBy = value
        }

    data class Deposit(
        override val id: String,
        override var aggregate: Account?,
        override var timestamp: Date,
        override var position: Long,
        val atmId: String,
        val amount: Long
    ) :
        Transaction(id, aggregate, timestamp, position)

    data class Withdraw(
        override val id: String,
        override var aggregate: Account?,
        override var timestamp: Date,
        override var position: Long,
        val atmId: String,
        val amount: Long
    ) :
        Transaction(id, aggregate, timestamp, position)
}