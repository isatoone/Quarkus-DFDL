package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jdom2.transform.XSLTransformException;

import java.io.IOException;
import java.net.URISyntaxException;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
	    try {
            HelloWorld helloWorld = new HelloWorld();
            helloWorld.start();
        } catch(IOException | XSLTransformException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return "Hello from Quarkus REST";
    }
}
