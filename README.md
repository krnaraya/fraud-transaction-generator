# Fraud Transaction Generator

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Prerequistes

Install a Openshift 4.x Cluster - Refer https://docs.openshift.com/container-platform/4.5/welcome/index.html

Install the demo of MLOps from this repository https://github.com/rhappdev-demo/fraud_detection

This should have created a fraud-demo-dev namespace, if you provided "fraud-demo" as the project fix. Else naviagate to <prj_prefix>_dev namespace.

Install AMQ Streams Operator in Openshift Cluster on to this (<prj_prefix>_dev) namespace  - Refer https://access.redhat.com/documentation/en-us/red_hat_amq/7.7/html/deploying_and_upgrading_amq_streams_on_openshift/deploy-intro_str

Install a Kafka Cluster (name it my-cluster, if not feel free the update the application properties to the correct kafka configuration) from the AMQ streams operator - Refer https://access.redhat.com/documentation/en-us/red_hat_amq/7.7/html/deploying_and_upgrading_amq_streams_on_openshift/deploy-tasks_str#kafka-cluster-str


## Packaging and running the application

Login to your openshift cluster - oc login <login_url>

Switch to you project (namesapace) - oc project <project_name>

```
mvn clean package -DskipTests

```
We have Openshift extension installed and hence the above command will package and deploy it as a container in openshift cluster


## Testing the application

Browse the URL that is exposed for the container as part of the build process 

Select the CSV file to upload - Sample file (creditcard_1000.csv) is available in resources folder and click submit.

This will parse the csv, convert it into json file and produce the records to kafka and tell you haow many records were produced at the end.

If the kafka cluster was named as (my_cluster) in the above step. you can check the messages produced with below command

```
oc exec -c kafka -i my-cluster-kafka-0  -- bin/kafka-console-consumer.sh --topic transactions --bootstrap-server localhost:9092

```
If the above sample csv file was uploaded, you should see some message like below 

```
{"Id":"9923","Time":"14665","V1":"1.1121215350701201","V2":"0.295503658257805","V3":"1.09371421396498","V4":"1.89034674204511","V5":"-0.339467865415942","V6":"0.0750482577739603","V7":"-0.37837053228883794","V8":"-0.0464934255957256","V9":"0.802377027401254","V10":"0.0902599895478566","V11":"0.838846610040917","V12":"-2.01981030396276","V13":"2.80911449747447","V14":"1.27203074485649","V15":"0.24641455064789605","V16":"0.6227253810289929","V17":"0.307803391657706","V18":"-0.41618208360635994","V19":"-1.5022265684233898","V20":"-0.0420221527793273","V21":"0.009984711597014391","V22":"0.278805619456167","V23":"-0.0105466740382272","V24":"0.107185559423634","V25":"0.163011822323505","V26":"1.01334056483191","V27":"-0.06699796633063","V28":"0.0126858777802122","Amount":"32.9","Class":"0"}

```
