package com.foco.boot.arthas.spring.endpoints;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * @author ChenMing
 * @date 2022/4/6
 */
@ConditionalOnProperty(name = "spring.arthas.enabled", matchIfMissing = true)
public class ArthasEndPointAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnAvailableEndpoint
	public ArthasEndPoint arthasEndPoint() {
		return new ArthasEndPoint();
	}
}
