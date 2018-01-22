package grooves.example.push

import com.google.common.eventbus.EventBus
import com.google.common.util.concurrent.AbstractService
import com.google.inject.Inject
import grooves.example.pushstyle.Public
import grooves.example.pushstyle.Tables.BALANCE
import org.jooq.DSLContext
import org.slf4j.LoggerFactory.getLogger
import java.util.*
import grooves.example.pushstyle.tables.Balance as BalanceTable

class Application @Inject constructor(
    val eventBus: EventBus, val eventService: EventService, val database: Database,
    val dslContext: DSLContext
) : AbstractService() {

    public override fun doStop() {
        dslContext.dropTable(BALANCE)
        eventBus.unregister(eventService)
        log.error("======== Shutting down ========")
    }

    public override fun doStart() {
        log.error("")
        log.error("")
        log.error("")
        log.error("========= Starting up =========")
        dslContext.createSchemaIfNotExists(Public.PUBLIC).execute()
        dslContext.createTableIfNotExists(BALANCE)
            .column(BALANCE.B_ID)
            .column(BALANCE.B_VERSION)
            .column(BALANCE.B_TIME)
            .column(BALANCE.B_ACCOUNT)
            .column(BALANCE.BALANCE_)
            .execute()
        eventBus.register(eventService)
        log.error("doStart complete")
    }

    private val log = getLogger(this.javaClass)

    fun deposit(accountId: String, position: Long, atmId: String, amount: Long) =
        eventBus.post(
            Transaction.Deposit(
                UUID.randomUUID().toString(), Account(accountId), Date(), position, atmId, amount
            )
        )

    fun withdraw(accountId: String, position: Long, atmId: String, amount: Long) =
        eventBus.post(
            Transaction.Withdraw(
                UUID.randomUUID().toString(), Account(accountId), Date(), position, atmId, amount
            )
        )

    fun getBalance(accountId: String) =
        database.getBalance(Account(accountId), null)?.balance

}