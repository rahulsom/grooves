package com.github.rahulsom.grooves.asciidoctor

import groovy.transform.TupleConstructor

import static java.lang.System.identityHashCode
import static Constants.*

/**
 * Represents an aggregate when rendering as an image
 *
 * @author Rahul Somasunderam
 */
@TupleConstructor
class Aggregate {
    String type
    String id
    String description
    List<Event> events = []

    int index

    @Override String toString() { "|$type,$id,$description\n${events.join('\n')}" }

    void buildSvg(builder, Map<Date, Double> dates) {
        builder.mkp.comment "   aggregate"
        builder.g(id: "aggregate${identityHashCode(this)}") {
            def y = index * eventLineHeight + offset

            builder.rect x: 10, y: y, width: aggregateWidth, height: aggregateHeight,
                    class: 'aggregate'
            builder.text type.toString(), x: 15, y: y + textLineHeight, class: 'aggregateText'
            builder.text id, x: 15, y: y + textLineHeight * 2, class: 'aggregateText'
            builder.text description, x: 10, y: y - 5, class: 'aggregateHeader'

            def yMid = index * eventLineHeight + offset + aggregateHeight / 2

            builder.line x1: 10 + aggregateWidth, y1: yMid,
                    x2: dates.values().max() * eventSpace + 3 * aggregateWidth, y2: yMid,
                    class: 'eventLine', 'marker-end': "url(#triangle)"

        }
    }
}
