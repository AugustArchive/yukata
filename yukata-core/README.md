# module `yukata-core`
> Core implementation of **yukata (浴衣)**.

## Usage
```kotlin
fun main(args: Array<String>) {
    val schema = Schema {
        resolvers += listOf(
            MyResolver(),
            AnotherResolver()
        )
    }
    
    schema
        .compile()
        .execute(
            "{ hello }",
            variables = mapOf()
        )
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
    implementation("dev.floofy.yukata:yukata-core:<VERSION>")
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
    implementation "dev.floofy.yukata:yukata-core:<VERSION>"
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
        <artifactId>yukata-core</artifactId>
        <version>{{VERSION}}</version>
        <type>pom</type>
    </dependency>
</dependencies>
```

