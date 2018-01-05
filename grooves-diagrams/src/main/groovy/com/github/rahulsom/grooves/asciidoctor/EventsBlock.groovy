package com.github.rahulsom.grooves.asciidoctor

import groovy.util.logging.Slf4j
import org.asciidoctor.ast.AbstractBlock
import org.asciidoctor.extension.BlockProcessor
import org.asciidoctor.extension.Reader

import java.security.MessageDigest

/**
 * Renders an Events block as SVG.
 *
 * @author Rahul Somasunderam
 */
@Slf4j
class EventsBlock extends BlockProcessor {

    EventsBlock(String name, Map<String, Object> config) {
        super(name, [contexts: [':literal'], content_model: ':simple'])
    }

    @Override
    Object process(AbstractBlock parent, Reader reader, Map<String, Object> attributes) {

        def docDir = new File(parent.document.attributes['docdir'] as String)
        def projectDir = docDir
        (parent.document.attributes['projectdir'] as String).split('/').size().times {
            projectDir = projectDir.parentFile
        }
        def outDir = new File(projectDir, "build/asciidoc/html5")

        def input = reader.readLines().join('\n')

        String filename = attributes.get(2 as Long) as String ?: md5(input)
        filename = filename.endsWith('.svg') ? filename : "${filename}.svg"

        new SvgBuilder(input).write(new File(outDir, filename))

        final Map options = [
                type  : ':image',
                target: filename,
                format: 'svg',
        ]

        def inlineTwitterLink = createBlock(parent, 'image', input, options, attributes)

        inlineTwitterLink
    }

    private static String md5(String input) {
        MessageDigest md5 = MessageDigest.getInstance('MD5')
        md5.update(input.bytes)
        BigInteger hash = new BigInteger(1, md5.digest())
        hash.toString(16)
    }

}