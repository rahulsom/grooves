package com.github.rahulsom.grooves.transformations.internal;

import com.github.rahulsom.grooves.transformations.Event;
import com.github.rahulsom.grooves.transformations.Query;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.text.MessageFormat;
import java.util.logging.Logger;

/**
 * Adds methods corresponding to the {@link Event} into the list of methods that a {@link Query}
 * must handle.
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
        AnnotatedNode annotatedNode = (AnnotatedNode) nodes[1];
        AnnotationNode annotationNode = (AnnotationNode) nodes[0];

        if (MY_TYPE.equals(annotationNode.getClassNode()) && annotatedNode instanceof ClassNode) {
            final Expression theAggregate = annotationNode.getMember("value");
            final ClassNode theClassNode = (ClassNode) annotatedNode;
            final String aggregateClassName = theAggregate.getType().getName();
            log.fine(() -> MessageFormat.format("Adding event {0} to aggregate {1}",
                    theClassNode.getNameWithoutPackage(), aggregateClassName));
            AggregateASTTransformation.addEventToAggregate(aggregateClassName, theClassNode);
        }

    }
}
