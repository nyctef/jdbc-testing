This repo is intended to be a minimal playground to verify how certain jdbc drivers work.

The goal is to figure out edge cases around jdbc url parsing and escaping which aren't necessarily listed in the official documentation.

### Getting started

The following tools are recommended:

- vscode with the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
- [Maven](https://maven.apache.org/download.cgi) (extract to disk in a known place)  
  Once extracted, you'll need to set the "Maven: executable path" setting in vscode to the path of the `bin/mvn` executable (`bin/mvn.cmd` for windows)

Once the java extensions are installed and set up, you should see a "MAVEN" section in the vscode sidebar. By right-clicking on the project in this sidebar, you should be able to run all tests in one go. Alternatively, you can run individual tests by clicking the play button in the editor gutter.


### Adding more jdbc drivers

Drivers are specified as dependencies in the `pom.xml` file. You'll need to find the correct `groupId`/`artifactId`/`version` values - [the flyway `pom.xml`](https://github.com/flyway/flyway/blob/main/pom.xml) may be a useful reference.

Once a new `<dependency>` element has been added to the pom, run the maven `install` target to check that it can successfully download the new package.