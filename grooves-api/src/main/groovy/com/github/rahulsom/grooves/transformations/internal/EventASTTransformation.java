package com.github.rahulsom.grooves.transformations.internal;

import com.github.rahulsom.grooves.transformations.Event;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 * Adds methods corresponding to event into the query interface
 *
 * @author Rahul Somasunderam
 */
@GroovyASTTransformation
public class EventASTTransformation extends AbstractASTTransformation {

    private static final Class<Event> MY_CLASS = Event.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode annotatedNode = DefaultGroovyMethods.asType(nodes[1], AnnotatedNode.class);
        AnnotationNode annotationNode = DefaultGroovyMethods.asType(nodes[0], AnnotationNode.class);

        if (MY_TYPE.equals(annotationNode.getClassNode()) && annotatedNode instanceof ClassNode) {
            final Expression theAggregate = annotationNode.getMember("value");
            final ClassNode theClassNode = DefaultGroovyMethods.asType(annotatedNode, ClassNode.class);
            log.fine(() -> MessageFormat.format("Adding event {0} to aggregate {1}", theClassNode.getNameWithoutPackage(), theAggregate.getType().getName()));
            AggregateASTTransformation.addEventToAggregate(theAggregate.getType().getName(), theClassNode);
        }

    }
}
