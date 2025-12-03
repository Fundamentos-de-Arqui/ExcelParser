package com.soulware.platform.docexcelparser.interfaces.web;

import com.soulware.platform.docexcelparser.infrastructure.liveness.LivenessCheck;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import java.util.HashMap;
import java.util.Map;
@Path("/health")
@ApplicationScoped
public class HealthController {

    @Inject
    @Liveness
    LivenessCheck livenessCheck;

    @GET
    @Path("/live")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> live() {
        HealthCheckResponse response = livenessCheck.call();
        Map<String, Object> map = new HashMap<>();
        map.put("status", response.getStatus().name()); // UP o DOWN
        map.put("checks", new Object[]{ Map.of(
                "name", "db",
                "status", response.getStatus().name()
        )});
        return map;
    }
}
