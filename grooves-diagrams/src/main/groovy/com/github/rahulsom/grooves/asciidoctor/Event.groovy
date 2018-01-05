package com.github.rahulsom.grooves.asciidoctor

import groovy.transform.TupleConstructor

import static java.lang.System.identityHashCode

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
        def revertedClass = this.reverted ? 'reverted' : ''
        builder.g(id: "event${identityHashCode(this)}", class: "event ${this.type.name()} ${revertedClass}") {
            def x = 10 + Constants.aggregateWidth * 2 + xOffset * Constants.eventSpace as int
            def y = index * Constants.eventLineHeight + Constants.offset + Constants.aggregateHeight / 2 as int

            while (svgBuilder.allEvents.find { it.x == x && it.y == y }) {
                y -= 20 * Math.sqrt(3) / 2
                x += 10
            }
            this.x = x
            this.y = (int) y

            if (type == EventType.Revert) {
                def revertedId = description.split(' ')[-1]
                def reverted = svgBuilder.allEvents.find { it.id == revertedId }

                def x1 = reverted.x + (x - reverted.x) / 2
                def y1 = y - ((x - reverted.x) / 3)
                builder.path d: "M${x} ${y} Q $x1 $y1, ${reverted.x + 15} ${reverted.y - 15}"
            }

            if (type == EventType.DeprecatedBy || type == EventType.Join || type == EventType.Disjoin) {
                def otherId = description.split(' ')[-1]
                def other = svgBuilder.allEvents.find { it.id == otherId }

                if (other.x && other.y) {

                    def x1 = (type == EventType.Disjoin ? -30 : 30) * Math.abs(other.y - y) / Constants.eventLineHeight
                    def xContactOffset = type == EventType.Disjoin ? -10 : 10
                    def y1 = (y + other.y) / 2

                    def yOffset = y > other.y ? 10 : -10
                    if (type == EventType.DeprecatedBy) {
                        yOffset *= 2
                    }

                    builder.path d: "M${x} ${y} Q ${x1 + x} $y1, ${other.x + xContactOffset} ${other.y + yOffset}"
                }
            }

            if (description.contains('created')) {
                builder.circle cx: x, cy: y, r: 14, 'stroke-dasharray': '2, 5',
                        class: "eventCreated ${this.type.name()}"
            }

            builder.circle cx: x, cy: y, r: 10, class: "event ${this.type.name()} ${revertedClass}"
            builder.text this.id, x: x, y: y, class: "eventId"
        }
    }

}
