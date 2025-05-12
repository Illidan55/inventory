<h1 align="center"> Inventory App </h1> <br>

![image](https://github.com/user-attachments/assets/1d876789-da93-4c3f-a634-ab3b1709380e)


<p align="center">
  This is a Java project built using Spring Boot, MongoDB, and Thymeleaf
</p>

## Requirements
The application can be run locally, the requirements are that the following environment variables need to be set in a application-dev.yml file under /resources/

-MONGODB_HOST:&emsp;&emsp;&emsp;//Hostname for a running MongoDb

-MONGODB_PORT:&emsp;&emsp;&emsp;//Port for a running MongoDB

-MONGODB_DATABASE:&emsp;//Database name

-MONGODB_USER:&emsp;&emsp;&emsp;//Database user name

-MONGODB_PASSWORD:&emsp;//Databse user password

-MONGODB_AUTH_SOURCE:&emsp;//Auth source

-TITLE:&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;//Company Name to be displayed

If you want a logo to appear at the top of the page replace logo.png under /resources/static/images


### MongoDb
A running instance of [MongoDB](https://www.mongodb.com/) is required to run this application

### Local
* [Java 17 SDK](https://openjdk.org/projects/jdk/17/)
* [Maven](https://maven.apache.org/download.cgi)

## Quick Start
Create application-dev.yml and add the properties listed in requirments

Make sure the MongoDB instance is running and is configured properly in application-dev.yml

### Run Local
```bash
$ mvn spring-boot:run
```

Application will run by default on port `8080`

Configure the port by changing `server.port` in __application.yml__

To get to the ui go to http://localhost:8080/


## TODO
TODO: The Sales page has yet to be implemented
