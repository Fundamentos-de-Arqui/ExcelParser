package com.soulware.platform.docexcelparser.infrastructure.liveness;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;



@Liveness
@ApplicationScoped
public class LivenessCheck implements HealthCheck {


    //Este es el JDNI name del datasource
    @Resource(lookup = "java:/jdbc/ExcelParserMySQLDS")

    //ejemplo -> @Resource(lookup = "java:jboss/datasources/MedicalHistories")

            DataSource dataSource;

    @Override
    public HealthCheckResponse call() {
        boolean dbOk = checkDb();
        return dbOk ?
                HealthCheckResponse.up("db") :
                HealthCheckResponse.down("db");
    }

    private boolean checkDb() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}
