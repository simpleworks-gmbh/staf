# xray-maven-plugin

## `fetch testplan`

### Parameter

- id: `Jira Key of any xray testplan` (f.e. ABC-123)
- file: `File to store xray testplan` (f.e. testplan/ABC-123.json)
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
-Did=`Jira Key of any xray testplan` \
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
