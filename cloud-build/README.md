# Google Cloud Build #

Continuous Deployment using Google cloud
Remember to enable `Cloud Run Admin` permissions in Cloud Build Settings (in the console).
The files in this folder are referenced by cloud build triggers.

## cloudbuild.yaml file

### Local ###
https://cloud.google.com/build/docs/build-debug-locally

- Install and run Docker
- Run in root folder `cloud-build-local --config=./build/cloudbuild.yaml --dryrun=false .`


npm and maven needs to run separately.
Make sure you run in the correct folder.

### Maven Build ###
https://cloud.google.com/build/docs/building/build-java
https://hub.docker.com/_/maven  


## Triggers

https://cloud.google.com/build/docs/automating-builds/create-github-app-triggers

