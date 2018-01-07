package com.github.rahulsom.grooves.asciidoctor

import groovy.transform.CompileStatic

/**
 * Classifies types of events.
 *
 * @author Rahul Somasunderam
 */
@CompileStatic
enum EventType {
    Normal, Revert, Deprecates, DeprecatedBy, Join, Disjoin
}
