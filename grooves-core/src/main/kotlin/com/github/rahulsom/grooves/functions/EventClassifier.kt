package com.github.rahulsom.grooves.functions

import com.github.rahulsom.grooves.EventType
import com.github.rahulsom.grooves.logging.Trace

/**
 * Classifies the type of event.
 */
interface EventClassifier<Event> {
    @Trace
    fun invoke(event: Event): EventType
}