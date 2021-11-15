# xray-maven-plugin

## `fetch testplan`

### Parameter

- ids: `comma separated list Jira Keys of any xray testplans` (f.e. ABC-123,.,ABC-999)
- file: `File to store xray testplan` (f.e. testplan/ABC-123.json) 
    - if `ids` is a list, and `file` is a directory, then 
    every `id` will be stored to a file like Testplan-`id`.json, that will be stored to the respecting directory.


    - if `ids` is a list, and `file` is a file, then 
    every `file` will be overwritten only the last `id` will be stored to  `file`.

- environment `Defined environment in JIRA` (f.e. Integration)

Property File
- property.file.root: `Root Folder where .property files were stored`

You may want to put the follwoing information in a property file

Credentials
- xray.client_id: `Xray Client Id`
- xrayclient_secret: `Xray Client Secret`
- jira.username: `Jira Username`
- jira.password: `Jira Password`

Basic Information

Jira Settings
- jira.url= `Url of the used Jira Instance`

Xray Settings
- xray.url: https://xray.cloud.xpand-it.com
- xray.authenticate_url: https://xray.cloud.xpand-it.com/api/v1/authenticate
- xray.graphql_api_url: https://xray.cloud.xpand-it.com/api/v2/graphql


Execute
```bash
mvn de.simpleworks.staf:xray-maven-plugin:fetchTestPlan \
-Dids=`comma separated Jira Key of any xray testplan` \
-Dproperty.file.root=`Root Folder where .property files were stored`  \
-Dfile=`File to store xray testplan` \
-Dxray.client_id=`Xray Client Id` \
-Dxrayclient_secret=`Xray Client Secret` \
-Djira.username=`Jira Username` \
-Djira.password=`Jira Password` \
-Denvironment=`Test environment`
```

## `upload testresult`

### Parameter

- testplan: `Any testplan, that was fetched` (f.e. testplan/ABC-123.json)
- result: `test result file from any test execution`

Property File
- property.file.root: `Root Folder where .property files were stored`

You may want to put the follwoing information in a property file

Credentials
- xray.client_id: `Xray Client Id`
- xrayclient_secret: `Xray Client Secret`
- jira.username: `Jira Username`
- jira.password: `Jira Password`

Basic Information

Jira Settings
- jira.url= `Url of the used Jira Instance`

Xray Settings
- xray.url: https://xray.cloud.xpand-it.com
- xray.authenticate_url: https://xray.cloud.xpand-it.com/api/v1/authenticate
- xray.graphql_api_url: https://xray.cloud.xpand-it.com/api/v2/graphql

Execute
```bash
mvn de.simpleworks.staf:xray-maven-plugin:uploadTestResult \
-Dtestplan=`Jira Key of any xray testplan` \
-Dresult=results/demo/demo-result.json \
-Dproperty.file.root=`Root Folder where .property files were stored`  \
-Dxray.client_id=`Xray Client Id` \
-Dxrayclient_secret=`Xray Client Secret` \
-Djira.username=`Jira Username` \
-Djira.password=`Jira Password`
```

> © Simpleworks GmbH, Philosophenweg 31, 47051 Duisburg
