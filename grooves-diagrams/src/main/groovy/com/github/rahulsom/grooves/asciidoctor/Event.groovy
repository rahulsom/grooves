package com.github.rahulsom.grooves.asciidoctor

import com.github.rahulsom.svg.Circle
import com.github.rahulsom.svg.G
import com.github.rahulsom.svg.Path
import com.github.rahulsom.svg.Text
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

import static com.github.rahulsom.grooves.asciidoctor.Constants.*
import static java.lang.System.identityHashCode

/**
 * Represents an event while rendering in asciidoctor.
 *
 * @author Rahul Somasunderam
 */
@TupleConstructor
@CompileStatic
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

    G buildSvg(int index, SvgBuilder svgBuilder) {
        if (description == '.') {
            return new G()
        }

        def xOffset = svgBuilder.dates[date]
        // builder.mkp.comment(toString())
        def revertedClass = this.reverted ? 'reverted' : ''
        return new G(id: "event${identityHashCode(this)}",
                clazz: "event ${this.type.name()} ${revertedClass}").content {
            def x = 10 + aggregateWidth * 2 + xOffset * eventSpace as int
            def y = index * eventLineHeight + offset + aggregateHeight / 2 as int

            while (svgBuilder.allEvents.find { it.x == x && it.y == y }) {
                y -= 20 * Math.sqrt(3) / 2
                x += 10
            }
            this.x = x
            this.y = (int) y

            if (type == EventType.Revert) {
                def revertedId = description.split(' ')[-1]
                def reverted = svgBuilder.allEvents.find { it.id == revertedId }

                def x1 = reverted.x  + (this.x - reverted.x) / 2
                def y1 = y  - ((this.x - reverted.x) / 3)
                it << new Path(d: "M${x} ${y} Q $x1 $y1, ${reverted.x + 15} ${reverted.y - 15}")
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

                    it << new Path(d: "M${this.x} ${y} Q ${x1 + this.x} $y1, ${other.x + xContactOffset} ${other.y + yOffset}")
                }
            }

            if (description.contains('created')) {
                it << new Circle(cx: "${this.x}", cy: "${y}", r: "14", strokeDasharray: '2, 5',
                        clazz: "eventCreated ${this.type.name()}")
            }

            it << new Circle(cx: "${this.x}", cy: "${this.y}",
                    r: '10', clazz: "event ${this.type.name()} ${revertedClass}")
            it << new Text(x: "${this.x}", y: "${this.y}", clazz: "eventId").withContent(this.id)
        }
    }

}
