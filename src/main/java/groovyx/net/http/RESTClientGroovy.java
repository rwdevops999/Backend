package groovyx.net.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;

import org.codehaus.groovy.runtime.IOGroovyMethods;

public class RESTClientGroovy extends RESTClient {
    public RESTClientGroovy() { super(); }

    public RESTClientGroovy( Object defaultURI ) throws URISyntaxException {
        super( defaultURI );
    }

    @Override
    protected HttpResponseDecorator defaultSuccessHandler( HttpResponseDecorator resp, Object data )
            throws ResponseParseException {
        try {
            //If response is streaming, buffer it in a byte array:
            if (data instanceof InputStream) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                // we've updated the below line
                IOGroovyMethods.leftShift(buffer, (InputStream) data);
                resp.setData(new ByteArrayInputStream(buffer.toByteArray()));
                return resp;
            }
            
            if (data instanceof Reader) {
                StringWriter buffer = new StringWriter();
                // we've updated the below line
                IOGroovyMethods.leftShift(buffer, (Reader) data);
                resp.setData(new StringReader(buffer.toString()));
                return resp;
            }
            
            return super.defaultSuccessHandler(resp, data);
        } catch (IOException ex) {
            throw new ResponseParseException(resp, ex);
        }
    }
}
