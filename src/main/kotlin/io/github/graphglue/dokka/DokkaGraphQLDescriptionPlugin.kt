package io.github.graphglue.dokka

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaPlugin

/**
 * Plugin to add GraphQLDescriptions as description where no description exists yet
 */
class DokkaGraphQLDescriptionPlugin : DokkaPlugin() {

    /**
     * The extension provided by the plugin
     */
    val graphQLDescriptionExtension by extending {
        plugin<DokkaBase>().preMergeDocumentableTransformer providing ::AddGraphQLDescriptionTransformer
    }

}