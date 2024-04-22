package com.github.rahulsom.grooves.asciidoctor

/**
 * Classifies types of events.
 *
 * @author Rahul Somasunderam
 */
enum class EventType {
    Normal,
    Revert,
    Deprecates,
    DeprecatedBy,
    Join,
    Disjoin,
}