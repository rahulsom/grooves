package com.github.rahulsom.grooves.asciidoctor

import com.github.rahulsom.grooves.asciidoctor.Constants.aggregateHeight
import com.github.rahulsom.grooves.asciidoctor.Constants.aggregateWidth
import com.github.rahulsom.grooves.asciidoctor.Constants.eventLineHeight
import com.github.rahulsom.grooves.asciidoctor.Constants.eventSpace
import com.github.rahulsom.grooves.asciidoctor.Constants.offset
import com.github.rahulsom.svg.Circle
import com.github.rahulsom.svg.G
import com.github.rahulsom.svg.ObjectFactory
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Represents an event while rendering in asciidoctor.
 *
 * @author Rahul Somasunderam
 */
class Event(private val counter: Int, var id: String, var date: Date, var description: String, var type: EventType) {

    override fun toString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return "  - $id ${sdf.format(date)} $description ($type)"
    }

    var reverted = false
    var x: Int = 0
    var y: Int = 0

    fun buildSvg(index: Int, svgBuilder: SvgBuilder): G {
        if (description == ".") {
            return G()
        }

        val xOffset = svgBuilder.dates[date]!!
        // builder.mkp.comment(toString())
        val revertedClass = if (this.reverted) "reverted" else ""
        val g = G().withId("event_$counter")
            .withClazz("event ${this.type.name} $revertedClass")

        var x = (10 + aggregateWidth * 2 + xOffset * eventSpace).toInt()
        var y = index * eventLineHeight + offset + aggregateHeight / 2

        while (svgBuilder.allEvents.find { it.x == x && it.y == y } != null) {
            y -= (20 * sqrt(3.0) / 2).toInt()
            x += 10
        }
        this.x = x
        this.y = y

        if (type == EventType.Revert) {
            val revertedId = description.split(' ').last()
            val reverted = svgBuilder.allEvents.find { it.id == revertedId }!!

            val x1 = reverted.x + (this.x - reverted.x) / 2
            val y1 = y - ((this.x - reverted.x) / 3)
            g.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
                ObjectFactory().createPath(
                    ObjectFactory().createPath().withD("M$x $y Q $x1 $y1, ${reverted.x + 15} ${reverted.y - 15}")
                )
            )
        }

        if (type == EventType.DeprecatedBy || type == EventType.Join || type == EventType.Disjoin) {
            val otherId = description.split(' ').last()
            val other = svgBuilder.allEvents.find { it.id == otherId }!!

            if (other.x > 0 && other.y > 0) {
                val x1 = (if (type == EventType.Disjoin) -30 else 30) * abs(other.y - y) / eventLineHeight
                val xContactOffset = if (type == EventType.Disjoin) -10 else 10
                val y1 = (y + other.y) / 2

                var yOffset = if (y > other.y) 10 else -10
                if (type == EventType.DeprecatedBy) {
                    yOffset *= 2
                }

                g.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
                    ObjectFactory().createPath(
                        ObjectFactory().createPath().withD(
                            "M${this.x} $y Q ${x1 + this.x} $y1, ${other.x + xContactOffset} ${other.y + yOffset}"
                        )
                    )
                )
            }
        }

        if (description.contains("created")) {
            g.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
                ObjectFactory().createCircle(
                    Circle().withCx("${this.x}").withCy("$y").withR("14")
                        .withStrokeDasharray("2, 5")
                        .withClazz("eventCreated ${this.type.name}")
                )
            )
        }

        g.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
            ObjectFactory().createCircle(
                ObjectFactory().createCircle().withCx("${this.x}").withCy("${this.y}")
                    .withR("10")
                    .withClazz("event ${this.type.name} $revertedClass")
            )
        )
        g.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
            ObjectFactory().createText(
                ObjectFactory().createText().withX("${this.x}").withY("${this.y}")
                    .withClazz("eventId").withContent(this.id)
            )
        )
        return g
    }
}