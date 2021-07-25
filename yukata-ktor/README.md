# module `yukata-ktor`
> Ktor plugin to use **yukata** within your GraphQL server.

## Usage
```kotlin
fun Application.module() {
    install(GraphQL) {
        playground = true
        schema = Schema {
            resolvers += listOf(
                MyResolver()
            )
        }
    }
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
    implementation("dev.floofy.yukata:yukata-ktor:<VERSION>")
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
    implementation "dev.floofy.yukata:yukata-ktor:<VERSION>"
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
        <artifactId>yukata-ktor</artifactId>
        <version>{{VERSION}}</version>
        <type>pom</type>
    </dependency>
</dependencies>
```
