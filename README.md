# psc-delta-consumer
The psc-delta-consumer is responsible for transforming psc data from the psc-delta kafka topic as part of chips and chs data sync.

# Requirements

- [Java 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)

### Building Docker Image Locally

_Note: this will change once the service is upgraded to Java 21_

```bash
mvn compile jib:dockerBuild -Dimage=416670754337.dkr.ecr.eu-west-2.amazonaws.com/psc-delta-consumer:latest
```