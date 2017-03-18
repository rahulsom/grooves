package com.github.rahulsom.grooves.transformations.internal

import com.github.rahulsom.grooves.transformations.Query
import com.github.rahulsom.grooves.api.EventApplyOutcome
import groovy.transform.CompileStatic
import groovy.util.logging.Log
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.ClassHelper.make

/**
 * Adds the query interface to a query type
 *
 * @author Rahul Somasunderam
 */
@CompileStatic
@GroovyASTTransformation()
@Log
class QueryASTTransformation extends AbstractASTTransformation {

    private static final Class<Query> MY_CLASS = Query.class
    private static final ClassNode MY_TYPE = make(MY_CLASS)

    static String describeMethod(MethodNode methodNode, ClassNode snapshotType = null) {
        def params = methodNode.parameters.toList().
                collect { "${it.type.name == 'SnapshotType' ? snapshotType.name : it.type.name} ${it.name}" }.
                join(', ')
        "${methodNode.returnType.name} ${methodNode.name}(${params})"
    }

    private static <A, B> List<Tuple2<A, B>> zip(List<A> a, List<B> b) {
        def result = []
        a.eachWithIndex { v, idx ->
            result << new Tuple2(v, b[idx])
        }
        result
    }

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source)
        AnnotatedNode annotatedNode = nodes[1] as AnnotatedNode
        AnnotationNode annotationNode = nodes[0] as AnnotationNode

        if (MY_TYPE == annotationNode.classNode && annotatedNode instanceof ClassNode) {
            def theSnapshot = annotationNode.getMember('snapshot')
            def theAggregate = annotationNode.getMember('aggregate')
            def theClassNode = annotatedNode as ClassNode
            log.fine "Checking ${theClassNode.nameWithoutPackage} for methods"
            def eventClasses = AggregateASTTransformation.getEventsForAggregate(theAggregate.type.name)

            eventClasses.each { eventClass ->
                def methodName = "apply${eventClass.nameWithoutPackage}"
                log.fine "  -> Checking for ${methodName}"

                def methodsByName = theClassNode.methods.
                        findAll { it.name == methodName.toString() }

                def methodSignature = "${make(EventApplyOutcome).name} $methodName(${eventClass.name} event, ${theSnapshot.type.name} snapshot)"

                if (methodsByName.size() == 0) {
                    addError("Missing expected method $methodSignature", annotationNode)
                } else {
                    def matchingMethod = methodsByName.find { implMethod ->
                        implMethod.parameters?.length == 2 &&
                                implMethod.returnType.name == 'com.github.rahulsom.grooves.api.EventApplyOutcome' &&
                                implMethod.parameters[0].type.name == eventClass.name &&
                                implMethod.parameters[1].type.name == theSnapshot.type.name
                    }
                    if (!matchingMethod) {
                        addError("Missing expected method ${methodSignature}", annotationNode)
                    }
                }
            }

        }
    }

}

