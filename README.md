# psc-delta-consumer
The psc-delta-consumer is responsible for transforming psc data from the psc-delta kafka topic as part of chips and chs data sync.

# Requirements

- [Java 11](https://www.oracle.com/uk/java/technologies/javase/jdk11-archive-downloads.html)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)

### Building Docker Image Locally

_Note: this will change once the service is upgraded to Java 21_

```bash
mvn compile jib:dockerBuild -Dimage=169942020521.dkr.ecr.eu-west-2.amazonaws.com/local/psc-delta-consumer:latest
```