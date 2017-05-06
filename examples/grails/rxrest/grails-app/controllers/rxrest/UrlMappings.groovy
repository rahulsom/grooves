package rxrest

@SuppressWarnings(['DuplicateStringLiteral'])
class UrlMappings {

    static mappings = {
        delete "/$controller/$id(.$format)?"(action: 'delete')
        get "/$controller(.$format)?"(action: 'index')
        get "/$controller/$id(.$format)?"(action: 'show')
        post "/$controller(.$format)?"(action: 'save')
        put "/$controller/$id(.$format)?"(action: 'update')
        patch "/$controller/$id(.$format)?"(action: 'patch')

        "/patient/account/$id(.$format)?"(controller: 'patient', action: 'account')
        "/patient/health/$id(.$format)?"(controller: 'patient', action: 'health')
        "/patient/show/$id(.$format)?"(controller: 'patient', action: 'show')

        '/'(controller: 'application', action: 'index')
        '500'(view: '/error')
        '404'(view: '/notFound')
    }
}
