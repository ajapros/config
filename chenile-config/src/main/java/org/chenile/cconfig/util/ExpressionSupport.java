package org.chenile.cconfig.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.cconfig.model.Cconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;
import java.util.Map;

/**
 * Use this class if you have a list of Cconfigs which need to manipulate an existing set of keys.
 */
public class ExpressionSupport {
    private static final Logger logger = LoggerFactory.getLogger(ExpressionSupport.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ExpressionParser parser = new SpelExpressionParser();

    public static void augmentKeys(Map<String,Object> keys, List<Cconfig> configList){
        addMissingKeys(keys,configList);
        overrideKeys(keys,configList);
    }
    /**
     * Add all keys from cconfigList whose path is null to the key list. They might have been defined for the
     * first time or perhaps they might have been redefined in the config retriever. Add only the keys that
     * are missing in the key list.(i.e. keys defined for the first time in the cconfig and are not present in the
     * key list)
     * @param keys - the existing set of keys that will be augmented.
     * @param cconfigList - the list of all cconfigs. This will manipulate the keys
     */
    private static void addMissingKeys(Map<String,Object> keys, List<Cconfig> cconfigList){
        for(Cconfig cc1: cconfigList.stream().filter(cc->(cc.path == null || cc.path.isEmpty())).toList()){
            if (keys.containsKey(cc1.keyName))
                continue;
            keys.put(cc1.keyName,objectify(cc1.avalue));
        }
    }

    /**
     * For every key from JSON make sure that you override it if there are records available in the cconfig
     * @param keys - the existing keys
     * @param configList - the cconfig list that will override these key values.
     */
    private static void overrideKeys(Map<String,Object> keys, List<Cconfig> configList){
        for (String key: keys.keySet()){
            Object val = override(key,keys.get(key),configList);
            keys.put(key,val);
        }
    }
    /**
     *
     * @param key the key
     * @param value the original value either from JSON or from the first record found with path null in DB
     * @param configList the list of all keys obtained for the module and the custom attribute.
     * @return the modified value after all the operations in dblist are applied against the key
     */
    private static Object override(String key, Object value,List<Cconfig> configList){
        List<Cconfig> configs = configList.stream().filter(cc -> (key.equals(cc.keyName))).toList();
        for (Cconfig cc: configs) {
            value = evaluate(key,cc.path,value,cc.avalue);
        }
        return value;
    }

    /**
     * Convert the path expression from form "a.b.c" to ['a']['b']['c'] as supported by SPEL for maps.
     * Note that even if expression contains indexes this handles it. Example: a.0.xyz becomes ['a']['0']['xyz']
     * @param p the path defined in the database
     * @return the expression in a form that is amenable to be used as a SPEL expression for maps
     */
    private static String mapExpression(String p){
        String[] arr = p.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (String part: arr){
            sb.append("['").append(part).append("']");
        }
        return sb.toString();
    }

    /**
     * @param key the key
     * @param path the path in the value that has been modified to a new value
     * @param value the original value that will be modified
     * @param dbValue the db value that will be applied against the original record
     * @return the modified value after SPEL constructs specified in the path are invoked on the original record
     */
    private static Object evaluate(String key, String path, Object value,String dbValue) {
        if (dbValue == null || dbValue.isEmpty())
            return value;
        // if path is not there then replace the existing value with the new value from the DB
        if (path == null || path.isEmpty() )
            return objectify(dbValue);

        Expression exp = parser.parseExpression(mapExpression(path));
        EvaluationContext context = new StandardEvaluationContext(value);
        logger.debug("Path of the expression in SPEL is {}. key = {}. dbvalue is {}",mapExpression(path),
                key, dbValue);

        exp.setValue(context,objectify(dbValue));
        return value;
    }

    /**
     * Parses the string as JSON if applicable and returns the parsed JSON object/list.
     * @param dbValue the value to objectify
     * @return the string, map or list
     */
    private static Object objectify(String dbValue){
        dbValue = dbValue.trim();
        if (dbValue.startsWith("{") || dbValue.startsWith("[")){
            try {
                JsonNode node = objectMapper.readTree(dbValue);
                return objectMapper.convertValue(node, Object.class);
            }catch(Exception ignore){
                // if this cannot be parsed as a JSON ignore it.
                return dbValue;
            }
        }
        return dbValue;
    }
}
