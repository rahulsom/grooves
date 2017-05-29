package grooves.example.javaee.util;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;

@Provider
public class DateParameterConverterProvider implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> type, Type type1, Annotation[] antns) {
        if (Date.class.equals(type)) {
            @SuppressWarnings("unchecked")
            ParamConverter<T> paramConverter = (ParamConverter<T>) new DateParameterConverter();
            return paramConverter;
        }
        return null;
    }

}