package com.chit.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
    info = @Info(
        title = "Chit API",
        description = "CRUD operation",
        version = "v1"
    ),
    servers = {
        @Server(
            description = "localhost",
            url = "http://localhost:9091/"
        ),
        @Server(
            description = "Dev",
            url = "https://dev.ardora.in/api"
        ),
        @Server(
            description = "Prod",
            url = "https://ardora.in/api/"
        )
    },
    security = @SecurityRequirement(
        name = "authBearer"
    )
)
@SecurityScheme(
    name = "authBearer",
    in = SecuritySchemeIn.HEADER,
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "JWT Bearer Token for Authentication"
)
public class SwaggerConfig {

}
