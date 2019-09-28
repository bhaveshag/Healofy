## Concurrent reading of logs

### Prerequisites

#### Install Java
Java SDK 1.6 or higher is required to run the application.

Follow the instructions [here](https://java.com/en/download/help/download_options.xml)

#### Install Gradle
Gradle installs all the dependencies required for the application including the framework Spring Boot itself.

Follow the instructions [here](https://gradle.org/install/)

#### Configuration
The application has to be configured to connect to the database. Specify the connection parameters by modifying these lines of code in `src/main/java/library/Application.java`

`dataSource.setDriverClassName()` specifies the driver to use to connect to the database

`dataSource.setUrl()` specifies the database connection URL 

`dataSource.setUserName()` specifies the username of the user having access to the database

`dataSource.setPassword()` specifies the password of the user 

### Build application

`./gradlew clean build`

### Run application

`./gradlew bootRun`


