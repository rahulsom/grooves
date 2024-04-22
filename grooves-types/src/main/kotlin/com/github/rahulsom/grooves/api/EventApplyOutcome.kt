package com.github.rahulsom.grooves.api

/**
 * The result of applying an event. This is used by Grooves to decide how to proceed with
 * a computation.
 *
 * @author Rahul Somasunderam
 */
enum class EventApplyOutcome {
    /**
     * No further events need to be applied, and the computation should return the current state
     * of the snapshot.
     */
    RETURN,

    /**
     * Normal processing can continue and the current value of snapshot can be passed to the
     * remainder of the computation.
     */
    CONTINUE,
}