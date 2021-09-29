## CapstoneAPI

This project is developed using MySQL database in Scala programming Language and is implemented using Akka HTTP. This project focuses on the JWT authentication and role based authorization. The user is restricted from the routes which are accessible by admin.

### Pre-requisites

* Java 11
* Scala 2.13.5
* sbt 1.4.7
* docker 19.03.9
* docker-compose 1.25.0

## Commands

### docker

This command set up MySQL server on system using docker file.
Enter the project folder and run the  command:
````
docker-compose up

MySQL details
-------------
user: root
password: root
schema: capstone
port: 3306
````

### clean

This command cleans the sbt project by deleting the target directory. The command output relevant messages.
````
sbt clean
````

### compile

This command compiles the scala source classes of the sbt project.
````
sbt compile
````
### run

Enter the project folder and run the first command and then type service name you want to execute along with the sbt run command:
###### Note: Make sure you run the user service first!
````
sbt "project admin" run
````
More details about project (e.g. version etc..) can be found in file build.sbt


### Tests

Code is developed by applying [TDD](https://en.wikipedia.org/wiki/Test-driven_development) and tests are located in
folder **/src/test/scala-2.13**,  For running all tests enter the project folder and type:

 ```
 sbt test
 ```

### Coverage

scoverage plugin is used in the code for checking code coverage. Code coverage is 100%


 ```
 sbt "project admin" clean coverage test coverageReport
 ```

More details about project libraraies (e.g. version etc..) can be found in files:
**build.sbt**
**Dependencies**
**CommonSettings**
**plugins.sbt**


### Routes

##### Login
###### Request: POST <- localhost:9004/admin/login
###### Body: raw JSON
````
role: user/admin
{
    "email": "",
    "password": "",
    "role": ""
}
````
##### Create User
###### Request: POST <- localhost:9001/admin/create-user
###### Body: raw JSON
````
{
    "email": "",
    "password": "",
    "role": "user"
}
````
##### Create Admins with special key provided
###### Request: POST <- localhost:9001/admin/create-user
###### Body: raw JSON
````
{
    "email": "",
    "name": "",
    "password": "",
    "role": "admin",
    "key": ""
}
````


### Admin Credentials
````
email: admin@admin.com
password: admin@admin
key: authenticated_admin_key
````

## END
