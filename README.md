# AccountRptGenCloud #
Accounting Report Generator on Google Cloud. Runs with Spring boot with a Bootstrap UI.

*Since this is a public domain, Google account keys and the Proprietory Accounting Excel is not uploaded to Github* 

## Run ##
In the root module run the following to run react build the web content (main.js)
`mvn clean package`

Running locally in IDE without google services (local)
* Put files in /local
* Main class in /local module: net.ketone.accrptgen.AccrptgenApplicationLocal 
* VM options: `-Dspring.profiles.active=local`

Local run with google services (cloud)
* make sure you have the right environment set in /cloud module's appengine-web.xml. Possible values are staging,prod
* Put in Enviroment variable GOOGLE_APPLICATION_CREDENTIALS for access to different buckets
* Run `mvn clean package` in root, then `mvn appengine:run` in cloud folder
* go to http://localhost:8080/_ah/admin to see task queues being set up

To deploy to google cloud
* make sure you have the right environment set in appengine-web.xml.
* Run `mvn validate appengine:deploy` in cloud folder

## Setup google services ## 

### View ###
- all projects: `gcloud projects list`
- current configuration: `gcloud config list`

### Set ###
1. set project: `gcloud config set project accountrptgen-hk`
2. Setup queues:, run the following in cloud/src/main/webapp/WEB-INF: `gcloud app deploy queue.yaml`
Reference:
https://cloud.google.com/appengine/docs/standard/python/config/queueref

## CI/CD ##

### Version Upgrade ###
    versions:set -DnewVersion=<new_version> -DprocessAllModules -DgenerateBackupPoms=false
