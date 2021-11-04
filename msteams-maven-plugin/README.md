# msteams-maven-plugin

## `notify testresult`

### Description
Publishes the contents of any test result 
to any Microsoft Teams Channel. 

The Message Card contains a link to the current test execution 
of an `Xray-Test`.

### Parameter

- result: `test result file from any test execution`
- testplan: `Any testplan, that was fetched` (f.e. testplan/ABC-123.json)
- template: `File that describes a MessageCard - `[Template](https://docs.microsoft.com/en-us/outlook/actionable-messages/message-card-reference) 

Property File
- property.file.root: `Root Folder where .property files were stored`

You may want to put the follwoing information in a property file

Basic Information

Jira Settings
- jira.url= `Url of the used Jira Instance`

Webhook
- teams.webhook=`webhook for microsoft teams`

Execute
```bash
mvn de.simpleworks.staf:msteams-maven-plugin:notifyTestResult \
-Dresult=`test result file from any test execution` \
-Dtestplan=`Any testplan, that was fetched` \
-template=`File that describes a MessageCard Template`
-Dproperty.file.root=`Root Folder where .property files were stored`  \
```
> © Simpleworks GmbH, Philosophenweg 31, 47051 Duisburg
