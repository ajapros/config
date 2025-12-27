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

## Module Nesting
As discussed above, all configurations belong to a module. Sub Modules are also supported. Sub modules
are named in the format m1.m2 Where m1 is the parent module and m2 is the child module. Modules can be 
arbitrarily nested. (i.e. you can have sub modules like m1.m2.m3.m4 etc.)

