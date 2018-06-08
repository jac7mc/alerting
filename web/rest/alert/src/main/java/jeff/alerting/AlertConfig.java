package jeff.alerting;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/alerts")
public class AlertConfig extends ResourceConfig {

    public AlertConfig(){
        packages("jeff.alerting.services");
    }
}
