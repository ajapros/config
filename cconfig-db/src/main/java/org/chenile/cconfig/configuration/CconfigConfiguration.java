package org.chenile.cconfig.configuration;

import org.chenile.cconfig.sdk.CconfigClient;
import org.chenile.cconfig.sdk.impl.CconfigClientImpl;
import org.chenile.cconfig.service.CconfigQueryService;
import org.chenile.cconfig.spi.ConfigContext;
import org.chenile.cconfig.service.CconfigService;
import org.chenile.cconfig.service.healthcheck.CconfigHealthChecker;
import org.chenile.cconfig.service.impl.CconfigQueryServiceImpl;
import org.chenile.cconfig.service.impl.CconfigServiceImpl;
import org.chenile.cconfig.service.impl.DbBasedCconfigRetriever;
import org.chenile.owiz.BeanFactoryAdapter;
import org.chenile.owiz.OrchExecutor;
import org.chenile.owiz.config.impl.XmlOrchConfigurator;
import org.chenile.owiz.impl.OrchExecutorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
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

	@Autowired ApplicationContext applicationContext;

	@Bean public DbBasedCconfigRetriever dbBasedCconfigRetriever(){
		return new DbBasedCconfigRetriever();
	}

	@Bean public OrchExecutor<ConfigContext> cconfigOrchExecutor() throws Exception {
		XmlOrchConfigurator<ConfigContext> xmlOrchConfigurator = new XmlOrchConfigurator<>();
		xmlOrchConfigurator.setBeanFactoryAdapter(new BeanFactoryAdapter() {
			@Override
			public Object lookup(String componentName) {
				try {
					return applicationContext.getBean(componentName);
				}catch (Exception e) {
					return null;
				}
			}
		});
		xmlOrchConfigurator.setFilename("org/chenile/cconfig/cconfig-orch.xml");
		OrchExecutorImpl<ConfigContext> executor = new OrchExecutorImpl<>();
		executor.setOrchConfigurator(xmlOrchConfigurator);
		return executor;
	}

	@Bean public CconfigClient serverCconfigClient(@Autowired OrchExecutor<ConfigContext> cconfigOrchExecutor){
		return new CconfigClientImpl(cconfigOrchExecutor);
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
