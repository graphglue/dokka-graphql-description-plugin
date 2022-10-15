package io.github.graphglue.dokka

import org.jetbrains.dokka.base.parsers.MarkdownParser
import org.jetbrains.dokka.base.transformers.documentables.DocumentableReplacerTransformer
import org.jetbrains.dokka.model.*
import org.jetbrains.dokka.model.doc.Description
import org.jetbrains.dokka.model.doc.DocumentationNode
import org.jetbrains.dokka.model.properties.WithExtraProperties
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Transformer which adds a description to all Classes, Interfaces, Objects, Annotations, Enums, functions and properties
 * for a source set if for this source set no documentation or no description exists and a `GraphQLDescription` is present
 *
 * @param context necessary for [DocumentableReplacerTransformer]
 */
class AddGraphQLDescriptionTransformer(context: DokkaContext) : DocumentableReplacerTransformer(context) {

    /**
     * Parser used to parse graphql documentation
     */
    private val parser = MarkdownParser({ null }, null)

    override fun processClassLike(classlike: DClasslike): AnyWithChanges<DClasslike> {
        return processDocumentable(super.processClassLike(classlike)) { documentable, documentation ->
            when (documentable) {
                is DClass -> documentable.copy(documentation = documentation)
                is DInterface -> documentable.copy(documentation = documentation)
                is DObject -> documentable.copy(documentation = documentation)
                is DAnnotation -> documentable.copy(documentation = documentation)
                is DEnum -> documentable.copy(documentation = documentation)
            }
        }
    }

    override fun processEnumEntry(dEnumEntry: DEnumEntry): AnyWithChanges<DEnumEntry> {
        return processDocumentable(super.processEnumEntry(dEnumEntry)) { documentable, documentation ->
            documentable.copy(documentation = documentation)
        }
    }

    override fun processFunction(dFunction: DFunction): AnyWithChanges<DFunction> {
        return processDocumentable(super.processFunction(dFunction)) { documentable, documentation ->
            documentable.copy(documentation = documentation)
        }
    }

    override fun processProperty(dProperty: DProperty): AnyWithChanges<DProperty> {
        return processDocumentable(super.processProperty(dProperty)) { documentable, documentation ->
            documentable.copy(documentation = documentation)
        }
    }

    /**
     * Transforms a documentable by applying documentation based on a `GraphQLDescription` annotation if necessary
     *
     * @param T the type of documentable
     * @param documentableWithChanges the documentable with a flag if already changes are made
     * @param copyCallback called to create a new documentable based on the new documentation
     * @return the resulting documentable with a flag if changes were made
     */
    private fun <T : Documentable> processDocumentable(
        documentableWithChanges: AnyWithChanges<T>,
        copyCallback: (documentable: T, documentation: SourceSetDependent<DocumentationNode>) -> T
    ): AnyWithChanges<T> {
        val documentable = documentableWithChanges.target
        val annotations = (documentable as? WithExtraProperties<*>)?.extra?.allOfType<Annotations>()
            ?.flatMap { it.directAnnotations.values.flatten() } ?: emptyList()
        val descriptionAnnotation = annotations.firstOrNull {
            it.dri.packageName == "com.expediagroup.graphql.generator.annotations" && it.dri.classNames == "GraphQLDescription"
        }
        if (descriptionAnnotation != null && documentable != null) {
            val description = parser.parseTagWithBody(
                "description", (descriptionAnnotation.params["value"] as StringValue).text()
            )
            val newDocumentation = documentable.sourceSets.associateWith { sourceSet ->
                val existingDocumentation = documentable.documentation[sourceSet]
                if (existingDocumentation == null) {
                    DocumentationNode(listOf(description))
                } else if (existingDocumentation.children.none { it is Description }) {
                    existingDocumentation.copy(existingDocumentation.children + description)
                } else {
                    existingDocumentation
                }
            }
            return AnyWithChanges(copyCallback(documentable, newDocumentation), true)
        }
        return documentableWithChanges
    }
}