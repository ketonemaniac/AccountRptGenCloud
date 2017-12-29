# AccountRptGenCloud #
Proprietory Accounting Report Generator on Google Cloud. Runs with Spring boot with static content written with ECMAScript6.

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





