package com.github.rahulsom.grooves.transformations.internal;

import com.github.rahulsom.grooves.api.EventApplyOutcome;
import com.github.rahulsom.grooves.transformations.Query;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Adds the query interface to a query type
 *
 * @author Rahul Somasunderam
 */
@GroovyASTTransformation
public class QueryASTTransformation extends AbstractASTTransformation {

    private static final Class<Query> MY_CLASS = Query.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode annotatedNode = DefaultGroovyMethods.asType(nodes[1], AnnotatedNode.class);
        final AnnotationNode annotationNode = DefaultGroovyMethods.asType(nodes[0], AnnotationNode.class);

        if (MY_TYPE.equals(annotationNode.getClassNode()) && annotatedNode instanceof ClassNode) {
            final Expression theSnapshot = annotationNode.getMember("snapshot");
            Expression theAggregate = annotationNode.getMember("aggregate");
            final ClassNode theClassNode = DefaultGroovyMethods.asType(annotatedNode, ClassNode.class);
            log.fine("Checking " + theClassNode.getNameWithoutPackage() + " for methods");
            List<ClassNode> eventClasses =
                    AggregateASTTransformation.getEventsForAggregate(theAggregate.getType().getName());

            eventClasses.forEach(eventClass -> {
                final String methodName = "apply" + eventClass.getNameWithoutPackage();
                log.fine("  -> Checking for " + String.valueOf(methodName));

                List<MethodNode> methodsByName =
                        theClassNode.getMethods().stream().filter(it -> it.getName().equals(methodName)).
                                collect(Collectors.toList());

                final String methodSignature = String.format("%s %s(%s event, %s snapshot)",
                        ClassHelper.make(EventApplyOutcome.class).getName(), methodName, eventClass.getName(),
                        theSnapshot.getType().getName());

                if (methodsByName.size() == 0) {
                    addError("Missing expected method " + String.valueOf(methodSignature), annotationNode);
                } else {
                    Optional<MethodNode> matchingMethod = methodsByName.stream().
                            filter(implMethod -> {
                                final Parameter[] parameters = implMethod.getParameters();
                                return parameters != null && parameters.length == 2 &&
                                        implMethod.getReturnType().getName().equals(EventApplyOutcome.class.getName()) &&
                                        parameters[0].getType().getName().equals(eventClass.getName()) &&
                                        parameters[1].getType().getName().equals(theSnapshot.getType().getName());
                            }).
                            findFirst();

                    if (!matchingMethod.isPresent()) {
                        addError("Missing expected method " + String.valueOf(methodSignature), annotationNode);
                    }
                }
            });

        }

    }
}
