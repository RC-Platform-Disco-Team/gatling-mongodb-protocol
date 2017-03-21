# gatling-mongodb-protocol

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ed9bf4ecb69f446986170dedebf582b9)](https://www.codacy.com/app/mskonovalov/gatling-mongodb-protocol?utm_source=github.com&utm_medium=referral&utm_content=RC-Platform-Disco-Team/gatling-mongodb-protocol&utm_campaign=badger)

MongoDB protocol for Gatling

### Mongo protocol configuration

* uri - the connection URI, see [the MongoDB URI documentation for more information](http://docs.mongodb.org/manual/reference/connection-string/)

Example:
```scala
val mongoProtocol = mongo
  .uri("mongodb://username:password@host:port/database")
```

* hosts - Array of string in format: "host:port"
* authSource: The database source for authentication credentials.
* authMode: The authentication mode. By default set to scram-sha1 for SCRAM-SHA-1. Can be configured with mongocr for the backward compatible MONGODB-CR.
* connectTimeoutMS: The number of milliseconds to wait for a connection to be established before giving up.
* maxIdleTimeMS: The maximum number of milliseconds that a connection can remain idle in the pool before being removed and closed.
* sslEnabled: It enables the SSL support for the connection (true|false).
* sslAllowsInvalidCert: If sslEnabled is true, this one indicates whether to accept invalid certificates (e.g. self-signed).
* tcpNoDelay: TCPNoDelay boolean flag (true|false).
* keepAlive: TCP KeepAlive boolean flag (true|false).
* nbChannelsPerNode: Number of channels (connections) per node.
* writeConcern: The default write concern (default: acknowledged).
        unacknowledged: Option w set to 0, journaling off (j), fsync off, no timeout.
        acknowledged: Option w set to 1, journaling off, fsync off, no timeout.
        journaled: Option w set to 1, journaling on, fsync off, no timeout.
    
* readPreference: The default read preference (primary|primaryPreferred|secondary|secondaryPreferred|nearest) (default is primary).
* failover: The default failover strategy.
        default: The default/minimal strategy, with 10 retries with an initial delay of 100ms and a delay factor of retry count * 1.25 (100ms .. 125ms, 250ms, 375ms, 500ms, 625ms, 750ms, 875ms, 1s, 1125ms, 1250ms).
        remote: The strategy for remote MongoDB node(s); Same as default but with 16 retries.
        strict: A more strict strategy; Same as default but with only 5 retries.
        <delay>:<retries>x<factor>: The definition of a custom strategy;
            delay: The initial delay as a finite duration string accepted by the Duration factory.
            retries: The number of retry (Int).
            factor: The Double value to multiply the retry counter with, to define the delay factor (retryCount * factor).
* monitorRefreshMS: The interval (in milliseconds) used by the ReactiveMongo monitor to refresh the node set (default: 10s); The minimal value is 100ms.

For more information see [the MongoDB connection options](http://reactivemongo.org/releases/0.12/documentation/tutorial/connect-database.html)

Example:
```scala
val mongoProtocol = mongo
  .hosts("host1", "host2:port2")
  .username("usr")
  .password("pass")
  .database("database")
```