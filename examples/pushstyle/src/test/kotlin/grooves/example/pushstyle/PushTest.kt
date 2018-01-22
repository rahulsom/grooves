package grooves.example.pushstyle

import com.google.inject.Guice
import grooves.example.push.Application
import grooves.example.push.BankingModule
import org.awaitility.Awaitility.await
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit.SECONDS

class PushTest {

    val injector = Guice.createInjector(BankingModule)
    val application = injector.getInstance(Application::class.java)

    @Before
    fun before() {
        application.doStart()
    }

    @After
    fun after() {
        application.doStop()
    }

    @Test
    fun testMissingAggregate() {
        val accountId = "A0000"

        Assert.assertEquals(null, application.getBalance(accountId))
    }

    @Test
    fun testTwoTransactionsOnOneAccount() {
        val accountId = "A0001"

        application.deposit(accountId, 1, "MAINST", 100)
        await()
            .atLeast(1, SECONDS)
            .atMost(10, SECONDS)
            .pollDelay(1, SECONDS)
            .until { application.getBalance(accountId) == 100L }

        application.withdraw(accountId, 2, "MAINST", 50)
        await()
            .atLeast(1, SECONDS)
            .atMost(10, SECONDS)
            .pollDelay(1, SECONDS)
            .until { application.getBalance(accountId) == 50L }
    }

    @Test
    fun testMultipleAccounts() {
        val firstAccount = "A0002"
        val secondAccount = "A0003"

        application.deposit(firstAccount, 1, "MAINST", 100)
        application.withdraw(firstAccount, 2, "MAINST", 50)

        await()
            .atLeast(1, SECONDS)
            .atMost(10, SECONDS)
            .pollDelay(1, SECONDS)
            .until { application.getBalance(firstAccount) == 50L }

        Assert.assertEquals(null, application.getBalance(secondAccount))
    }
}