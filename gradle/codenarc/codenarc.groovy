ruleset {

    ruleset('rulesets/basic.xml')
    ruleset('rulesets/braces.xml')
    ruleset('rulesets/concurrency.xml')
    ruleset('rulesets/comments.xml') {
        ClassJavadoc(enabled: false)
        JavadocEmptyFirstLine(enabled: false)
        JavadocEmptyLastLine(enabled: false)
    }
    ruleset('rulesets/convention.xml') {
        CompileStatic(enabled: false)
        FieldTypeRequired(enabled: false)
        NoDef(enabled: false)
        VariableTypeRequired(enabled: false)
        MethodReturnTypeRequired(enabled: false)
        NoJavaUtilDate(enabled: false)
        ImplicitClosureParameter(enabled: false)
        ImplicitReturnStatement(enabled: false)
    }
    ruleset('rulesets/design.xml')
    // ruleset('rulesets/dry.xml')
    ruleset('rulesets/enhanced.xml')
    ruleset('rulesets/exceptions.xml')
    ruleset('rulesets/formatting.xml') {
        SpaceAroundMapEntryColon {
            characterBeforeColonRegex = /./
            characterAfterColonRegex = /\s/
        }
        Indentation(enabled: false)
        ClassStartsWithBlankLine(enabled: false)
        ClassEndsWithBlankLine(enabled: false)
    }
    ruleset('rulesets/generic.xml')
    ruleset('rulesets/grails.xml')
    ruleset('rulesets/groovyism.xml')
    ruleset('rulesets/imports.xml') {
        MisorderedStaticImports {
            comesBefore = false
        }
        NoWildcardImports(enabled: false)
    }
    ruleset('rulesets/jdbc.xml')
    ruleset('rulesets/junit.xml') {
        JUnitPublicNonTestMethod(enabled: false)
        JUnitPublicProperty(enabled: false)
    }
    ruleset('rulesets/logging.xml')
    ruleset('rulesets/naming.xml') {
        MethodName(enabled: false)
    }
    ruleset('rulesets/serialization.xml')
    ruleset('rulesets/size.xml')
    ruleset('rulesets/unnecessary.xml') {
        UnnecessaryBooleanExpression(enabled: false)
    }
    ruleset('rulesets/unused.xml')

}