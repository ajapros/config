package org.chenile.cconfig.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.chenile.jpautils.entity.BaseJpaEntity;

/**
 * This entity allows modification to a config key -k  in module m for a specific customAttribute
 * or for all customAttributes if the customAtttibute value is set to {@link #GLOBAL_CUSTOMIZATION_ATTRIBUTE}
 * The whole key can be mutated or part of the key - as specified in {@link #path} can be mutated
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
    public String customAttribute;
    public String path;
}
