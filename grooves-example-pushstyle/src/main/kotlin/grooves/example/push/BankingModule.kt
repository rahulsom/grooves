package grooves.example.push

import com.google.common.eventbus.EventBus
import com.google.inject.AbstractModule
import com.google.inject.Provides
import org.jooq.SQLDialect.H2
import org.jooq.impl.DSL
import java.sql.DriverManager.getConnection

object BankingModule : AbstractModule() {

    override fun configure() {
        bind(Application::class.java)
        bind(EventBus::class.java)
        bind(EventService::class.java)
        bind(Database::class.java)
    }

    @Provides
    fun dslContext() =
        DSL.using(getConnection("jdbc:h2:mem:app", "sa", ""), H2)
}