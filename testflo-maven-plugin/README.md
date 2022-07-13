# testflo-maven-plugin

## `fetch testplan`

### Parameter

- id: `Jira Key of any testflo testplan` (f.e. ABC-123)
- file: `File to store testflo testplan` (f.e. testplan/ABC-123.json)

You may want to put the follwoing information in a property file

Property File
- property.file.root: `Root Folder where .property files were stored`

Credentials
- jira.username: `Jira Username`
- jira.password: `Jira Password` ([API Token](https://id.atlassian.com/manage-profile/security/api-tokens) in newer versions of jira)

Basic Information

Jira Settings
- jira.url: `Url of the used Jira Instance`

Fix Versions
- fixVersions: `Comma separated list of existing Fix Versions`

Labels
- labels: `Comma separated list of Jira Labels`

Custom Fields
- customFields: `Comma separated list of Custom Labels, that will be added as a Jira Label`

Keep Jira Label
- keepJiraLabel: `flag idicating, if the labels of a specific issue, should be kept.`

Extra Settings
- testflo.jira_rest_timeout: `socket timeout in seconds, that shall be used`
- testflo.jira_rest_skip_timeout: `flag, determines if socket timeout, shall be skipped` (needed if move to next iteration raises an error)


TestFLO Settings

(Value of first two properties below has to be replaced with a valid url. The rest maintain their values given)
- testflo.jira_rest_api: `Url of Jira REST API`
- testflo.jira_rest_tms: `Url of the Tile Map Service`
- testflo.authentication: `"Basic Authenticated Client"`
- testflo.media_type: `application/json`
- testflo.type.testplan: `Test Plan`
- testflo.type.testcase: `Test Case`
- testflo.type.teststep: `Test Step`
- testflo.testplan.status.open: `Open`
- testflo.testplan.status.inProgress: `In Progress_TF`
- testflo.testplan.status.acceptance: `Acceptance`
- testflo.testplan.status.closed: `Closed`
- testflo.testplan.transition.start.name: `Start`
- testflo.testplan.transition.start.id: `11`
- testflo.testplan.transition.acceptance.name: `Acceptance`
- testflo.testplan.transition.acceptance.id: `21`
- testflo.testplan.transition.accept.name: `Accept`
- testflo.testplan.transition.accept.id: `31`
- testflo.testplan.transition.retest.name: `Retest`
- testflo.testplan.transition.retest.id: `41`
- testflo.testplan.transition.stop.name: `Stop`
- testflo.testplan.transition.stop.id: `51`
- testflo.testplan.transition.close.name: `Close`
- testflo.testplan.transition.close.id: `71`
- testflo.testcase.status.open: `Open`
- testflo.testcase.status.inProgress: `In Progress_TF`
- testflo.testcase.status.pass: `Pass`
- testflo.testcase.status.fail: `Fail`
- testflo.testcase.status.retest: `Retest`
- testflo.testcase.transition.test.name: `Test`
- testflo.testcase.transition.test.id: `11`
- testflo.testcase.transition.pass.name: `Pass`
- testflo.testcase.transition.pass.id: `21`
- testflo.testcase.transition.fail.name: `Fail`
- testflo.testcase.transition.fail.id: `31`
- testflo.testcase.transition.retest.name: `Retest`
- testflo.testcase.transition.retest.id: `51`
- testflo.testcase.transition.inProgress.name: `In Progress`
- testflo.testcase.transition.inProgress.id: `61`
- testflo.testcase.transition.retestPass.name: `Pass`
- testflo.testcase.transition.retestPass.id: `71`
- testflo.testcase.transition.retestFail.name: `Fail`
- testflo.testcase.transition.retestFail.id: `81`
- testflo.testcase.transition.open.name: `Open`
- testflo.testcase.transition.open.id: `91`
- testflo.teststep.status.toDo: `To do`
- testflo.teststep.status.inProgress: `In Progress_TF`
- testflo.teststep.status.failed: `Failed`
- testflo.teststep.status.passed: `Passed`
- testflo.teststep.status.blocked: `Blocked`
- testflo.teststep.status.na: `N/A`

Execute
```bash
mvn de.simpleworks.staf:testflo-maven-plugin:fetchTestPlan \
-Did=`Jira Key of any testflo testplan` \
-Dfile=`File to store testflo testplan` \
-Dtestflo.configuration=file:`Root folder where testflo.properties file is stored` \
-Dproperty.file.root=`Root Folder where .property files were stored`
```

## `upload testresult`

### Parameter

- testplan: `Any testplan, that was fetched` (f.e. testplan/ABC-123.json)
- result: `test result file, or directory, that contains results from any test execution`

Property File
- property.file.root: `Root Folder where .property files were stored`

You may want to put the follwoing information in a property file

Credentials
- jira.username: `Jira Username`
- jira.password: `Jira Password` ([API Token](https://id.atlassian.com/manage-profile/security/api-tokens) in newer versions of jira)

Basic Information

Jira Settings
- jira.url: `Url of the used Jira Instance`

TestFLO Settings
- testflo.jira_rest_api: `Url of Jira REST API`
- testflo.jira_rest_tms: `Url of the Tile Map Service`

Optional
- screenshot.format= `Format of screenshot, default is png`

Execute
```bash
mvn de.simpleworks.staf:testflo-maven-plugin:uploadTestResult \
-Dresult=`Jira Key of any testflo testplan` \
-Dtestplan=`File to store testflo testplan` \
-Dproperty.file.root=`Root Folder where .property files were stored`
```

## `fetch API Request File`

### Parameter

- id: `Jira Key of any testflo test template` (f.e. ABC-123)
- file: `File to store testflo request json` (f.e. request/testflo.request.json)

You may want to put the follwoing information in a property file

Property File
- property.file.root: `Root Folder where .property files were stored`

Credentials
- jira.username: `Jira Username`
- jira.password: `Jira Password` ([API Token](https://id.atlassian.com/manage-profile/security/api-tokens) in newer versions of jira)

Basic Information

Jira Settings
- jira.url: `Url of the used Jira Instance`

TestFLO Settings
- testflo.jira_rest_api: `Url of Jira REST API`
- testflo.jira_rest_tms: `Url of the Tile Map Service`

Execute
```bash
mvn de.simpleworks.staf:testflo-maven-plugin:fetchAPIRequestFile \
-Did=`Jira Key of any testflo test template` \
-Dfile=`File to store testflo request json` \
-Dproperty.file.root=`Root Folder where .property files were stored`
```

Example Request File
```bash
{
    "classname": "de.simpleworks.staf.commons.api.APITeststep",
    "instance": {
      "name": "Any Description of a respecting test step",
      "order": 1,
      "request": {
        "method": "[GET|POST|PUT|DELETE]",
        "scheme": "[http|https]",
        "contentType": "UNKNOWN",
        "port": 443 | 8443,
        "host": "simpleworks.de",
        "urlPath": "/any/endpoint",
        "cookies": [
            {
                "name": "cookie name", 
                "value": "cookie value",
            }
        ],
        "body": "request body, can be obsolet, depending on the method",
        "queryParameters": [
            {
                "name": "cookie name", 
                "value": "cookie value",
            }
        ],
        "headers":  [
            {
                "name": "cookie name", 
                "value": "cookie value",
            }
        ]
      },
      "response": {
        "status": 200 | 204 | 404,
        "headers":  [
            {
                "name": "cookie name", 
                "value": "cookie value",
            }
        ]
        "body": "response body, can be obsolet, depending on the method",
        "contentType": "[application/json | text/html | ...]",
        "jsonBody":  "json representation of a response body, can be obsolet, depending on the request",
        "base64Body": "base64 representation of a response body, can be obsolet, depending on the request",
        "bodyFileName": "path to the file that contains the expected Http Response"
      },
      "assertions": [
          {
            "id": "return all instances, that are more expensive than 10",
            "validateMethod": "JSONPATH",
            "jsonpath": "$.store.book[?(@.price > 10)]",
            "allowedValue": "STRICT_ORDER",
            "value": "[Book Store#expensive books]"
          }
      ]
    }
  }
```



## TestFLO documentation

[REST API](https://deviniti.com/support/addon/server/testflo-87/latest/rest-api/)

[swagger](https://app.swaggerhub.com/apis-docs/DevinitiApps/testflo-public-api/)


> © Simpleworks GmbH, Philosophenweg 31, 47051 Duisburg