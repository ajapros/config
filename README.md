# Chenile Configurations
The Chenile Configurations utility simplifies modular configurations. Each module can define a bunch
of configurations in a JSON file that is named after the module. The values for configurations can be 
simple strings or complex JSONs. 

Chenile configuration allows the entire value to be over-ridden during runtime. This can happen for the 
entire value or selective parts of the value. For example, consider the following module JSON (ctest.json
defined in the test folder):
```json - ctest.json
{
  "key1": "value1",
  "key2": {
    "abc": "123",
    "fields": {
      "field1": {
        "range": [
          1,
          100
        ]
      }
    }
  }
}
```
This defines two keys - key1 and key2. 
Value of key1 is a simple string whilst the value of key2 is a complex JSON.

Chenile Config allows "key1" to be re-written during runtime. For key2, it is possible to override 
specific portions of the value such as "abc". 

The test cases demonstrate how it is possible to 
override specific "paths" in the value. In the test cases the following features are demonstrated:
1. It is possible to override key1 with a new value. This can be done for every request or for a specific
request with the correct customAttribute. (in this case tenant ID is the custom attribute)
2. It is possible to override key2.abc with a new value
3. It is possible to change the value of fields.field1.range[1] from 100 to 101
4. It is possible to add a new field - field2 with a range 2,200. Chenile supports the addition of 
an entire JSON snippet to the value.

## Custom Attribute
The custom attribute is used to identify the request and apply changes to the config for specific 
requests. Chenile Config allows a key to be modified for all requests or for specific requests using 
a "customAttribute". In a SaaS implementation, the customAttribute can be 
tenant ID. It can also be a combination of multiple request headers which can be considered as per
the specific requirements of an installation.

## Why Trajectory-Aware Resource Layering Was Added
The resource loader was extended to support trajectory-aware overrides because some installations need
resource overrides that vary not just by custom attribute such as tenant, but also by trajectory.

The concrete requirement was:

1. continue loading properties and JSON by module name as usual
2. allow trajectory-level overrides for classpath resources
3. allow the trajectory folder either directly under the base folder or under the custom-attribute folder
4. prefer the trajectory folder under the custom-attribute folder when both are present
5. return all matching resources, not just the single highest-priority one
6. merge those resources so higher-priority resources override lower-priority ones

## Resource Discovery And Priority
Classpath resource discovery is now layered rather than replacement-based.

For a resource such as `ctest.json` or `m1.properties`, Chenile now considers these locations:

1. `<basePath>/<resource>`
2. `<basePath>/<customAttribute>/<resource>`
3. `<basePath>/<trajectoryId>/<resource>`
4. `<basePath>/<customAttribute>/<trajectoryId>/<resource>`

The effective priority order is:

1. base resource
2. custom-attribute resource
3. base trajectory resource
4. custom-attribute trajectory resource

Both trajectory paths are considered when they exist. The custom-attribute trajectory resource is applied
after the base trajectory resource, so it has higher precedence without suppressing the lower-priority
trajectory file.

All matched resources are returned and processed in ascending priority order so that higher-priority
resources are applied later and therefore win on conflicts.

## JSON Merge Rules
JSON resources are no longer replacement-only.

Chenile now:

1. loads every matched JSON resource for the module
2. parses them in priority order
3. deep-merges object nodes
4. lets higher-priority values override lower-priority values

The merge decisions are:

1. if both values are JSON objects, merge recursively
2. if the higher-priority value is a scalar, replace the lower-priority value
3. if the higher-priority value is an array, replace the lower-priority value

So base JSON can establish defaults, tenant JSON can override part of the object, and a trajectory JSON
can override that further without forcing the entire module file to be duplicated.

## Properties Merge Rules
Properties resources are also layered.

Chenile now:

1. loads every matched `.properties` resource
2. converts each resource into `Cconfig` entries in priority order
3. allows later resources to override earlier ones using the existing key/path manipulation pipeline

This preserves the normal module-name filtering while allowing multiple resource layers to participate.
If both trajectory folders exist, both property files are loaded, with the custom-attribute trajectory
layer winning only where it explicitly overrides keys from the base trajectory layer.

## Cache Key
Resolved module values are cached by:

1. module
2. custom attribute
3. trajectory ID

This is necessary because the same module and custom attribute can now legitimately resolve to different
resource stacks for different trajectories.

## Module Nesting
As discussed above, all configurations belong to a module. Sub Modules are also supported. Sub modules
are named in the format m1.m2 Where m1 is the parent module and m2 is the child module. Modules can be 
arbitrarily nested. (i.e. you can have sub modules like m1.m2.m3.m4 etc.)

## CconfigClient Usage
`CconfigClient` can still be injected and used exactly as before:

```java
@Autowired
CconfigClient cconfigClient;
```

The existing contract remains unchanged:

```java
Map<String,Object> keyValue = cconfigClient.value("ctest", "key2");
Map<String,Object> allValues = cconfigClient.value("ctest", null);
```

Typed reads are also supported:

```java
MyPojo value = cconfigClient.value("ctest", "key2", MyPojo.class);
List<MyPojo> listValue = cconfigClient.value(
        "ctest",
        "key7",
        new TypeReference<List<MyPojo>>() { }
);
```

If the requested type does not match the stored config shape, the client fails fast with a configuration exception.

## JSON Object And Array Overrides From DB
DB overrides are stored as strings, but when they contain JSON they are parsed back into structured values.

- A JSON object string such as `{"a":1}` is returned as a `Map`
- A JSON array string such as `[{"a":1}]` is returned as a `List`

This applies both to:

1. typed reads using `Class<T>` or `TypeReference<T>`
2. plain `value(module, key)` reads, where the returned `Map<String,Object>` may contain a `Map` or `List` as the value

For example, if `key7` is overridden in the DB with a JSON array string:

```java
Map<String,Object> value = cconfigClient.value("ctest", "key7");
Object raw = value.get("key7"); // raw is a List, not a String
```

## Cache
Resolved module values are cached in memory. The default cache TTL is 5 minutes.

This can be overridden using:

```properties
chenile.config.cache-duration=PT5M
```

The value uses `Duration` format such as `PT30S`, `PT5M`, or `PT1H`.
