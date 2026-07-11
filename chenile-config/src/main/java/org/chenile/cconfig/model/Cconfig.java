package org.chenile.cconfig.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.chenile.jpautils.entity.BaseJpaEntity;

/**
 * This entity allows modification to a config key 'k" in module m for a specific customAttribute
 * or for all customAttributes if the customAtttibute value is set to {@link #GLOBAL_CUSTOMIZATION_ATTRIBUTE}
 * The whole key can be mutated or part of the key - as specified in {@link #path} can be mutated.
 * The key can be mutated for all trajectories or for specific trajectories only. (if {@link #trajectoryId}
 * is set to {@link #GLOBAL_CUSTOMIZATION_ATTRIBUTE})
 */
@Entity
@Table(name = "cconfig")
public class Cconfig extends BaseJpaEntity  {
    /**
     * Value of customAttribute for everyone. This will work for all requests.
     */
    public final static String GLOBAL_CUSTOMIZATION_ATTRIBUTE = "__GLOBAL__";
    public String moduleName;
    public String keyName;
    public String avalue;
    /**
     * The attribute allows for overrides for specific requests. For example, in a SaaS application it is
     * possible to change the config only for specific tenants. custom Attribute is used to allow for
     * overrides on the basis of specific tenants etc.
     */
    public String customAttribute ;// = GLOBAL_CUSTOMIZATION_ATTRIBUTE;
    public String path;
    /**
     * For what trajectory id should this value be set? Make it
     * {@link #GLOBAL_CUSTOMIZATION_ATTRIBUTE}  to affect mainstream trajectory.
     */
    public String trajectoryId = GLOBAL_CUSTOMIZATION_ATTRIBUTE;
}
