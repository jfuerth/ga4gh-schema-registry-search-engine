## Quick Setup

*Java 23* - You can use any distribution. For example,
[Amazon Corretto](https://docs.aws.amazon.com/corretto/latest/corretto-23-ug/downloads-list.html) provides installers
for Linux, Mac, and Windows.

## Advanced Setup - Durable Storage

*PostgreSQL with pgvector* - Optional. If you're just trying it out, start with the in-memory store.

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
