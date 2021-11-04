# stafutils-maven-plugin

## `generate API Testclasses`

### Description
Utility that creates Testclass "Java Files" from `Request Json Files`

### Parameter
- testclassDirectory: `Directory, where the test classes should be stored to`
- requestFileDirectory: `Path where thre "Request Json Files" are stored`  
- packageName: `packageName that the created test classes should have`

Property File
- property.file.root: `Root Folder where .property files were stored`

You may want to put the follwoing information in a property file

Testclass Settings
- testclass-generator.testcase-file=`Template that contains any testcase`
- testclass-generator.methods-file=`Template that contains the step method`

Example Template Files: 

`testcase-file`
```bash
// PACKAGE_NAME will be substituted to the name of the respecting packageName of the parameter
package ${PACKAGE_NAME};

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.simpleworks.staf.commons.annotation.Step;
import de.simpleworks.staf.commons.annotation.Testcase;
import de.simpleworks.staf.framework.elements.api.APITestCase;
import de.simpleworks.staf.framework.elements.api.RewriteUrlObject;
import de.simpleworks.staf.module.junit4.STAFBlockJUnit4ClassRunner;

@Testcase(id = "TESTCLASS_ID")
@RunWith(STAFBlockJUnit4ClassRunner.class)
public class ${TESTCLASS_NAME} extends APITestCase  {

	// TESTCLASS_NAME will be substituted to the name of the respecting "Request Json File"
	public  ${TESTCLASS_NAME}() throws Exception {
		super("classpath:requests/.../${TESTCLASS_NAME}.json");
	}

	// METHODS will be substituted with substituted "methods-files" 
	${METHODS}

	@Override
	public List<RewriteUrlObject> getRewriteUrls() {
		return new ArrayList<>();
	}
}
```

`methods-file`
```bash
    // STEP_NAME will be substituted to the name of the respecting test step
    // STEP_ORDER will be substituted to the order of the respecting test step
	@Test
	@Step(description = "${STEP_NAME}", order = ${STEP_ORDER})
	public void step${STEP_ORDER}() {   
		// do testing stuff :)   
	}
```

```bash
mvn de.simpleworks.staf:staf-utils-maven-plugin:generateAPITestclasses \
-DrequestFileDirectory=`Jira Key of any xray testplan` \
-DtestclassDirectory=`Path where the artefacts will be stores (will be created if on exist)` \
-DpackageName=`Package Name of the created test scripts` \ 
-Dproperty.file.root=dev/properties
```

## `extract Artefacts`

### Description
Utility that safes the artefacts from `Test Result Files` (as Base64 String) to a File

Artefacts will be stored in a structure like that: 

    
    
    targetDirectory
    |
    |-> Test Id
            |
            |-> Step Description
                    |
                    |-> Time Stamp
                            |
                            |-> Artefact File

### Parameter

- result: `Jira Key of any xray testplan` (f.e. ABC-123)
- targetDirectory: `Path where the artefacts will be stores (will be created if one exist)` 

Execute
```bash
mvn de.simpleworks.staf:staf-utils-maven-plugin:extractArtefacts \
-Dresult=`Jira Key of any xray testplan` \
-DtargetDirectory=`Path where the artefacts will be stores (will be created if one exist)`
```
> © Simpleworks GmbH, Philosophenweg 31, 47051 Duisburg
