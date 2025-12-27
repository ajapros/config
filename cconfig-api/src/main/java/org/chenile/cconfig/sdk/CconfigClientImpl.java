package org.chenile.cconfig.sdk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.chenile.base.exception.ConfigurationException;
import org.chenile.cconfig.sdk.cache.MemoryCache;
import org.chenile.cconfig.service.CconfigRetriever;
import org.chenile.core.context.ContextContainer;
import org.chenile.cconfig.model.Cconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CconfigClientImpl implements  CconfigClient{
    private static final Logger logger = LoggerFactory.getLogger(CconfigClientImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExpressionParser parser = new SpelExpressionParser();
    private final String configPath;
    private final CconfigRetriever cconfigRetriever;
    @Autowired
    MemoryCache memoryCache;
    /**
     * Caches module file in memory. Avoids repeated reads from the file system for the module file.
     */
    private final Map<String,String> moduleCache = new HashMap<>();
    @Autowired
    ContextContainer contextContainer;

    public CconfigClientImpl(String configPath,CconfigRetriever retriever) {
        this.configPath = configPath;
        this.cconfigRetriever = retriever;
    }

    private String module(String key){
        if (key == null) return null;
        int index = key.lastIndexOf('.');
        if (index == -1) return key;
        return key.substring(0,index);
    }

    private String key(String key){
        if (key == null) return null;
        int index = key.lastIndexOf('.');
        if (index == -1) return null;
        return key.toLowerCase().substring(index+1);
    }

    @Override
    public Object value(String k){
        String mod = module(k);
        String key = key(k);
        Map<String,Object> jsonMap  = allKeysForModule(mod,customizationAttribute());
        return (key == null || key.isEmpty())? jsonMap : jsonMap.get(key);
    }

    @SuppressWarnings("unchecked")
    private Map<String,Object> getJsonMapForModule(String module){
        String s = readModuleAsString(module);
        if (s == null)
            return new LinkedHashMap<>();
        return (Map<String,Object>)objectify(s);
    }

    /**
     * @param module name of the module
     * @return all the keys in the module. This includes all the keys in the JSON file (if found)
     * and also the DB keys. 
     */
    private Map<String,Object> allKeysForModule(String module,String customAttribute) {
        Map<String,Object> allKeys = memoryCache.findJsonMap(module,customAttribute);
        if (allKeys != null) {
            return allKeys;
        }
        allKeys = new LinkedHashMap<>();
        Map<String,Object> jsonMap = getJsonMapForModule(module);
        List<Cconfig> dbList = cconfigRetriever.findAllKeysForModule(module,customAttribute);
        if (dbList == null || dbList.isEmpty()){
            memoryCache.save(module,customAttribute,jsonMap);
            return jsonMap;
        }
        // Add all keys from dbList whose path is null to the JSON Map. They might have been defined for the
        // first time or perhaps they might have been redefined in the DB. For now, add only the keys that
        // are missing in the JSON.(i.e. keys defined for the first time in the DB and are not present in the
        // JSON files) The rest will be handled in the loop after this one.
        for (Cconfig cc1: dbList.stream().filter(cc->(cc.path == null || cc.path.isEmpty())).toList()){
            if (jsonMap.containsKey(cc1.keyName))
                continue;
            allKeys.put(cc1.keyName,objectify(cc1.avalue));
        }

        // For every key from JSON make sure that you override it if there are records available in DB list
        for (String key: jsonMap.keySet()){
            Object val = override(module,key,jsonMap.get(key),dbList);
            allKeys.put(key,val);
        }
        memoryCache.save(module,customAttribute,allKeys);
        return allKeys;
    }

    protected String customizationAttribute(){
        return contextContainer.getHeader("chenile-tenant-id");
    }

    private String readModuleAsString(String module) {
        String s = moduleCache.get(module);
        if (s != null) return s;
        String modPath = configPath + "/" + module.replaceAll("\\.","/") + ".json";
        logger.debug("Finding the JSON file for {}",modPath);

        try(InputStream stream = getClass().getClassLoader().getResourceAsStream(modPath)) {
            if(stream == null){
                return null;
            }
            s = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            moduleCache.put(module,s);
            return s;
        }catch(Exception e){
            throw new ConfigurationException(1700,"Cconfig:Cannot read module " + module,e);
        }
    }

    private Object override(String module, String key, Object value,List<Cconfig> dbList){
        List<Cconfig> configs = dbList.stream().filter(cc -> (cc.moduleName.equals(module) &&
                key.equals(cc.keyName))).toList();
        for (Cconfig cc: configs) {
            value = evaluate(key,cc.path,value,cc.avalue);
        }
        return value;
    }

    private String mapExpression(String p){
        String[] arr = p.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (String part: arr){
            sb.append("['").append(part).append("']");
        }
        return sb.toString();
    }

    private Object evaluate(String key, String path, Object value,String dbValue) {
        if (dbValue == null || dbValue.isEmpty())
            return value;
        // if path is not there then replace the existing value with the new value from the DB
        if (path == null || path.isEmpty() || path.equals(key) )
            return objectify(dbValue);

        Expression exp = parser.parseExpression(mapExpression(path));
        EvaluationContext context = new StandardEvaluationContext(value);
        logger.debug("Path of the expression in SPEL is {}. key = {}. dbvalue is {}",mapExpression(path),
                key, dbValue);

        exp.setValue(context,objectify(dbValue));
        return value;
    }

    /**
     * Parses the string as JSON if applicable and returns the parsed string as map
     * @param dbValue the value to objectify
     * @return the string or map
     */
    private Object objectify(String dbValue){
        dbValue = dbValue.trim();
        if (dbValue.startsWith("{") || dbValue.startsWith("[")){
            try {
                JsonNode node = objectMapper.readTree(dbValue);
                return objectMapper.convertValue(node,
                        new TypeReference<Map<String, Object>>(){});
            }catch(Exception ignore){
                // if this cannot be parsed as a JSON ignore it.
                return dbValue;
            }
        }
        return dbValue;
    }

}
