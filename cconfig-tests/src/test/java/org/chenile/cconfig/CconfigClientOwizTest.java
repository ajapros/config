package org.chenile.cconfig;

import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.sdk.cache.MemoryCache;
import org.chenile.cconfig.sdk.impl.CconfigClientImpl;
import org.chenile.cconfig.sdk.impl.EnvBasedCconfigRetriever;
import org.chenile.cconfig.sdk.impl.JsonBasedCconfigRetriever;
import org.chenile.cconfig.sdk.impl.MessageBundleConfigRetriever;
import org.chenile.cconfig.spi.ConfigContext;
import org.chenile.cconfig.spi.KeyManipulatingConfigRetriever;
import org.chenile.core.context.ContextContainer;
import org.chenile.owiz.BeanFactoryAdapter;
import org.chenile.owiz.Command;
import org.chenile.owiz.OrchExecutor;
import org.chenile.owiz.config.impl.XmlOrchConfigurator;
import org.chenile.owiz.impl.OrchExecutorImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CconfigClientOwizTest {
    @After
    public void clearContext() {
        ContextContainer.CONTEXT_CONTAINER.clear();
    }

    @Test
    public void mergesMultipleRetrieversThroughOwizOrchestration() throws Exception {
        OrchExecutor<ConfigContext> orchExecutor = orchExecutor(Map.of(
                "jsonBasedCconfigRetriever", new JsonBasedCconfigRetriever("org/chenile/cconfig/test"),
                "stubRetrieverLow", new StubRetriever(List.of(
                        cconfig("ctest", "key1", "value2", "tenant0", null),
                        cconfig("ctest", "key2", "{\"range\":[2,200]}", "tenant0", "fields.field2"),
                        cconfig("ctest", "dynamicKey", "lowValue", "tenant0", null)
                )),
                "stubRetrieverHigh", new StubRetriever(List.of(
                        cconfig("ctest", "key1", "value3", "tenant0", null),
                        cconfig("ctest", "key2", "{\"range\":[9,900]}", "tenant0", "fields.field2"),
                        cconfig("ctest", "dynamicKey", "highValue", "tenant0", null)
                ))
        ), "org/chenile/cconfig/test/owiz/merge-orch.xml");

        CconfigClientImpl client = client(orchExecutor);
        ContextContainer.CONTEXT_CONTAINER.put("x-chenile-tenant-id", "tenant0");

        Map<String, Object> values = client.value("ctest", null);

        Assert.assertEquals("value3", values.get("key1"));
        Assert.assertEquals("highValue", values.get("dynamicKey"));

        @SuppressWarnings("unchecked")
        Map<String, Object> key2 = (Map<String, Object>) values.get("key2");
        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) key2.get("fields");
        @SuppressWarnings("unchecked")
        Map<String, Object> field2 = (Map<String, Object>) fields.get("field2");
        @SuppressWarnings("unchecked")
        List<Integer> range = (List<Integer>) field2.get("range");
        Assert.assertEquals(List.of(9, 900), range);
    }

    @Test
    public void preservesExistingBehaviorWithSingleRetrieverChain() throws Exception {
        OrchExecutor<ConfigContext> orchExecutor = orchExecutor(Map.of(
                "jsonBasedCconfigRetriever", new JsonBasedCconfigRetriever("org/chenile/cconfig/test"),
                "stubRetrieverSingle", new StubRetriever(List.of(
                        cconfig("ctest", "key1", "value2", "tenant0", null),
                        cconfig("ctest", "key2", "{\"range\":[2,200]}", "tenant0", "fields.field2")
                ))
        ), "org/chenile/cconfig/test/owiz/single-orch.xml");

        CconfigClientImpl client = client(orchExecutor);
        ContextContainer.CONTEXT_CONTAINER.put("x-chenile-tenant-id", "tenant0");

        Map<String, Object> values = client.value("ctest", null);

        Assert.assertEquals("value2", values.get("key1"));

        @SuppressWarnings("unchecked")
        Map<String, Object> key2 = (Map<String, Object>) values.get("key2");
        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) key2.get("fields");
        @SuppressWarnings("unchecked")
        Map<String, Object> field2 = (Map<String, Object>) fields.get("field2");
        @SuppressWarnings("unchecked")
        List<Integer> range = (List<Integer>) field2.get("range");
        Assert.assertEquals(List.of(2, 200), range);
    }

    @Test
    public void envRetrieverOverridesOtherSourcesForKnownKeys() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("__GLOBAL___ctest_key1", "globalValue")
                .withProperty("tenant0_ctest_key1", "envValue");
        OrchExecutor<ConfigContext> orchExecutor = orchExecutor(Map.of(
                "jsonBasedCconfigRetriever", new JsonBasedCconfigRetriever("org/chenile/cconfig/test"),
                "stubRetrieverSingle", new StubRetriever(List.of(
                        cconfig("ctest", "key1", "dbValue", "tenant0", null)
                )),
                "envBasedCconfigRetriever", new EnvBasedCconfigRetriever(environment)
        ), "org/chenile/cconfig/test/owiz/env-orch.xml");

        CconfigClientImpl client = client(orchExecutor);
        ContextContainer.CONTEXT_CONTAINER.put("x-chenile-tenant-id", "tenant0");

        Map<String, Object> values = client.value("ctest", null);
        Assert.assertEquals("envValue", values.get("key1"));
    }

    @Test
    public void messageBundleRetrieverAddsLocalizedKeys() throws Exception {
        OrchExecutor<ConfigContext> orchExecutor = orchExecutor(Map.of(
                "jsonBasedCconfigRetriever", new JsonBasedCconfigRetriever("org/chenile/cconfig/test"),
                "messageBundleConfigRetriever", new MessageBundleConfigRetriever("messages")
        ), "org/chenile/cconfig/test/owiz/message-orch.xml");

        CconfigClientImpl client = client(orchExecutor);
        ContextContainer.CONTEXT_CONTAINER.put("x-chenile-tenant-id", "tenant0");

        Map<String, Object> values = client.value("ctest", null);

        @SuppressWarnings("unchecked")
        Map<String, String> greeting = (Map<String, String>) values.get("greeting");
        @SuppressWarnings("unchecked")
        Map<String, String> banner = (Map<String, String>) values.get("banner");
        @SuppressWarnings("unchecked")
        Map<String, String> welcome = (Map<String, String>) values.get("welcome");

        Assert.assertEquals("Hello", greeting.get("default"));
        Assert.assertEquals("Bonjour", greeting.get("fr"));
        Assert.assertEquals("Global Banner", banner.get("default"));
        Assert.assertEquals("Banniere Globale", banner.get("fr"));
        Assert.assertEquals("Tenant Welcome", welcome.get("default"));
        Assert.assertEquals("Bienvenue Client", welcome.get("fr"));
    }

    private OrchExecutor<ConfigContext> orchExecutor(Map<String, Command<ConfigContext>> commands, String filename) throws Exception {
        XmlOrchConfigurator<ConfigContext> xmlOrchConfigurator = new XmlOrchConfigurator<>();
        Map<String, Command<ConfigContext>> commandMap = new HashMap<>(commands);
        xmlOrchConfigurator.setBeanFactoryAdapter(new BeanFactoryAdapter() {
            @Override
            public Object lookup(String componentName) {
                return commandMap.get(componentName);
            }
        });
        xmlOrchConfigurator.setFilename(filename);
        OrchExecutorImpl<ConfigContext> orchExecutor = new OrchExecutorImpl<>();
        orchExecutor.setOrchConfigurator(xmlOrchConfigurator);
        return orchExecutor;
    }

    private CconfigClientImpl client(OrchExecutor<ConfigContext> orchExecutor) throws Exception {
        CconfigClientImpl client = new CconfigClientImpl(orchExecutor);
        setField(client, "memoryCache", new MemoryCache());
        return client;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Cconfig cconfig(String module, String key, String value, String customAttribute, String path) {
        Cconfig cconfig = new Cconfig();
        cconfig.moduleName = module;
        cconfig.keyName = key;
        cconfig.avalue = value;
        cconfig.customAttribute = customAttribute;
        cconfig.path = path;
        return cconfig;
    }

    private static class StubRetriever extends KeyManipulatingConfigRetriever {
        private final List<Cconfig> configs;

        private StubRetriever(List<Cconfig> configs) {
            this.configs = configs;
        }

        @Override
        public List<Cconfig> retrieveCconfigs(ConfigContext configContext) {
            return configs.stream()
                    .filter(cc -> configContext.getModule().equals(cc.moduleName) &&
                            configContext.getCustomAttribute().equals(cc.customAttribute))
                    .toList();
        }
    }
}
