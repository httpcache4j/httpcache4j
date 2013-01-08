# HTTPCache4j a Java Browser cache.

HTTP Cache 4 Java is a project that tries to uphold the caching rules defined in HTTP.
The rules are mostly defined in [Section 13](http://tools.ietf.org/html/rfc2616#section-13) in RFC2616.
The rules are changing slightly in httpbis, and will result in a new set of RFCs which will invalidate RFC2616.

Most types in HTTPCache4j are immutable. Meaning all builders, and other types of objects.

I have created a few mutable versions which delegates to the immutable types underneath.
The mutable versions are not thread safe.

## Usage

### Spring support
I have added spring support to be able to use the RestTemplate.

    <dependency>
      <groupId>org.codehaus.httpcache4j</groupId>
      <artifactId>httpcache4j-spring</artifactId>
      <version>4.0-M4</version>
    </dependency>

### Maven
Add this to your POM

    <dependency>
      <groupId>org.codehaus.httpcache4j</groupId>
      <artifactId>httpcache4j-core</artifactId>
      <version>4.0-M4</version>
    </dependency>

Now choose your resolver.

    <dependency>
      <groupId>org.codehaus.httpcache4j.resolvers</groupId>
      <artifactId>resolvers-commons-httpclient</artifactId>
      <version>4.0-M4</version>
    </dependency>

    <dependency>
      <groupId>org.codehaus.httpcache4j.resolvers</groupId>
      <artifactId>resolvers-httpcomponents-httpclient</artifactId>
      <version>4.0-M4</version>
    </dependency>

    <dependency>
      <groupId>org.codehaus.httpcache4j.resolvers</groupId>
      <artifactId>resolvers-net-urlconnection</artifactId>
      <version>4.0-M4</version>
    </dependency>

    <dependency>
      <groupId>org.codehaus.httpcache4j.resolvers</groupId>
      <artifactId>resolvers-ning-async</artifactId>
      <version>4.0-M4</version>
    </dependency>

Then choose your storage mechanism.

The core has a built in Memory storage, and is useful for non-persistent small data.

If this is not enough, there are a number of persistent storages to choose from

      <dependency>
        <groupId>org.codehaus.httpcache4j.storage</groupId>
        <artifactId>storage-h2</artifactId>
        <version>4.0-M4</version>
      </dependency>
      <dependency>
        <groupId>org.codehaus.httpcache4j.storage</groupId>
        <artifactId>storage-file</artifactId>
        <version>4.0-M4</version>
      </dependency>
      <dependency>
        <groupId>org.codehaus.httpcache4j.storage</groupId>
        <artifactId>storage-derby</artifactId>
        <version>4.0-M4</version>
      </dependency>
      <dependency>
        <groupId>org.codehaus.httpcache4j.storage</groupId>
        <artifactId>storage-ehcache</artifactId>
        <version>4.0-M4</version>
      </dependency>


There is also a generic jdbc storage which you can use to build your own jdbc based storage.

## SNAPSHOT version

Snapshots are available from

### Maven

    <repository>
      <id>codehaus-snapshots</id>
      <url>https://nexus.codehaus.org/content/repositories/snapshots</url>
    </repository>



## HTTP Library

If you want to use HTTPCache4j as a normal HTTP library without the caching functions, you can.
Just add the appropriate resolver and use it as normal.