package application.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "spring.application.cloudant")
public class CloudantProperties {
    private String protocol;
    private String username;
    private String password;
    private String host;
    private int port;
    private String database;
    private String sharedSecret;
}
