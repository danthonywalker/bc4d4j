# BC4D4J
Better Commands 4 Discord4J is a [Discord4J](https://github.com/austinv11/Discord4J) command framework built to
*command*. It is designed for huge bots in mind, knowing bot owners want commands with infinite flexibility and
unimaginable parallel processing requirements. BC4D4J takes an unopinionated and functional approach to command design
so bot owners can focus on making commands, rather than conforming to them.

While BC4D4J was written in [Kotlin](https://kotlinlang.org/), it has been designed with Java in mind, allowing easy
interop between the two languages.

## Modules
BC4D4J is built around the Discord4J module mechanism, allowing singular instances to be handled across multiple
clients. To further increase efficiency, BC4D4J itself is split into several modules depending on your exact usage
requirements.

[bc4d4j-common](https://github.com/danthonywalker/bc4d4j/tree/master/common) - An utility module built on top of
*bc4d4j-core*. Setup simple commands quickly using the provided utility functions and pre-defined, and otherwise common,
implementations of command functionality bindings.

[bc4d4j-core](https://github.com/danthonywalker/bc4d4j/tree/master/core) - The brains and heart of the BC4D4J system.
This module provides only the bare minimum to designing commands; leaving no room for bloat or wastefulness. Don't be
fooled by its minimalism, however, it is every bit capable and more so than any existing command framework.

[bc4d4j-java-bind](https://github.com/danthonywalker/bc4d4j/tree/master/java-bind) - If you're using Java, it is highly
recommended to use this module if you're directly implementing command functionality bindings provided by *bc4d4j-core*.

## Installing
To add BC4D4J to your CLASSPATH add the following to Maven or Gradle:

Maven:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.danthonywalker.bc4d4j</groupId>
        <artifactId>MODULE</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```
Gradle:
```groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compile "com.github.danthonywalker.bc4d4j:MODULE:VERSION"
}
```

* `VERSION` should be replaced with either a released version tag or a commit hash.
* `MODULE` should be replaced with the name of a module (do *not* include the bc4d4j- prefix).
