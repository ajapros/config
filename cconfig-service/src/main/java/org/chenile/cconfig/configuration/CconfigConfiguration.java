package org.chenile.cconfig.configuration;

import org.chenile.cconfig.sdk.CconfigClient;
import org.chenile.cconfig.sdk.CconfigClientImpl;
import org.chenile.cconfig.service.CconfigQueryService;
import org.chenile.cconfig.service.CconfigRetriever;
import org.chenile.cconfig.service.CconfigService;
import org.chenile.cconfig.service.healthcheck.CconfigHealthChecker;
import org.chenile.cconfig.service.impl.CconfigQueryServiceImpl;
import org.chenile.cconfig.service.impl.CconfigServiceImpl;
import org.chenile.cconfig.service.impl.DbBasedCconfigRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 Initialize all the Chenile config related services.
*/
@Configuration
public class CconfigConfiguration {
	/**
	 * Chenile Config will scan this path for all config JSONs. All configs are expected
	 * to be of the form {module}.{key}.<br/>
	 * A JSON file module.json file must exist in this path. <br/>
	 * It should contain "key" at the root level. See test cases for more info.
	 */
	@Value("${chenile.config.path:org/chenile/config}")
	private String configPath;

	/**
	 * Pass all the modules whose values are needed in this instance of the config.
	 */
	@Value("${chenile.config.modules:}")
	private List<String> modules;

	/**
	 * Pass all the modules whose values are needed in this instance of the config.
	 */
	@Value("${chenile.config.customAttributes:}")
	private List<String> customAttributes;

	@Bean public CconfigRetriever dbBasedCconfigRetriever(){
		return new DbBasedCconfigRetriever();
	}

	@Bean public CconfigClient serverCconfigClient(@Autowired @Qualifier("dbBasedCconfigRetriever") CconfigRetriever cconfigRetriever){
		return new CconfigClientImpl(configPath,cconfigRetriever);
	}
	@Bean public CconfigQueryService _cconfigQueryService_(@Autowired @Qualifier("serverCconfigClient") CconfigClient client) {
		return new CconfigQueryServiceImpl(client);
	}
	@Bean public CconfigService _cconfigService_() {
		return new CconfigServiceImpl();
	}
	@Bean CconfigHealthChecker cconfigHealthChecker(){
    	return new CconfigHealthChecker();
    }
}
