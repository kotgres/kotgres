![kotgres banner](media/readme.png)

**This library is in alpha, and the API is subject to change**

This is a blazing-fast Kotlin ORM to access your Postgres database _(also works with Java)_.

It stands out because it is:

✅ **Blazing fast**: trading blows with ORMLite as the fastest Kotlin/Java ORM for Postgres (according to our benchmarks)

✅ **Simple &low-api surface**: only one way to do things to avoid confusion

✅ **Postgres-only**: to keep things simple and maximize performance (no other databases will ever be integrated)

✅ **Extensible**: you can accommodate any use cases by adding new operators, using raw queries, ...

✅ **No-config**: no more XML or YAML files, only minimal annotations on your entities

✅ **Clean architecture friendly**: does not leak into your domain or presentation layers


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

