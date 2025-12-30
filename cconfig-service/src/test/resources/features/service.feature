Feature: Tests the Chenile Config Service using a REST client.
  Chenile Config service can accept new keys to be added or an existing key's value
  to be modified in either its entirety or parts of it.
  Chenile Config must be able to store the incremental updates for keys in a DB.
  It must be able to retrieve the key from the DB and add incremental updates to it from the DB records.
  It must return the value for one key or all keys in a JSON. The returned value will be JSON with
  keys as first level elements and the values (either a string or an entire sub JSON) in the second element.
  We will test both the /cconfig and /config URLs.
  /cconfig (POST) allows us to add new DB records for keys in a module
  /cconfig (GET) allows us to retrieve the DB record that has been stored by the UUID
  /config/module retrieves all the keys and values for a module
  /config/module/key retrieves the value of a specific key that belongs to a module.

  Scenario: Save the overriding cconfig for a string key for tenant0.
    When I POST a REST request to URL "/cconfig" with payload
    """JSON
    {
      "moduleName": "ctest",
      "keyName": "key1",
      "avalue": "value2",
      "customAttribute": "tenant0"
	}
	"""
    Then success is true
    And store "$.payload.id" from response to "id"
    # And the REST response key "tenant" is "${tenant}"
    # And the REST response key "createdBy" is "${employee}"

  Scenario: Save the overriding cconfig for a string key for __GLOBAL__.
    When I POST a REST request to URL "/cconfig" with payload
    """JSON
    {
      "moduleName": "ctest",
      "keyName": "key1",
      "avalue": "value5",
      "customAttribute": "__GLOBAL__"
	}
	"""
    Then success is true
    And store "$.payload.id" from response to "id"

  Scenario: Save the overriding cconfig for a string key for tenant2.
    When I POST a REST request to URL "/cconfig" with payload
    """JSON
    {
      "moduleName": "ctest",
      "keyName": "key1",
      "avalue": "value25",
      "customAttribute": "tenant2"
	}
	"""
    Then success is true
    And store "$.payload.id" from response to "id"

  Scenario: Save the overriding cconfig for a JSON key. This alters a specific portion of the key
    When I POST a REST request to URL "/cconfig" with payload
    """JSON
    {
      "moduleName": "ctest",
      "keyName": "key2",
      "avalue": "456",
      "path": "abc",
      "customAttribute": "tenant0"
	}
	"""
    Then success is true
    And store "$.payload.id" from response to "id"

  Scenario: Save the overriding cconfig for a JSON key. This alters a specific portion of the key.
    In this case the portion changed is deep within the key. (Specific array index is altered)
    When I POST a REST request to URL "/cconfig" with payload
    """JSON
    {
      "moduleName": "ctest",
      "keyName": "key2",
      "avalue": "101",
      "path": "fields.field1.range.1",
      "customAttribute": "tenant0"
	}
	"""
    Then success is true
    And store "$.payload.id" from response to "id"

  Scenario: Save the overriding cconfig for a JSON key. This alters a specific portion of the key.
  In this case we add a new field. We will add an entire JSON field and expect this to be in incorporated
    When I POST a REST request to URL "/cconfig" with payload
    """JSON
    {
      "moduleName": "ctest",
      "keyName": "key2",
      "avalue": "{\"range\": [2,200]}",
      "path": "fields.field2",
      "customAttribute": "__GLOBAL__"
	}
	"""
    Then success is true
    And store "$.payload.id" from response to "id"

  Scenario: Save the overriding cconfig for a JSON key. This adds new JSON nodes to the key key2
    When I POST a REST request to URL "/cconfig" with payload
    """JSON
    {
      "moduleName": "ctest",
      "keyName": "key2",
      "avalue": "777",
      "path": "def",
      "customAttribute": "tenant0"
	}
	"""
    Then success is true
    And store "$.payload.id" from response to "id"

  Scenario: Save the overriding cconfig for a JSON key in another module.
    When I POST a REST request to URL "/cconfig" with payload
    """JSON
    {
      "moduleName": "ctest1.ctest1",
      "keyName": "key3",
      "avalue": "some_other_value",
      "path": "some_name",
      "customAttribute": "tenant0"
	}
	"""
    Then success is true
    And store "$.payload.id" from response to "id"

  Scenario: Retrieve the saved cconfig
    When I GET a REST request to URL "/cconfig/${id}"
    Then success is true
    And the REST response key "id" is "${id}"

  Scenario: Save a cconfig using an ID that already is determined
    Given that "id" equals "123"
    And I POST a REST request to URL "/cconfig" with payload
  """json
  {
    "id": "${id}",
    "keyName": "key3",
    "avalue": "456",
    "path": "key2.abc",
    "customAttribute": "tenant0"
  }
  """
    Then success is true
    And the REST response key "id" is "${id}"

  Scenario: Retrieve the saved cconfig
    When I GET a REST request to URL "/cconfig/${id}"
    Then success is true
    And the REST response key "id" is "${id}"

  Scenario: Get the value for "key1" for tenant0. This tests simple values being set
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I GET a REST request to URL "/config/ctest/key1"
    Then success is true
    And the REST response key "key1" is "value2"

  Scenario: Get the value for "key1" for tenant2. This tests simple values being set
    and over-ridden at different tenant levels.
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant2"
    And I GET a REST request to URL "/config/ctest/key1"
    Then success is true
    And the REST response key "key1" is "value25"

  Scenario: Get the value for "key2". This tests setting complex JSONs.
    This tests non-conflicting updates performed for __GLOBAL__ and tenant0.
    Both the updates must be reflected for tenant0
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I GET a REST request to URL "/config/ctest/key2"
    Then success is true
    And the REST response key "key2.abc" is "456"
    And the REST response key "key2.def" is "777"
    And the REST response key "key2.fields.field1.range[0]" is "1"
    And the REST response key "key2.fields.field1.range[1]" is "101"
    And the REST response key "key2.fields.field2.range[0]" is "2"
    And the REST response key "key2.fields.field2.range[1]" is "200"

  Scenario: Get the value for "key2" for tenant1. This tests updates performed for __GLOBAL__
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant1"
    And I GET a REST request to URL "/config/ctest/key2"
    Then success is true
    And the REST response key "key2.abc" is "123"
    And the REST response does not contain key "key2.def"
    And the REST response key "key2.fields.field1.range[0]" is "1"
    And the REST response key "key2.fields.field1.range[1]" is "100"
    And the REST response key "key2.fields.field2.range[0]" is "2"
    And the REST response key "key2.fields.field2.range[1]" is "200"

  Scenario: Get the value for "key3" - this tests sub modules.
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I GET a REST request to URL "/config/ctest1.ctest1/key3"
    Then success is true
    And the REST response key "key3.some_name" is "some_other_value"

  Scenario: Get the value for "key1" for non tenant0. This tests __GLOBAL__ for value replacement at the DB level
    When I GET a REST request to URL "/config/ctest/key1"
    Then success is true
    And the REST response key "key1" is "value5"

  Scenario: Get all the keys and values for module ctest for tenant0. This checks if all keys are being returned.
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I GET a REST request to URL "/config/ctest"
    Then success is true
    And the REST response key "key1" is "value2"
    And the REST response key "key2.abc" is "456"
    And the REST response key "key2.def" is "777"
    And the REST response key "key2.fields.field1.range[0]" is "1"
    And the REST response key "key2.fields.field1.range[1]" is "101"
    And the REST response key "key2.fields.field2.range[0]" is "2"
    And the REST response key "key2.fields.field2.range[1]" is "200"
    And the REST response key "key4" is "value4"
    And the REST response key "key5" is "value5"

  Scenario: Get the value for "key4" tenant0. This tests introducing new keys in the DB for tenant0
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant0"
    And I GET a REST request to URL "/config/ctest/key4"
    Then success is true
    And the REST response key "key4" is "value4"

  Scenario: Get the value for "key5" non tenant0. This tests introducing new keys in the DB for __GLOBAL__
    When I GET a REST request to URL "/config/ctest/key5"
    Then success is true
    And the REST response key "key5" is "value5"

  Scenario: Get the value for "key5" tenant8. This tests introducing new keys in the DB for __GLOBAL__
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant8"
    And I GET a REST request to URL "/config/ctest/key5"
    Then success is true
    And the REST response key "key5" is "value5"

  Scenario: Get the value for "key20" in module "ctest20".
  This tests introducing new module in DB without corresponding JSON
    When I GET a REST request to URL "/config/ctest20/key20"
    Then success is true
    And the REST response key "key20" is "value20"

  Scenario: Get the value for a non existent module
    When  I GET a REST request to URL "/config/ctest29"
    Then success is true

  Scenario: Get the value for a non existent module and key
    When I GET a REST request to URL "/config/ctest29/key29"
    Then success is true