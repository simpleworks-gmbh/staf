# surefire-maven-plugin

## `run testplan`

### Parameter

- file: `File to any testplan` (f.e. testplan/ABC-123.json)

Property File
- property.file.root: `Root Folder where .property files were stored`

You may want to put any test related configuration in a property file

Execute
```bash
mvn de.simpleworks.staf:surefire-maven-plugin:test \
-Dproperty.file.root=`Root Folder where .property files were stored`  \
-Dfile=`File to any testplan`
```

You may want to change the `reporter.manager.override.report` property to `false`. 

If not the respecting "results file" will be overwritte with the results 
of the last testcase.

> © Simpleworks GmbH, Philosophenweg 31, 47051 Duisburg