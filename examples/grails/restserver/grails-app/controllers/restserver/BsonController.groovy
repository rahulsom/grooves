package restserver

import grails.rest.RestfulController
import org.grails.datastore.bson.json.JsonReader
import org.grails.datastore.bson.query.BsonQuery

/**
 * Performs Restful queries using BSON
 */
class BsonController<T> extends RestfulController<T> {
    BsonController(Class<T> resource) {
        super(resource)
    }
    BsonController(Class<T> resource, boolean readOnly) {
        super(resource, readOnly)
    }
    @Override
    protected List<T> listAllResources(Map params) {
        params.q ? BsonQuery.parse(resource, new JsonReader(params.q)).list(params) :
                super.listAllResources(params)
    }

}
