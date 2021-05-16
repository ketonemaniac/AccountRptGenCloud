# AccountRptGenCloud #
Accounting Report Generator on Google Cloud. Runs with Spring boot with a Bootstrap UI.

*Since this is a public domain, Google account keys and the Proprietory Accounting Excel is not uploaded to Github* 

## Run ##
In the root module run the following to run react build the web content (main.js)
`mvn clean package`

Main class: /app AccrptgenApplication.java

Running locally in IDE without google services (local)
* Put files in /local/files
* Working directory: /local
* VM options: `-Dspring.profiles.active=local`

Submitting locally but running the task in App Engine
* Working directory: /app
* VM options: `-Dspring.profiles.active=staging,gCloudStandard`
* Put in Environment variable GOOGLE_APPLICATION_CREDENTIALS for access to different buckets

## Deploy ##
* set project: `gcloud config set project accountrptgen-hk-test`
* Run `mvn validate appengine:deploy -Dapp.stage.appEngineDirectory=src/main/appengine/staging` in app folder

This is equivalent to running the following in the /app/target/appengine-staging folder
`gcloud app deploy --version 2-6-0-1 --project accountrptgen-hk-test`

### Prod ###
* set project: `gcloud config set project accountrptgen-hk`
* Run `mvn validate appengine:deploy -Dapp.stage.appEngineDirectory=src/main/appengine/prod` in app folder


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

### Version History ###
2.2.0: appengine/Java 11 upgrade, Mongo DB, use LocalDateTime

2.1.1: new React UI

1.0.0: initial version 