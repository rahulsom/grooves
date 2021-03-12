package com.github.rahulsom.grooves.asciidoctor

import org.asciidoctor.ast.AbstractBlock
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Reader
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Renders an Events block as SVG.
 *
 * @author Rahul Somasunderam
 */
class EventsBlock(name: String, config: Map<String, Any>) :
    BlockProcessor(name, mapOf("contexts" to listOf(":literal"), "content_model" to ":simple")) {

    override fun process(parent: AbstractBlock, reader: Reader, attributes: MutableMap<String, Any>?): Any {
        val docDir = File(parent.document.attributes["docdir"] as String)
        var projectDir = docDir
        val projectDirAttr = parent.document.attributes["projectdir"] as String
        repeat(projectDirAttr.split('/').size) { projectDir = projectDir.parentFile }
        val outDir = File(projectDir, "build/asciidoc/html5")

        val input = reader.readLines().joinToString("\n")

        var filename = (attributes as Map<*, *>)[2L] as String? ?: md5(input)
        filename = if (filename.endsWith(".svg")) filename else "$filename.svg"

        SvgBuilder(input).write(File(outDir, filename))

        val newAttributes = mapOf<String, Any>(
            "type" to ":image",
            "target" to filename,
            "format" to "svg"
        )

        val block = createBlock(parent, "image", input, newAttributes, attributes.mapKeys { it as Any })
        return block
    }

    private fun md5(input: String) =
        MessageDigest.getInstance("MD5").let { md5 ->
            md5.update(input.toByteArray())
            val hash = BigInteger(1, md5.digest())
            hash.toString(16)
        }
}