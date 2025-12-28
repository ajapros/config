package org.chenile.cconfig.configuration.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.cconfig.model.Cconfig;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@ChenileController(value = "cconfigService", serviceName = "_cconfigService_",
		healthCheckerName = "cconfigHealthChecker")
public class CconfigController extends ControllerSupport{

    @GetMapping("/config/{module}")
    public ResponseEntity<GenericResponse<Map<String,Object>>> getAllKeys(
            HttpServletRequest httpServletRequest,
            @PathVariable("module") String module){
        return process(httpServletRequest,module);
    }

    @GetMapping("/config/{module}/{key}")
    public ResponseEntity<GenericResponse<Map<String,Object>>> value(
            HttpServletRequest httpServletRequest,
            @PathVariable("module") String module,
            @PathVariable("key") String key){
        return process(httpServletRequest,module,key);
    }

    @PostMapping("/cconfig")
    public ResponseEntity<GenericResponse<Cconfig>> save(
        HttpServletRequest httpServletRequest,
        @RequestBody Cconfig entity){
        return process(httpServletRequest,entity);
        }

    @GetMapping("/cconfig/{id}")
    public ResponseEntity<GenericResponse<Cconfig>> retrieve(
    HttpServletRequest httpServletRequest,
    @PathVariable("id") String id){
    return process(httpServletRequest,id);
    }
}
