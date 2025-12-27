package org.chenile.cconfig.model;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.chenile.jpautils.entity.BaseJpaEntity;


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
