package grooves.boot.kotlin

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

/**
 * Created by rahul on 6/10/17.
 */
@Component
class BeansHolder {
    @Autowired
    fun setApplicationContext(ctx: ApplicationContext) {
        context = ctx
    }

    companion object {
        var context: ApplicationContext? = null
    }
}