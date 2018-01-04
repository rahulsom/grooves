package com.github.rahulsom.grooves.grails

import com.github.rahulsom.grooves.api.AggregateType

interface GormAggregate<T> extends AggregateType {
    T getId()
}