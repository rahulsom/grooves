package grooves.example.javaee.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ParamConverter;

public class DateParameterConverter implements ParamConverter<Date> {

    private static final String FORMAT = "yyyy-MM-dd"; // set the format to whatever you need

    @Override
    public Date fromString(String string) {
        final var simpleDateFormat = new SimpleDateFormat(FORMAT);
        try {
            return simpleDateFormat.parse(string);
        } catch (ParseException ex) {
            throw new WebApplicationException(ex);
        }
    }

    @Override
    public String toString(Date t) {
        return new SimpleDateFormat(FORMAT).format(t);
    }
}
