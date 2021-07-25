# module `yukata-parser`
> Language parsing for parsing GraphQL objects.
> 
> Note: This is bundled with `yukata-core`.

## Usage
```kotlin
fun main(args: Array<String>) {
    val parser = Parser {
        noLocation = false
    }
    
    parser.parse("{ hello }", variables = mapOf())
}
```

## Installation
> Documentation: https://yukata.floofy.dev
>
> Version: 1.0.0

## Gradle
### Kotlin DSL
```kotlin
repositories {
    maven {
        url = uri("https://maven.floofy.dev/repo/releases")
    }
}

dependencies {
    implementation("dev.floofy.yukata:yukata-parser:<VERSION>")
}
```

### Groovy DSL
```groovy
repositories {
    maven {
        url "https://maven.floofy.dev/repo/releases"
    }
}

dependencies {
    implementation "dev.floofy.yukata:yukata-parser:<VERSION>"
}
```

## Maven
```xml
<repositories>
    <repository>
        <id>noel-maven</id>
        <url>https://maven.floofy.dev/repo/releases</url>
    </repository>
</repositories>
```

```xml
<dependencies>
    <dependency>
        <groupId>dev.floofy.yukata</groupId>
        <artifactId>yukata-parser</artifactId>
        <version>{{VERSION}}</version>
        <type>pom</type>
    </dependency>
</dependencies>
```
