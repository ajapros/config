Feature: Tests tenant-specific config file replacement.
  A tenant-specific JSON file should replace the base module JSON at the file-loading layer.
  Downstream retrievers may still apply additional overlays on top of the tenant file.

  Scenario: Get all keys for tenant-json. This tests tenant JSON file replacement of the base file
    When I construct a REST request with header "x-chenile-tenant-id" and value "tenant-json"
    And I GET a REST request to URL "/config/ctest"
    Then success is true
    And the REST response key "key1" is "value5"
    And the REST response key "tenantKey" is "tenant-value"
    And the REST response does not contain key "key2"

  Scenario: Get all keys for a tenant without a tenant file. This falls back to the default file
    When I construct a REST request with header "x-chenile-tenant-id" and value "missing-tenant"
    And I GET a REST request to URL "/config/ctest"
    Then success is true
    And the REST response key "key1" is "value5"
    And the REST response key "key2.abc" is "123"
    And the REST response does not contain key "tenantKey"

  Scenario: Get all keys without a tenant header. This falls back to the default file
    When I GET a REST request to URL "/config/ctest"
    Then success is true
    And the REST response key "key1" is "value5"
    And the REST response key "key2.abc" is "123"
    And the REST response does not contain key "tenantKey"
