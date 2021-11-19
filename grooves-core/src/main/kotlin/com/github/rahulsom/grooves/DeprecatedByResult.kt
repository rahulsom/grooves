package com.github.rahulsom.grooves

data class DeprecatedByResult<Aggregate, EventId>(val aggregate: Aggregate, val eventId: EventId)