package ca.fuerth.ga4gh.schemaregistry;

import com.github.valb3r.letsencrypthelper.tomcat.TomcatWellKnownLetsEncryptChallengeEndpointConfig;
import org.springframework.context.annotation.Import;

/**
 * Configuration for automatic Let's Encrypt certificate generation. The imported configuration is already
 * conditional on the {@code server.ssl.enabled} property, so this configuration can be imported unconditionally.
 */
@Import(TomcatWellKnownLetsEncryptChallengeEndpointConfig.class)
public class LetsEncryptConfiguration {
}
