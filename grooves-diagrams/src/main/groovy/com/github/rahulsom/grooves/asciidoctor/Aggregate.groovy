package com.github.rahulsom.grooves.asciidoctor

import com.github.rahulsom.svg.G
import com.github.rahulsom.svg.Line
import com.github.rahulsom.svg.Rect
import com.github.rahulsom.svg.Text
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

import static com.github.rahulsom.grooves.asciidoctor.Constants.*
import static java.lang.System.identityHashCode

/**
 * Represents an aggregate when rendering as an image
 *
 * @author Rahul Somasunderam
 */
@TupleConstructor
@CompileStatic
class Aggregate {
    int counter
    String type
    String id
    String description
    List<Event> events = []

    int index

    @Override String toString() { "|$type,$id,$description\n${events.join('\n')}" }

    G buildSvg(Map<Date, Double> dates) {

        return new G(id: "aggregate_${counter}", clazz: 'aggregate').content {
            def y = index * eventLineHeight + offset

            it << new Rect(x: '10', y: y.toString(),
                    width: "${aggregateWidth}", height: "${aggregateHeight}")

            it << new Text(x: '15', y: "${y + textLineHeight}", clazz: 'type').withContent(type)
            it << new Text(x: '15', y: "${y + textLineHeight * 2}", clazz: 'id').withContent(id)
            it << new Text(x: '10', y: "${y - 5}", clazz: 'description').withContent(description)

            def yMid = index * eventLineHeight + offset + aggregateHeight / 2

            it << new Line(x1: "${10 + aggregateWidth}", y1: "${yMid}",
                    x2: "${dates.values().max() * eventSpace + 3 * aggregateWidth}", y2: "${yMid}",
                    clazz: 'eventLine', markerEnd: "url(#triangle)")
        }
    }
}
