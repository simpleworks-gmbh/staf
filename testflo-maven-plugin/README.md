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

TestFLO Settings
- testflo.jira_rest_api: `Url of Jira REST API`
- testflo.jira_rest_tms: `Url of the Tile Map Service`

Execute
```bash
mvn de.simpleworks.staf:testflo-maven-plugin:fetchTestPlan \
-Did=`Jira Key of any testflo testplan` \
-Dfile=`File to store testflo testplan` \
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