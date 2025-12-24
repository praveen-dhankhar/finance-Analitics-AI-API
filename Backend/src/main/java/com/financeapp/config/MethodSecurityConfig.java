package com.financeapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true
)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    
    // This configuration enables:
    // @PreAuthorize and @PostAuthorize annotations
    // @Secured annotations
    // JSR-250 annotations (@RolesAllowed, @PermitAll, @DenyAll)
}
