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

            if (type == EventType.DeprecatedBy || type == EventType.Join || type == EventType.Disjoin) {
                def otherId = description.split(' ')[-1]
                def other = svgBuilder.allEvents.find { it.id == otherId }

                if (other.x && other.y) {

                    def x1 = (type == EventType.Disjoin ? -30 : 30) * Math.abs(other.y - y) / eventLineHeight
                    def xContactOffset = type == EventType.Disjoin ? -10 : 10
                    def y1 = (y + other.y) / 2

                    def yOffset = y > other.y ? 10 : -10
                    if (type == EventType.DeprecatedBy) {
                        yOffset *= 2
                    }

                    def pathParams = [
                            d                 : "M${x} ${y} Q ${x1 + x} $y1, ${other.x + xContactOffset} ${other.y + yOffset}",
                            stroke            : "blue",
                            fill              : "transparent",
                            'stroke-dasharray': '5, 5',
                            'stroke-width'    : this.reverted ? 0.2 : 2
                    ]

                    if (type == EventType.DeprecatedBy) {
                        pathParams.put 'marker-end', "url(#triangle)"
                    }
                    if (type == EventType.Disjoin) {
                        pathParams.put 'stroke', 'gray'
                        pathParams.put 'stroke-dasharray', '2, 5'

                    }
                    if (type == EventType.Join) {
                        pathParams.put 'stroke', '#e6c300'
                    }
                    builder.path pathParams
                }
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
