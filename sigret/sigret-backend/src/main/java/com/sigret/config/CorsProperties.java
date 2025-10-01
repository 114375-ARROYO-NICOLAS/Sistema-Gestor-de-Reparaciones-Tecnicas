package com.sigret.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
    
    private String allowedOrigins;
    private String allowedMethods;
    private String allowedHeaders;
    private Boolean allowCredentials;
    
    // Constructors
    public CorsProperties() {}
    
    // Getters and Setters
    public String getAllowedOrigins() {
        return allowedOrigins;
    }
    
    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
    
    public String getAllowedMethods() {
        return allowedMethods;
    }
    
    public void setAllowedMethods(String allowedMethods) {
        this.allowedMethods = allowedMethods;
    }
    
    public String getAllowedHeaders() {
        return allowedHeaders;
    }
    
    public void setAllowedHeaders(String allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }
    
    public Boolean getAllowCredentials() {
        return allowCredentials;
    }
    
    public void setAllowCredentials(Boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }
}
