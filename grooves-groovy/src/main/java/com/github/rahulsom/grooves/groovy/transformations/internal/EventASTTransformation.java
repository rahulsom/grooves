package com.github.rahulsom.grooves.groovy.transformations.internal;

import com.github.rahulsom.grooves.groovy.transformations.Event;
import com.github.rahulsom.grooves.groovy.transformations.Query;
import java.text.MessageFormat;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

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
        final var annotatedNode = (AnnotatedNode) nodes[1];
        final var annotationNode = (AnnotationNode) nodes[0];

        if (MY_TYPE.equals(annotationNode.getClassNode()) && annotatedNode instanceof ClassNode theClassNode) {
            final var theAggregate = annotationNode.getMember("value");
            final var aggregateClassName = theAggregate.getType().getName();
            log.fine(() -> MessageFormat.format(
                    "Adding event {0} to aggregate {1}", theClassNode.getNameWithoutPackage(), aggregateClassName));
            AggregateASTTransformation.addEventToAggregate(aggregateClassName, theClassNode);
        }
    }
}
