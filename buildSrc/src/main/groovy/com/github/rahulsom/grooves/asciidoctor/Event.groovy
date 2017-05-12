package com.github.rahulsom.grooves.asciidoctor

import groovy.transform.TupleConstructor

import static java.lang.System.identityHashCode
import static Constants.*

/**
 * Represents an event while rendering in asciidoctor.
 *
 * @author Rahul Somasunderam
 */
@TupleConstructor
class Event {
    String id
    Date date
    String description
    EventType type

    @Override String toString() {
        "  - $id ${date.format('yyyy-MM-dd')} $description ($type)"
    }

    boolean reverted = false
    int x, y

    void buildSvg(builder, int index, SvgBuilder svgBuilder) {
        if (description == '.') {
            return
        }

        def xOffset = svgBuilder.dates[date]
        builder.mkp.comment(toString())
        builder.g(id: "event${identityHashCode(this)}", class: "event ${this.type.name()}") {
            def x = 10 + aggregateWidth * 2 + xOffset * eventSpace as int
            def y = index * eventLineHeight + offset + aggregateHeight / 2 as int

            while (svgBuilder.allEvents.find { it.x == x && it.y == y }) {
                y -= 20 * Math.sqrt(3) / 2
                x += 10
            }
            this.x = x
            this.y = y

            if (type == EventType.Revert) {
                def revertedId = description.split(' ')[-1]
                def reverted = svgBuilder.allEvents.find { it.id == revertedId }

                def x1 = reverted.x + (x - reverted.x) / 2
                def y1 = y - ((x - reverted.x) / 3)
                builder.path d: "M${x} ${y} Q $x1 $y1, ${reverted.x + 15} ${reverted.y - 15}",
                        stroke: "red",
                        fill: "transparent",
                        'stroke-dasharray': '5, 5',
                        'marker-end': "url(#triangle)",
                        'stroke-width': this.reverted ? 0.2 : 2
            }

            def revertedClass = this.reverted ? 'reverted' : ''

            if (description.contains('created')) {
                builder.circle cx: x, cy: y, r: 13, 'stroke-dasharray': '5, 5',
                        class: "eventCreated ${this.type.name()}"
            }

            builder.circle cx: x, cy: y, r: 10, class: "event ${this.type.name()} ${revertedClass}"

            builder.text this.id, x: x, y: y, class: "eventId",
                    'text-anchor': 'middle',
                    'alignment-baseline': 'central'
        }
    }

}
