# AccountRptGenCloud #
Accounting Report Generator on Google Cloud. Runs with Spring boot with a Bootstrap UI.

*Will not run because the Proprietory Accounting Excel is not uploaded to Github. Might make a simple example in the future for the sake of the Unit tests.* 

## Run ##
In the root module run the following to run browsify the web content (main.js)
`mvn clean install`

Running locally in IDE without google services
* Main class: net.ketone.accrptgen.AccrptgenApplication
* VM options: `-Dspring.profiles.active=local`
* Run in app folder

Local run with google services
`mvn appengine:run`

To deploy to google cloud run
`mvn appengine:deploy`


## Switching Google App Engine Plans ##
There are two flavors from Google, the standard accepting a WAR and hosting it in a bunch of shared webservers, and flexible which is basically dedicated docker instances.
For the current usage, standard is far cheaper than flexible since the server only needs to be up for a matter of minutes per day, the rest being idle time (which flexible will count into the cost).

To switch between plans,

### Standard ###
* In application.properties, `spring.profiles.active=gCloudStandard` 
* `war/src/main/webapp/WEB-INF/appengine-web.xml` will be read. This defines the instance count etc
* uncomment `spring-boot-maven-plugin` in app's `pom.xml`. This will let the war include the resulting jar from app.  
* To deploy, run `clean appengine:deploy -Dmaven.test.skip=true` directly from app

### Flexible ###
* You don't need the war project.
* In application.properties, `spring.profiles.active=gCloudFlexible` 
* `app/src/main/appengine.yaml` will be read. This defines the instance count etc
* comment `spring-boot-maven-plugin` in app's `pom.xml`. This will make the app jar executable.  
* To deploy, run `clean appengine:deploy -Dmaven.test.skip=true` directly from app


