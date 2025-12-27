package org.chenile.cconfig.bdd;

import cucumber.api.java.en.Then;
import org.chenile.cconfig.SpringTestConfig;
import org.chenile.cucumber.CukesContext;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import cucumber.api.java.en.Given;
import org.springframework.test.web.servlet.ResultActions;

import static org.chenile.cucumber.VariableHelper.substituteVariables;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


/**
    * This "steps" file's purpose is to hook up the SpringConfig to the test case.
    * It does not contain any methods currently but can be used for writing your own custom BDD steps
    * if required. In most cases people don't need additional steps since cucumber-utils provides for
    * most of the steps. <br/>
    * This class requires a dummy method to keep Cucumber from erring out. (Cucumber needs at least
    * one step in a steps file)<br/>
*/
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,classes = SpringTestConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("unittest")
public class CukesSteps {
	CukesContext context = CukesContext.CONTEXT;
	@Given("dummy") public void dummy(){}

	@Then("the value of {string} is {string}")
	public void the_value_of_is(String key, String value) throws Exception{
		ResultActions response = context.get("actions");
		response.andExpect(jsonPath(key).
				value(substituteVariables(value)));
	}
}
