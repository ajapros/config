package org.chenile.cconfig;

import com.fasterxml.jackson.core.type.TypeReference;
import org.chenile.cconfig.configuration.dao.CconfigRepository;
import org.chenile.cconfig.model.Cconfig;
import org.chenile.cconfig.sdk.CconfigClient;
import org.chenile.core.context.ContextContainer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringTestConfig.class)
@ActiveProfiles("unittest")
public class CconfigClientAutowiredTest {
    @Autowired
    private CconfigClient cconfigClient;

    @Autowired
    private CconfigRepository cconfigRepository;

    @After
    public void clearContext() {
        ContextContainer.CONTEXT_CONTAINER.clear();
    }

    @Test
    public void autowiredClientCanConvertWholeJsonOverrideToPojo() {
        ContextContainer.CONTEXT_CONTAINER.put("x-chenile-tenant-id", "typed-tenant");
        save(cconfig("typed-object", "ctest", "key2",
                "{\"abc\":\"db-override\",\"fields\":{\"field1\":{\"range\":[11,22]}}}",
                "typed-tenant", null));

        CconfigClientOwizTest.Key2Value key2 = cconfigClient.value("ctest", "key2", CconfigClientOwizTest.Key2Value.class);

        Assert.assertEquals("db-override", key2.abc);
        Assert.assertEquals(List.of(11, 22), key2.fields.field1.range);
    }

    @Test
    public void autowiredClientCanConvertWholeJsonArrayOverrideToTypedList() {
        ContextContainer.CONTEXT_CONTAINER.put("x-chenile-tenant-id", "typed-array-tenant");
        save(cconfig("typed-array", "ctest", "key7",
                "[{\"range\":[5,6]},{\"range\":[7,8]}]",
                "typed-array-tenant", null));

        List<CconfigClientOwizTest.RangeHolder> key7 = cconfigClient.value("ctest", "key7",
                new TypeReference<List<CconfigClientOwizTest.RangeHolder>>() { });

        Assert.assertEquals(List.of(5, 6), key7.get(0).range);
        Assert.assertEquals(List.of(7, 8), key7.get(1).range);
    }

    @Test
    public void autowiredClientSimpleValueReturnsListForWholeJsonArrayOverride() {
        ContextContainer.CONTEXT_CONTAINER.put("x-chenile-tenant-id", "raw-array-tenant");
        save(cconfig("raw-array", "ctest", "key8",
                "[{\"range\":[9,10]},{\"range\":[11,12]}]",
                "raw-array-tenant", null));

        Map<String, Object> value = cconfigClient.value("ctest", "key8");

        Assert.assertTrue(value.get("key8") instanceof List<?>);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> key8 = (List<Map<String, Object>>) value.get("key8");
        @SuppressWarnings("unchecked")
        List<Integer> firstRange = (List<Integer>) key8.get(0).get("range");
        @SuppressWarnings("unchecked")
        List<Integer> secondRange = (List<Integer>) key8.get(1).get("range");

        Assert.assertEquals(List.of(9, 10), firstRange);
        Assert.assertEquals(List.of(11, 12), secondRange);
    }

    private void save(Cconfig cconfig) {
        cconfigRepository.save(cconfig);
    }

    private static Cconfig cconfig(String id, String module, String key, String value,
                                   String customAttribute, String path) {
        Cconfig cconfig = new Cconfig();
        cconfig.id = id;
        cconfig.moduleName = module;
        cconfig.keyName = key;
        cconfig.avalue = value;
        cconfig.customAttribute = customAttribute;
        cconfig.path = path;
        return cconfig;
    }
}
