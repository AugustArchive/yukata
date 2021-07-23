<div align="center">
    <h2>🗼 yukata</h2>
    <blockquote>浴衣 - <strong>Modernized and fast implementation of GraphQL made in <a href="https://kotlinlang.org">Kotlin</a></strong></blockquote>
</div>

> **yukata** is never meant to be captialised, so it'll just be **yukata** if you mention it in your projects.

## Usage
```kotlin
fun main(args: Array<String>) {
    val schema = Schema {
        resolvers += listOf(
            SomeResolver(),
            AnotherResolver()
        )
    }
}
```

## Modules
- [yukata-ktor](./yukata-ktor) **~** Ktor plugin to implement a GraphQL endpoint.
- [yukata-core](./yukata-core) **~** Core implementation of **yukata**.

## License
**yukata** is released under the **MIT** License, read the [LICENSE](./LICENSE) file for more information.
