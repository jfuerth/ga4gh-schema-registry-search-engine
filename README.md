## GA4GH Schema Registry Search Engine

This application demonstrates a use case for the
[GA4GH Schema Registry Specification](https://ga4gh.github.io/schema-registry/), which is currently in draft.

This app crawls through all the namespaces and schemas of one or more Schema Registry implementations, and allows fuzzy
search of the schemas they contain.

## Quick Setup

### You will need
*Java 23* - You can use any distribution. For example,
[Amazon Corretto](https://docs.aws.amazon.com/corretto/latest/corretto-23-ug/downloads-list.html) provides installers
for Linux, Mac, and Windows.

### Launch the application

```shell
./mvnw spring-boot:run
```

Then visit http://localhost:7000 in your browser.

## Advanced Setup - Durable Storage

This part is optional. If you're just trying it out, start with the in-memory store.

### You will need
*PostgreSQL with pgvector*

On Mac, you can install the pgvector extension with the following command:
```shell
brew install pgvector
```

If you provide your own PostgreSQL instance, you'll need to create a database with the pgvector extension enabled:

```shell
createuser gscr_crawler
createdb -O gscr_crawler gscr_crawler
psql gscr_crawler -c "create extension if not exists vector"
```
