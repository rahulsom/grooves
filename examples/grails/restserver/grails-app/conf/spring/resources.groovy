import org.springframework.web.filter.CommonsRequestLoggingFilter

// Place your Spring DSL code here
beans = {
    requestLoggingFilter(CommonsRequestLoggingFilter,
            includeClientInfo: true,
            includeQueryString: true,
            includePayload: true)
}
