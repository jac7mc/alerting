package jeff.alerting.services;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/alert")
public class AlertsService {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response view(
            @Context HttpHeaders headers,
            @QueryParam("fullRecord")boolean fullRecord)
            throws IOException{
        String testResponse = "Boop";

        return Response.ok().entity(testResponse).build();
    }

}