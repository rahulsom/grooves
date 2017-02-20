package com.github.rahulsom.grooves.internal

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.GenericsType

import static org.codehaus.groovy.ast.ClassHelper.make

/**
 * Created by rahul on 2/14/17.
 */
class AstUtils {
    static ClassNode replaceGenericsPlaceholders(Map<String, ClassNode> genericsPlaceholders, ClassNode type) {
        return replaceGenericsPlaceholders(genericsPlaceholders, type, null)
    }

    static ClassNode replaceGenericsPlaceholders(
            Map<String, ClassNode> genericsPlaceholders, ClassNode type, ClassNode defaultPlaceholder) {
        if (type.isArray()) {
            return replaceGenericsPlaceholders(genericsPlaceholders, type.getComponentType()).makeArray()
        }

        if (!type.isUsingGenerics() && !type.isRedirectNode()) {
            return type.getPlainNodeReference()
        }

        if (type.isGenericsPlaceHolder() && genericsPlaceholders != null) {
            final ClassNode placeHolderType
            if (genericsPlaceholders.containsKey(type.getUnresolvedName())) {
                placeHolderType = genericsPlaceholders.get(type.getUnresolvedName())
            } else {
                placeHolderType = defaultPlaceholder
            }
            if (placeHolderType != null) {
                return placeHolderType.getPlainNodeReference()
            } else {
                return make(Object.class).getPlainNodeReference()
            }
        }

        final ClassNode nonGen = type.getPlainNodeReference()

        if ("java.lang.Object".equals(type.getName())) {
            nonGen.setGenericsPlaceHolder(false)
            nonGen.setGenericsTypes(null)
            nonGen.setUsingGenerics(false)
        } else {
            if (type.isUsingGenerics()) {
                GenericsType[] parametrized = type.getGenericsTypes()
                if (parametrized != null && parametrized.length > 0) {
                    GenericsType[] copiedGenericsTypes = new GenericsType[parametrized.length]
                    for (int i = 0; i < parametrized.length; i++) {
                        GenericsType parametrizedType = parametrized[i]
                        GenericsType copiedGenericsType = null
                        if (parametrizedType.isPlaceholder() && genericsPlaceholders != null) {
                            ClassNode placeHolderType = genericsPlaceholders.get(parametrizedType.getName())
                            if (placeHolderType != null) {
                                copiedGenericsType = new GenericsType(placeHolderType.getPlainNodeReference())
                            } else {
                                copiedGenericsType = new GenericsType(make(Object.class).getPlainNodeReference())
                            }
                        } else {
                            copiedGenericsType = new GenericsType(
                                    replaceGenericsPlaceholders(genericsPlaceholders, parametrizedType.getType()))
                        }
                        copiedGenericsTypes[i] = copiedGenericsType
                    }
                    nonGen.setGenericsTypes(copiedGenericsTypes)
                }
            }
        }

        return nonGen
    }

}
