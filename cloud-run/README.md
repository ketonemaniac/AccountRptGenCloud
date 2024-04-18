# Cloud Run setup
One time setup before cloud build could start deploying the services.
The services need to be present in the list first, and access configured, before cloud build could actually 
deploy into it.
The files in this folder is to be run locally using gcloud command.

[Cloud Run Dashboard](https://console.cloud.google.com/run?project=accountrptgen-hk&supportedpurview=project)

Run the following in project root:

## Staging
```
gcloud config set run/region australia-southeast1
gcloud config set run/platform managed
```

image location
> australia-southeast1-docker.pkg.dev/accountrptgen-hk-test/accountrptgen/app`

Cloud run UI
> https://console.cloud.google.com/run?project=accountrptgen-hk-test&supportedpurview=project

deploy service/IAM
```
gcloud run services replace cloud-run/staging/service.yaml
gcloud run services set-iam-policy acctrptgen-test cloud-run/staging/policy.yaml
```

## Prod
Note: asia-east2 (hk) has no cloud run domain mapping. Use asia-east1 instead
"Firebase Hosting is unavailable for this service. Reason: Region not supported."
See https://cloud.google.com/run/docs/mapping-custom-domains#top
```
gcloud config set run/region asia-east1
gcloud config set run/platform managed
```


deploy service/IAM
```
gcloud run services replace cloud-run/prod/service.yaml
gcloud run services set-iam-policy acctrptgen cloud-run/prod/policy.yaml
```
