package grooves.example.push

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.internal.schedulers.NewThreadWorker
import io.reactivex.internal.schedulers.RxThreadFactory
import java.util.concurrent.TimeUnit

/**
 * This and [ContextManager] are based on
 * [http://akarnokd.blogspot.com/2015/05/schedulers-part-1.html](http://akarnokd.blogspot.com/2015/05/schedulers-part-1.html).
 *
 * This allows to create an execution context for the RxJava2 to simulate [ThreadLocal] in a way
 * that works for RxJava even though RxJava manages threads differently.
 */
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
                if (context != null) {
                    ContextManager.set(context)
                }
                runnable.run()
            }

            return worker.scheduleActual(contextAwareRunnable, delay, unit, tracking)
        }

        override fun isDisposed() = tracking.isDisposed

        override fun dispose() = tracking.dispose()
    }
}