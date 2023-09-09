package io.github.graphglue.dokka

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.DokkaPluginApiPreview
import org.jetbrains.dokka.plugability.PluginApiPreviewAcknowledgement

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

    @OptIn(DokkaPluginApiPreview::class)
    override fun pluginApiPreviewAcknowledgement(): PluginApiPreviewAcknowledgement =
        PluginApiPreviewAcknowledgement

}