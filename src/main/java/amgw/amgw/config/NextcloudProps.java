package amgw.amgw.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "nextcloud")
@Getter
@Setter
public class NextcloudProps {
    private String baseUrl;
}
