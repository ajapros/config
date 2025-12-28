package org.chenile.cconfig.configuration.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.chenile.base.response.GenericResponse;
import org.chenile.http.annotation.ChenileController;
import org.chenile.http.handler.ControllerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@ChenileController(value = "cconfigQueryService", serviceName = "_cconfigQueryService_",
		healthCheckerName = "cconfigHealthChecker")
public class CconfigQueryController extends ControllerSupport{

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

}
