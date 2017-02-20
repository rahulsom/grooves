package com.github.rahulsom.grooves.internal

import com.github.rahulsom.grooves.annotations.Event
import groovy.transform.CompileStatic
import groovy.util.logging.Log
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.ClassHelper.make

/**
 * Adds methods corresponding to event into the query interface
 *
 * @author Rahul Somasunderam
 */
@CompileStatic
@GroovyASTTransformation()
@Log
class EventASTTransformation extends AbstractASTTransformation {

    private static final Class<Event> MY_CLASS = Event.class
    private static final ClassNode MY_TYPE = make(MY_CLASS)

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source)
        AnnotatedNode annotatedNode = nodes[1] as AnnotatedNode
        AnnotationNode annotationNode = nodes[0] as AnnotationNode

        if (MY_TYPE == annotationNode.classNode && annotatedNode instanceof ClassNode) {
            def theAggregate = annotationNode.getMember('value')
            def theClassNode = annotatedNode as ClassNode
            log.fine "Adding event ${theClassNode.nameWithoutPackage} to aggregate ${theAggregate.type.name}"
            AggregateASTTransformation.addEventToAggregate(theAggregate.type.name, theClassNode)
        }
    }
}

