package com.github.rahulsom.grooves.functions

import com.github.rahulsom.grooves.logging.Trace

/**
 * A predicate that decides whether to apply more events to the snapshot.
 * Some conditions may require that the snapshot is not updated and returned as is.
 */
interface ApplyMoreEventsPredicate<Snapshot> {
    @Trace(false)
    fun invoke(snapshot: Snapshot): Boolean
}