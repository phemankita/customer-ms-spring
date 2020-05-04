package customer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix="jwt")
public class JwtConfig {
	
	private String sharedSecret;
	
	public String getSharedSecret() {
		System.out.println("SHAREDSECRET ");
		return sharedSecret;
	}
	
	public void setSharedSecret(String sharedSecret) {
		System.out.println("set shared secret " + sharedSecret);
		this.sharedSecret = sharedSecret;
	}

}
