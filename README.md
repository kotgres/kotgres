![kotgres-dsl banner](media/readme.png)

**This library is in alpha, and the API is subject to change**

This is a efficient Kotlin ORM to access your Postgres database (also works with Java).

It stands out because it is:

✅ **Blazing fast**: between 1st and 2nd place among the most used Kotlin/Java ORM for Postgres (according to our benchmarks)

✅ **Simple, opinionated & low-api surface**: only one way to do things to avoid confusion

✅ **Postgres-only**: no other databases will ever be integrated, allowing us to achieve the best performance

✅ **Extensible**: you can accommodate any use cases by adding new operators, using raw queries, ...

✅ **No-config**: more XML or YAML files, only minimal annotations on your entities

✅ **Clean architecture friendly**: more XML or YAML files, only minimal annotations on your entities


## Installation

The library is currently distributed through Jitpack, planning to release on Maven Central soon. To install, add this to your `build.gradke.kts`:

```kotlin
repositories {
    <...>
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    <...>
    implementation("com.github.kotgres:kotgres:0.1.0")
}
```

Or if you use Groovy (aka `build.gradle`):
```groovy
repositories {
    <...>
    maven { url 'https://jitpack.io' }
}

dependencies {
    <...>
    implementation 'com.github.kotgres:kotgres:0.1.0'
}
```

## Quick start

TODO

