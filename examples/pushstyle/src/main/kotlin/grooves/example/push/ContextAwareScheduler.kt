package grooves.example.push

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.internal.schedulers.NewThreadWorker
import io.reactivex.internal.schedulers.RxThreadFactory
import java.util.concurrent.TimeUnit

object ContextAwareScheduler : Scheduler() {

    private val worker = NewThreadWorker(RxThreadFactory("ContextAwareScheduler"))

    override fun createWorker(): Scheduler.Worker = ContextAwareWorker(worker)

    internal class ContextAwareWorker(val worker: NewThreadWorker) : Scheduler.Worker() {

        val tracking = CompositeDisposable()

        override fun schedule(runnable: Runnable, delay: Long, unit: TimeUnit): Disposable {
            if (isDisposed) {
                return Disposables.disposed()
            }

            val context = ContextManager.get()
            val contextAwareRunnable = {
                ContextManager.set(context)
                runnable.run()
            }

            return worker.scheduleActual(contextAwareRunnable, delay, unit, tracking)
        }

        override fun isDisposed() = tracking.isDisposed

        override fun dispose() = tracking.dispose()
    }

}

object ContextManager {

    internal val ctx = ThreadLocal<Any>()

    fun get(): Any {
        return ctx.get()
    }

    fun set(context: Any) {
        ctx.set(context)
    }

}