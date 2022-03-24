# staf

## `Introduction`
The *Simpleworks Test Automation Framework* `staf` is an open source test-automation framework, 
and toolkit, that aims to ease the automation of "GUI, API and Database" - related testcases.

**Please Note**, this product is distributed in the hope that it will be useful, but 
without any *warranty*.

if you find any problem, or want to suggest a feature, please raise an [issue](https://github.com/simpleworks-gmbh/staf/issues/new/choose).

For any "business" - related inquiry, please contact us, on our [company homepage](https://www.simpleworks.de/).

## `Usage`

### `Workflow`
A typical *Test Automation* workflow should contain the 
following. 

`Fetch Testplan`: fetching a collection of relevant tests.
It's expected, that any "non technical"- role is in charge of the creation 
and management of tests. 

*staf* supports some Atlassian Jira - plugins, like [TestFLO](https://deviniti.com/products/testflo/en/) 
and [Xray](https://www.getxray.app/), to *fetch* its respecting *Testplan* entities. 
		  

`Test Execution`: the execution of any *Testplan*.

*staf* has extended the well known [maven-surefire-plugin](https://maven.apache.org/surefire/maven-surefire-plugin/), 
to handle the *Test Execution".

`Safe Test Results`: the persiting of artefacts, that reflect the results of any test execution.

*staf* can upload the test results, to the same Atlassian Jira - plugins, that have been used to 
fetch the testplans.


It's recommended to use this phases in *ci/cd* pipelines as well.
Take a look in the *README* Files, of the respecting *staf plugins*, to learn how to 
use them. 


### `Implementation`

### `Testcases`
Testcases need to have the following Annotations:

-`Testcase` that has any *non emtpy* value that identifies the testcase uniquely.
It is recommended, to use the "Jira Issue Id"

-`RunWith` needs to be suited with a "STAF Test Runner".
Until now there is only `STAFBlockJUnit4ClassRunner.class`

Testcases need to extend any implementation of the 
[TestCase](https://github.com/simpleworks-gmbh/staf/blob/main/framework/src/main/java/de/simpleworks/staf/framework/elements/commons/TestCase.java) class.

### `Test Methods`
The methods in the testclasses reflect the steps, 
of the respecting `test`. 

The methods need to have the [Step](https://github.com/simpleworks-gmbh/staf/blob/main/commons/src/main/java/de/simpleworks/staf/commons/annotation/Step.java) - Annotation , so the framework will notice them.

The `description` property needs to match the description of the 
test step (of the respecting Jira Plugin)

The `order` property needs to match the order of the test step (of the respecting Jira Plugin)
The first step has the order 1

**NOTE** if you use the test runner `STAFBlockJUnit4ClassRunner.class`, then 
one also needs to add the Test-Annotation. 

### `Basis Testcase Implementations`  
`GUITestCase`: used for "Browser based" UI Tests.
These ones, make heavy use of [Selenium](https://www.selenium.dev/documentation/)

## `GUITestCase`
```bash
@Testcase(id = "TESTCLASS_ID")
@RunWith(STAFBlockJUnit4ClassRunner.class)
public class TC_GUITestCase extends GUITestCase {

	@Test
	@Step(description = "execute gui test ", order = 1)
	public void step1() throws Exception {
		// do testing stuff :)  
	}

	@Override
	public List<RewriteUrlObject> getRewriteUrls() {
		return new ArrayList<>();
	}
}
```
### `Components`

`GUITestCase`s are based on the Page Object Model (POM)
Every Page on the application is represented by any "Page Object" 
that is represented by a class, that extends the `PageObject`

```bash
public class AnyPage extends PageObject {

	@FindBy(xpath = "//any//xpath")
        // STAFButton - WebElement thatis provided by STAF
	private STAFButton button;

	@Inject
        // Creation and Destruction of the WebDriver is handled by STAF 
	protected AnyPage(final WebDriver driver) {
		super(driver);
	}

	/**
	 * @brief actions on WebElemens
	 */
	public void clicButton() {
		button.click();
	}
}
```
This in an example of a `CromeWebDriverManager`- Implementation 
that is used by Selenium, to set up a webdriver. 

Add the fully qualified path, to the `webdriver.manager.class`- property.
```bash
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import de.simpleworks.staf.framework.gui.webdriver.module.WebDriverManagerImpl;

public class CromeWebDriverManager extends WebDriverManagerImpl {

   @Override
   protected final WebDriver createDriver() {

	final ChromeOptions chromeOptions = new ChromeOptions();
	chromeOptions.addArguments("--no-sandbox"); // Bypass OS security model
	chromeOptions.addArguments("--remote-debugging-port=9222"); // workaround to create "DevToolsActivePort file"
	chromeOptions.addArguments("--disable-dev-shm-usage");

	if (isHeadless()) {
	  chromeOptions.setHeadless(true);
	  chromeOptions.addArguments("--window-size=1920,1080");
	} 
	else {
		chromeOptions.addArguments("start-maximized");
	}

	return new ChromeDriver(chromeOptions);	
   }
}
```
`APITestCase` and `DbTestCase`, follow an other concept then 
the `GUITestCase`.

These ones follow a `Less Code` concept. 
This means, one does not need to implement the 'Step annotated' methods.

The implementation, happens in the "Teststep JSONs", that need to be set 
in the constructor. 

### `Teststep`

The "Teststep JSONs", are simply JSON Files, that will be deserialized to 
"Teststep Instances", that reflect the respecting test step.

Teststeps need to implement the interface [ITeststep](https://github.com/simpleworks-gmbh/staf/blob/main/commons/src/main/java/de/simpleworks/staf/commons/interfaces/ITeststep.java)

The Teststeps, can be validated through `Assertions`. 
Assertions validate the responses of Testcases. 

Assertions that are not met, will `fail` the test execution. 
The different testcases, support only its subset of 
validation methods.

Variables in the "Teststep JSONs" need to be set like that

`<&`**Storage Name**#**Variable Name**`&>`

Variables can be set with one of these procedures: 

- `Set in the testcase`: put a field, named like the **Variable Name** in the testcase.
One needs to add the annotation [Property](https://github.com/simpleworks-gmbh/staf/blob/main/commons/src/main/java/de/simpleworks/staf/commons/annotation/Property.java) on the field. 

The value of the variable, will be set through a property. 

The respecting property, needs to be set in the JVM (-D argument)
or during a property file gets read. 

**Storage Name**, differs for any of the "Testcase implementation"

- `Set in the assertions`: assertions, that have been *validated successfully*, store 
its validation result. 

The **Storage Name** equals the step name.
The **Variable Name** equals the one in the id field of the assertion.

`APITestCase`: used for "API" Tests.
`Storage Name`, is APITestCase. 

The following validateMethods are supported: 

## `APITestCase`
```bash
@Testcase(id = "TESTCLASS_ID")
@RunWith(STAFBlockJUnit4ClassRunner.class)
public class TC_APITestCase extends APITestCase  {

	@Property(value = "api_test.rest-environment")
	private String Host; 
	
	@Property(value = "api_test.rest-version")
	private String Version; 
	
	@Property(value = "user.email")
	private String Email; 
	
	@Property(value = "user.password")
	private String Password; 

	public TC_APITestCase() throws Exception {
		super("classpath:requests/.../request.json");
	}

	@Test
	@Step(description = "execute api test ", order = 1)
	public void step1() {   
		// no implementation neccessary
	}

	@Override
	public List<RewriteUrlObject> getRewriteUrls() {
		return new ArrayList<>();
	}
}
```
### `request.json`
```bash
[
     {
		
	    "classname": "de.simpleworks.staf.commons.api.APITeststep",
	    "instance": {
          	  "order": 1,
           	  "name": "Login",
            	  "request": {
                	"host": "<&APITestCase#Host&>",
                	"urlPath": "<&APITestCase#Version&>/login",
                	"scheme": "https",  
                	"method": "POST",
                	"contentType": "JSON",
                	"body": 
			   "{\"email\": \"<&APITestCase#Email&>\",
			     \"password\": \"<&APITestCase#Password&>\"}"
            	  }, 
            	  "response":{
                	"contentType": "JSON",
                	"status": 204
            	  }
	   }
      }
]
```
`DbTestCase`: used for "Database" Tests.
`Storage Name`, is DbTestCase. 

The following validateMethods are supported: 

## `DbTestCase`

```bash
@Testcase(id = "TESTCLASS_ID")
@RunWith(STAFBlockJUnit4ClassRunner.class)
public class TC_DbTestCase extends DbTestCase  {

	@Property(value = "db_test.table")
	private String Table;

	public TC_DbTestCase() throws Exception {
		super("classpath:database/select.json", 
		// adding Database driver, f.E. adding the `Postgres DB Driver`
		new PostgresqlConnectionManagerModule());
	}

	@Test
	@Step(description = "Step 1", order = 1)
	public void step1() {
		// do testing stuff :)
	}

		@Override
	public List<RewriteUrlObject> getRewriteUrls() {
		return new ArrayList<>();
	}
}
```
### `select.json`
```bash
[
	{
		"classname": "de.simpleworks.staf.commons.database.DbTeststep",
		"instance": {
			"order": 1,
			"name": "Step 1", 
			"statement": {
				"type": "SELECT",
				"expression": "SELECT name, population FROM <&DbTestCase#Table&> WHERE id = 6;",
				"connectionId": "world-db",
				"expectedRows": 1
			},
			"assertions": [{
                  	  "id": "assert001",
                    	  "validateMethod": "DB_RESULT",
                    	  "allowedValue": "NON_EMPTY", 
			  "value": "population"
                	},
			{
                    	  "id": "assert002",
                    	  "validateMethod": "DB_RESULT",
                    	  "allowedValue": "EXACT_VALUE", 
			  "attribute": "name",
			  "value": "any city"
                	}]
		}
	}
]
```
### Note:
`type` in `statement` has two values to choose from depending on the type of SQL statement:

- "SELECT" is for only SELECT sql statements like in the `select.json` above.

- "QUERY" is for all other types of sql statements (e.g. INSERT, UPDATE, DELETE etc.).

if you omit `expectedRows`, the number of result rows won't be checked.
One can use it, if the number of rows is **unknown**.

Keep in mind, that the assertions will checked on every row of the result set, but only the values "of the last row" will be stored.

### `dbconfig.json`
```bash
[
	{
		"classname": "de.simpleworks.staf.commons.database.connection.DbConnection",
		"instance": {
			"id": "world-db",
			"connectionString": "jdbc:postgresql://localhost:5432/world-db",
			"driver": "org.postgresql.Driver",
			"username": "world",
			"password": "world123"
		}
	}
]
```
 

## `properties`

All files, ending with `.properties` will be read as properties. 

The argument `property.file.root` describes the root directory, where all files 
ending with `.properties` will be searched in all (sub-) directories, and loaded.

One can also put several directories in a "comma separated list"

### `available properties`
#
- `testcase.header_name`: (String) value needed to identify Testcase Exections, *Default value* is  `TestcaseExecution`

- `testcase.create_artefact`: (String) value of any CreateArtefactEnum *Default value* is `ON_FAILURE`

- `teststep.header_name`: (String) value, will be used as header name, for TestStep Execution *Default value* is `X-REQUEST-ID`

- `httpclient.cookie_policy`: (String) value, name of any java.net.CookiePolicy implementation *Default value* is `ACCEPT_ALL`

- `httpclient.logging_level`: (String) value, name of any okhttp3.logging.HttpLoggingInterceptor.Level *Default value* is `BODY`

- `httpclient.ignore_certificate`: (Boolean) value, determines if (untrusted SSL) certificates should be ignored *Default value* is `true`

- `proxy.enabled`: (Boolean) value, determines if the (HTTP) Requests will be proxied *Default value* is `false`

- `proxy.gui.port`: (Integer) value, port of the proxy (server), responsible to proxy the requests from the webdriver *Default value* is `8888`

- `proxy.api.port`: (Integer) value, port of the proxy (server), responsible to proxy the requests from the httpclient *Default value* is `9999`

- `proxy.headers`: (String) list of ";" seperated list of "Key:Value"-Pairs

- `webdriver.screenshot`: (Boolean) value, determines if screenshots will be taken for step report artefacts *Default value* is `false`

- `webdriver.headless`: (Boolean) value, determines if the respecting webdriver is exectued in headless mode *Default value* is `false`

- `webdriver.manager.class`: (String) value, fully qualified path name to any WebDriverManager Class. It needs to implement WebDriverManagerImpl *Mandatory* if `GUI Testcases are used`

- `reporter.manager.report.directory`: (String) value, name of the diretory, that will store the result files` *Default value* is `results Directoy will be created, if it does not exist

- `reporter.manager.report.name`: (String) value, name of the test report *Default value* is `result.json`

- `reporter.manager.override.report`: (Boolean) value, determines if the result file will be appended (false) or overwritten (true) *Default value* is `true`

- `gui.timeout`: (Integer) value, determines how long the html will be pulled, until the "Timeout Exception" is thrown *Default value* is `60`

- `api.timeout`: (Integer) value, determines the connection timeout (connect, read, write) of the http client  *Default value* is `1`

- `retry.connection`: (Boolean) value, determines if the connection should be established again, if an error happens *Default value* is `true`

- `database.connection_pool.config_file`: (String) value, the file that contains the *Database Connection - Configuration Files*

- `value.substitution.regex`: (String) value, regular expression to determine the value substitution 'Default value' is `(?<=\\<&)(.*?)(?=\\&>)`

- `function.substitution.regex`: (String) value, regular expression to determine the function substitution 'Default value' is `([\\\\a-z._,$:*{}\";|@A-Z0-9ÄÜÖäöü\\/\\p{javaSpaceChar}-]+)`

- `argument.substitution.regex`: (String) value, regular expression to determine the (function) argument substitution 'Default value' is `([\\\\a-z._,$:*{}\";@A-Z0-9ÄÜÖäöü\\/\\p{javaSpaceChar}-]+)`

- `function.template.regex`: (String) value, regular expression to determine the (function) template 'Default value' is `FUNCTION#(.+)#FUNCTION`


> © Simpleworks GmbH, Philosophenweg 31, 47051 Duisburg
