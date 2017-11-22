package grooves.example.push

import org.slf4j.LoggerFactory

/**
 * Wraps [ThreadLocal] such that it works with [ContextAwareScheduler].
 * This along with [ContextAwareScheduler] is based on
 * [http://akarnokd.blogspot.com/2015/05/schedulers-part-1.html](http://akarnokd.blogspot.com/2015/05/schedulers-part-1.html).
 *
 */
object ContextManager {

    private val log = LoggerFactory.getLogger(this.javaClass)
    private val ctx = ThreadLocal<Map<String, Any>>()

    fun get() = ctx.get()
    fun set(context: Map<String, Any>) = ctx.set(context)

}