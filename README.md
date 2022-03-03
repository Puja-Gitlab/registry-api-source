# registry-api-source

WSO2 registry rest APIs enable developers to access or do CRUD operation on registry resources present in wso2 registry. This source code has the logic of registry REST API implementation.

## Table of contents

- [Download and build webapp](#download-and-build)
- [Running webapp](#running-sample-applications)

## Download and build

### Prerequisites

* [Maven](https://maven.apache.org/download.cgi)
* [Java](http://www.oracle.com/technetwork/java/javase/downloads)

### Building from source

1. Get a clone or download source of [WSO2 sample-is repository](https://github.com/Puja-Gitlab/wso2-resgistry-restAPI).We will refer this root(master) directory as `<wso2-registry>` here onwards.
2. Run the Maven command `mvn clean install` from the `<wso2-registry> root folder` directory.You can find built .war file in `target` directory of `<wso2-registry>/target`.

## Running sample applications
 
In order to run the war file , please follow these steps 
 
1. Start the WSO2 APIM server. 
2. Deploy the war file to <APIM_HOME>/repository/deployments/server/webapps.
3. Access WSO2 APIM management console and add a sample registry resource file.
5. Try out registry REST API behavior by calling APIs as mentioned here https://docs.wso2.com/display/Governance540/Resources+with+REST+API
