# Dokka GraphQLDescription Plugin

Transformer which adds a description to all Classes, Interfaces, Objects, Annotations, Enums, Functions and Properties 
for a source set if for this source set no description exists and a `GraphQLDescription` is present.

Usage:
```kt
dependencies {
    dokkaPlugin("io.github.graphglue:dokka-graphql-description-plugin:1.0.0")
}
```