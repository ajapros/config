package org.chenile.cconfig;

import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.sdk.cache.MemoryCache;
import org.chenile.cconfig.sdk.impl.CconfigClientImpl;
import org.chenile.cconfig.sdk.impl.CconfigRetrieverFactoryImpl;
import org.chenile.cconfig.sdk.impl.EnvBasedCconfigRetriever;
import org.chenile.cconfig.sdk.impl.JsonBasedCconfigRetriever;
import org.chenile.cconfig.sdk.impl.MessageBundleConfigRetriever;
import org.chenile.cconfig.spi.CconfigRetriever;
import org.chenile.cconfig.spi.ConfigContext;
import org.chenile.cconfig.spi.KeyManipulatingConfigRetriever;
import org.chenile.core.context.ContextContainer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class CconfigClientRetrieverFactoryTest {
    @After
    public void clearContext() {
        ContextContainer.CONTEXT_CONTAINER.clear();
    }

    @Test
    public void mergesMultipleRetrieversInPrecedenceOrder() throws Exception {
        CconfigRetrieverFactoryImpl factory = new CconfigRetrieverFactoryImpl();
        factory.register(new JsonBasedCconfigRetriever("org/chenile/cconfig/test", factory));
        factory.register(new StubRetriever(10, List.of(
                cconfig("ctest", "key1", "value2", "tenant0", null),
                cconfig("ctest", "key2", "{\"range\":[2,200]}", "tenant0", "fields.field2"),
                cconfig("ctest", "dynamicKey", "lowValue", "tenant0", null)
        )));
        factory.register(new StubRetriever(20, List.of(
                cconfig("ctest", "key1", "value3", "tenant0", null),
                cconfig("ctest", "key2", "{\"range\":[9,900]}", "tenant0", "fields.field2"),
                cconfig("ctest", "dynamicKey", "highValue", "tenant0", null)
        )));

        CconfigClientImpl client = client(factory);
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
    public void preservesExistingBehaviorWithSingleRetriever() throws Exception {
        CconfigRetrieverFactoryImpl factory = new CconfigRetrieverFactoryImpl();
        factory.register(new JsonBasedCconfigRetriever("org/chenile/cconfig/test", factory));
        factory.register(new StubRetriever(0, List.of(
                cconfig("ctest", "key1", "value2", "tenant0", null),
                cconfig("ctest", "key2", "{\"range\":[2,200]}", "tenant0", "fields.field2")
        )));

        CconfigClientImpl client = client(factory);
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
    public void jsonRetrieverRunsBeforeOtherRetrievers() {
        CconfigRetrieverFactoryImpl factory = new CconfigRetrieverFactoryImpl();
        JsonBasedCconfigRetriever jsonRetriever = new JsonBasedCconfigRetriever("org/chenile/cconfig/test", factory);
        factory.register(new StubRetriever(0, List.of()));
        factory.register(jsonRetriever);

        List<CconfigRetriever> retrievers = factory.getRetrievers();

        Assert.assertTrue(retrievers.get(0) instanceof JsonBasedCconfigRetriever);
        Assert.assertEquals(-1, retrievers.get(0).order());
    }

    @Test
    public void envRetrieverOverridesOtherSourcesForKnownKeys() throws Exception {
        CconfigRetrieverFactoryImpl factory = new CconfigRetrieverFactoryImpl();
        MockEnvironment environment = new MockEnvironment()
                .withProperty("__GLOBAL___ctest_key1", "globalValue")
                .withProperty("tenant0_ctest_key1", "envValue");
        JsonBasedCconfigRetriever jsonRetriever = new JsonBasedCconfigRetriever("org/chenile/cconfig/test", factory);
        factory.register(jsonRetriever);
        factory.register(new StubRetriever(0, List.of(
                cconfig("ctest", "key1", "dbValue", "tenant0", null)
        )));
        factory.register(new EnvBasedCconfigRetriever(environment, factory));

        CconfigClientImpl client = client(factory);
        ContextContainer.CONTEXT_CONTAINER.put("x-chenile-tenant-id", "tenant0");

        Map<String, Object> values = client.value("ctest", null);

        Assert.assertEquals("envValue", values.get("key1"));
    }

    @Test
    public void messageBundleRetrieverAddsLocalizedKeys() throws Exception {
        CconfigRetrieverFactoryImpl factory = new CconfigRetrieverFactoryImpl();
        factory.register(new JsonBasedCconfigRetriever("org/chenile/cconfig/test", factory));
        factory.register(new MessageBundleConfigRetriever("messages", factory));

        CconfigClientImpl client = client(factory);
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

    private CconfigClientImpl client(CconfigRetrieverFactoryImpl factory) throws Exception {
        CconfigClientImpl client = new CconfigClientImpl("org/chenile/cconfig/test", factory);
        setField(client, "memoryCache", new MemoryCache());
        setField(client, "contextContainer", ContextContainer.CONTEXT_CONTAINER);
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
        private int order;
        private List<Cconfig> configs;
        public StubRetriever(int order,List<Cconfig> configs){
            this.order = order;
            this.configs = configs;
        }
        @Override
        public List<Cconfig> retrieveCconfigs(ConfigContext configContext) {
            return configs.stream()
                    .filter(cc -> configContext.getModule().equals(cc.moduleName) &&
                            configContext.getCustomAttribute().equals(cc.customAttribute))
                    .toList();
        }

        @Override
        public int order() {
            return order;
        }
    }
}
