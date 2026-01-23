package com.github.rahulsom.grooves.asciidoctor

import com.github.rahulsom.grooves.asciidoctor.Constants.AGGREGATE_HEIGHT
import com.github.rahulsom.grooves.asciidoctor.Constants.AGGREGATE_WIDTH
import com.github.rahulsom.grooves.asciidoctor.Constants.EVENT_LINE_HEIGHT
import com.github.rahulsom.grooves.asciidoctor.Constants.EVENT_SPACE
import com.github.rahulsom.grooves.asciidoctor.Constants.OFFSET
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
class Event(
    private val counter: Int,
    var id: String,
    var date: Date,
    var description: String,
    var type: EventType,
) {
    override fun toString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return "  - $id ${sdf.format(date)} $description ($type)"
    }

    var reverted = false
    var x: Int = 0
    var y: Int = 0

    fun buildSvg(
        index: Int,
        svgBuilder: SvgBuilder,
    ): G {
        if (description == ".") {
            return G()
        }

        val revertedClass = if (this.reverted) "reverted" else ""
        val g =
            G()
                .withId("event_$counter")
                .withClazz("event ${this.type.name} $revertedClass")

        calculatePosition(index, svgBuilder)
        addRevertLine(g, svgBuilder)
        if (type == EventType.DeprecatedBy || type == EventType.Join || type == EventType.Disjoin) {
            val otherId = description.split(' ').last()
            val other = svgBuilder.allEvents.find { it.id == otherId }!!

            if (other.x > 0 && other.y > 0) {
                addLinkLine(other, g)
            }
        }
        addCreatedCircle(g)
        addEventCircleAndText(g, revertedClass)
        return g
    }

    private fun calculatePosition(
        index: Int,
        svgBuilder: SvgBuilder,
    ) {
        val xOffset = svgBuilder.dates[date]!!
        var x = (10 + AGGREGATE_WIDTH * 2 + xOffset * EVENT_SPACE).toInt()
        var y = index * EVENT_LINE_HEIGHT + OFFSET + AGGREGATE_HEIGHT / 2

        while (svgBuilder.allEvents.any { it.x == x && it.y == y }) {
            y -= (20 * sqrt(3.0) / 2).toInt()
            x += 10
        }
        this.x = x
        this.y = y
    }

    private fun addRevertLine(
        g: G,
        svgBuilder: SvgBuilder,
    ) {
        if (type == EventType.Revert) {
            val revertedId = description.split(' ').last()
            val reverted = svgBuilder.allEvents.find { it.id == revertedId }!!

            val x1 = reverted.x + (this.x - reverted.x) / 2
            val y1 = this.y - ((this.x - reverted.x) / 3)
            g.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
                ObjectFactory().createPath(
                    ObjectFactory().createPath().withD(
                        "M${this.x} ${this.y} Q $x1 $y1, ${reverted.x + 15} ${reverted.y - 15}",
                    ),
                ),
            )
        }
    }

    private fun addLinkLine(
        other: Event,
        g: G,
    ) {
        val x1 = (if (type == EventType.Disjoin) -30 else 30) * abs(other.y - this.y) / EVENT_LINE_HEIGHT
        val xContactOffset = if (type == EventType.Disjoin) -10 else 10
        val y1 = (this.y + other.y) / 2

        var yOffset = if (this.y > other.y) 10 else -10
        if (type == EventType.DeprecatedBy) {
            yOffset *= 2
        }

        g.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
            ObjectFactory().createPath(
                ObjectFactory().createPath().withD(
                    "M${this.x} ${this.y} Q ${x1 + this.x} $y1, ${other.x + xContactOffset} ${other.y + yOffset}",
                ),
            ),
        )
    }

    private fun addCreatedCircle(g: G) {
        if (description.contains("created")) {
            g.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
                ObjectFactory().createCircle(
                    Circle()
                        .withCx("${this.x}")
                        .withCy("${this.y}")
                        .withR("14")
                        .withStrokeDasharray("2, 5")
                        .withClazz("eventCreated ${this.type.name}"),
                ),
            )
        }
    }

    private fun addEventCircleAndText(
        g: G,
        revertedClass: String,
    ) {
        g.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
            ObjectFactory().createCircle(
                ObjectFactory()
                    .createCircle()
                    .withCx("${this.x}")
                    .withCy("${this.y}")
                    .withR("10")
                    .withClazz("event ${this.type.name} $revertedClass"),
            ),
        )
        g.withSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass(
            ObjectFactory().createText(
                ObjectFactory()
                    .createText()
                    .withX("${this.x}")
                    .withY("${this.y}")
                    .withClazz("eventId")
                    .withContent(this.id),
            ),
        )
    }
}