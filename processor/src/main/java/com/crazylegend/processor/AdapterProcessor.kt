package com.crazylegend.processor

import com.crazylegend.annotations.ViewBindingAdapter
import com.crazylegend.annotations.color.BindColor
import com.crazylegend.annotations.diffUtil.DiffUtilBinding
import com.crazylegend.annotations.image.BindImage
import com.crazylegend.annotations.text.BindText
import com.crazylegend.annotations.viewHolder.ViewHolderBinding
import com.crazylegend.annotations.visibility.BindVisibility
import com.crazylegend.processor.codegen.AdapterBuilder
import com.crazylegend.processor.codegen.AdapterModel
import com.crazylegend.processor.color.ColorBindingData
import com.crazylegend.processor.image.ImageBindingData
import com.crazylegend.processor.text.TextBindingData
import com.crazylegend.processor.utils.bindEnclosedElements
import com.crazylegend.processor.utils.tryOrNull
import com.crazylegend.processor.visibility.VisibilityBindingData
import com.squareup.kotlinpoet.FileSpec
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic


@SupportedSourceVersion(SourceVersion.RELEASE_8)
class AdapterProcessor : AbstractProcessor() {

    private val elementUtils get() = processingEnv.elementUtils
    private val typeUtils get() = processingEnv.typeUtils

    companion object {
        const val KAPT_KOTLIN_GENERATED_NAME = "kapt.kotlin.generated"
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(ViewBindingAdapter::class.java.canonicalName)

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {

        val generatedDirectory = processingEnv.options[KAPT_KOTLIN_GENERATED_NAME] ?: return false
        roundEnv.getElementsAnnotatedWith(ViewBindingAdapter::class.java).forEach {
            val adapterData = createAdapterData(it) ?: return false
            val adapterFileName = "${adapterData.pojoModelName}Adapter"
            FileSpec.builder(adapterData.packageName, adapterFileName)
                    .addType(AdapterBuilder(adapterFileName, adapterData).build())
                    .build()
                    .writeTo(File(generatedDirectory))
        }
        return true
    }

    private fun getViewBindingAdapterMirror(annotation: ViewBindingAdapter): TypeMirror? {
        try {
            annotation.viewBinding
        } catch (mte: MirroredTypeException) {
            return mte.typeMirror
        }
        return null
    }

    private fun createAdapterData(element: Element?): AdapterModel? {
        element ?: return null

        val packageName = elementUtils.getPackageOf(element).toString()
        val pojoModelName = element.simpleName.toString()
        val annotation = element.getAnnotation(ViewBindingAdapter::class.java)
        val viewBindingTypeMirror = getViewBindingAdapterMirror(annotation)
        val viewBindingPackageElement = typeUtils.asElement(viewBindingTypeMirror)
        val viewBindingPackage = "${elementUtils.getPackageOf(viewBindingPackageElement)}.${viewBindingPackageElement.simpleName}"
        val viewBindingPackageName = viewBindingPackage.substringBeforeLast(".")
        val viewBindingType = viewBindingPackage.substringAfterLast(".")

        checkIfViewBindingCanBeProcessed(viewBindingPackage)

        val diffUtilPackageName = constructDiffUtilPackageName(element, packageName, pojoModelName)

        val customViewHolder = constructCustomViewHolderPackageName(element, packageName, pojoModelName, viewBindingPackage)


        val colorBindingData = element.bindEnclosedElements<BindColor, ColorBindingData> { fieldName, callback ->
            ColorBindingData(fieldName, callback.viewName, callback.type, callback.clickListenerType)
        }

        val textBindingData = element.bindEnclosedElements<BindText, TextBindingData> { fieldName, callback ->
            TextBindingData(fieldName, callback.viewName, callback.textBindingType, callback.clickListenerType)
        }

        val imageBindingData = element.bindEnclosedElements<BindImage, ImageBindingData> { fieldName, callback ->
            ImageBindingData(fieldName, callback.viewName, callback.imageBindingType, callback.clickListenerType)
        }

        val visibilityBindingData = element.bindEnclosedElements<BindVisibility, VisibilityBindingData> { fieldName, callback ->
            VisibilityBindingData(fieldName, callback.viewName, callback.visibilityType, callback.checkPreviousVisibilityToSetNew, callback.elseClauseSetToTheOppositeVisibility, callback.visibleToGone, callback.clickListenerType)
        }


        return AdapterModel(packageName, pojoModelName, viewBindingPackageName,
                viewBindingType, diffUtilPackageName, customViewHolder, textBindingData, imageBindingData,
                visibilityBindingData,
                colorBindingData, annotation.attachItemViewClickListener, annotation.attachItemViewLongClickListener)
    }


    private fun constructCustomViewHolderPackageName(element: Element, packageName: String, pojoModelName: String, viewBindingPackage: String): String? {

        val viewHolderElements = element.enclosedElements.mapNotNull { it.getAnnotation(ViewHolderBinding::class.java) }

        if (viewHolderElements.size >= 2) {
            error("Only one ViewHolder is allowed for the recyclerview")
            return null
        }

        return if (viewHolderElements.size >= 2) {
            error("Only one ViewHolder is allowed for the recyclerview")
            null
        } else {
            val viewHolderElement = element.enclosedElements.firstOrNull {
                it.getAnnotation(ViewHolderBinding::class.java) != null
            }
            val viewHolderPackageName = if (viewHolderElement != null) {
                "${packageName}.${pojoModelName}.${viewHolderElement.simpleName}"
            } else {
                null
            }

            checkIfViewHolderCanBeProcessed(viewHolderPackageName, viewBindingPackage, packageName, pojoModelName)

            return viewHolderPackageName
        }
    }

    private fun checkIfViewHolderCanBeProcessed(viewHolderPackagename: String?, viewBindingPackage: String, packageName: String, pojoModelName: String) {
        if (!viewHolderPackagename.isNullOrEmpty()) {
            val errorMessageNotViewHolder = "Please extend androidx.recyclerview.ViewHolder "
            val errorMessageContractNotImplemented = "Please implement com.crazylegend.annotations.viewHolder.ViewHolderContract and it's members "
            val viewHolderType = tryOrNull { elementUtils.getTypeElement("androidx.recyclerview.widget.RecyclerView.ViewHolder").asType() }
            val viewHolderFromAnnotation = tryOrNull { elementUtils.getTypeElement(viewHolderPackagename).asType() }
            val viewHolderContractType = tryOrNull { elementUtils.getTypeElement("com.crazylegend.annotations.viewHolder.ViewHolderContract").asType() }


            if (viewHolderType == null) {
                error("Please include androidx.recyclerview as a dependency")
                return
            }

            if (viewHolderFromAnnotation == null) {
                error(errorMessageNotViewHolder)
                return
            }

            val element = typeUtils.asElement(viewHolderFromAnnotation)
            element.enclosedElements.forEach {
                when (it.kind) {
                    ElementKind.CONSTRUCTOR -> {
                        it as ExecutableElement
                        val errorMessage = "Your constructor parameter must be $viewBindingPackage"
                        val viewBindingType = tryOrNull { elementUtils.getTypeElement(viewBindingPackage).asType() }
                        val constructorParam = it.parameters.firstOrNull()
                        if (constructorParam == null) {
                            error(errorMessage)
                            return
                        }

                        if (viewBindingType == null) {
                            error("Please enable view binding in your app build gradle, buildFeatures { viewBinding = true  }")
                            return
                        }

                        val constructorType = constructorParam.asType()
                        if (!typeUtils.isSameType(viewBindingType, constructorType))
                            error(errorMessage)
                    }
                    ElementKind.METHOD -> {
                        it as ExecutableElement

                        if (it.simpleName.toString().toLowerCase() == "bind") {
                            val errorMessage = "You must implement bind function from ViewHolderContract with $pojoModelName as a parameter"
                            val bindParam = it.parameters.firstOrNull()
                            if (bindParam == null) {
                                error(errorMessage)
                                return
                            }
                            val bindPojoType = tryOrNull { elementUtils.getTypeElement("${packageName}.${pojoModelName}").asType() }
                            if (bindPojoType == null) {
                                error("Checkout the $pojoModelName class for errors")
                                return
                            }

                            if (!typeUtils.isSameType(bindParam.asType(), bindPojoType)) {
                                error(errorMessage)
                                return
                            }
                        }
                    }
                    else -> {}
                }

            }


            val isViewHolderAssignable = typeUtils.isAssignable(viewHolderFromAnnotation, viewHolderType)
            if (!isViewHolderAssignable)
                error(errorMessageNotViewHolder)

            if (viewHolderContractType == null) {
                error(errorMessageContractNotImplemented)
                return
            }

            val isContracted = typeUtils.isAssignable(viewHolderFromAnnotation, typeUtils.erasure(viewHolderContractType))
            if (!isContracted) {
                error(errorMessageContractNotImplemented)
                return
            }
        }
    }

    private fun checkIfDiffUtilCanBeProcessed(diffUtilPackageName: String?, pojoModelName: String, packageName: String) {
        if (!diffUtilPackageName.isNullOrEmpty()) {
            val errorMessage = "Please extend androidx.recyclerview.widget.DiffUtil and implement ItemCallback<$pojoModelName> members "
            val diffUtilType = tryOrNull { elementUtils.getTypeElement("androidx.recyclerview.widget.DiffUtil.ItemCallback").asType() }
            val diffUtilFromAnnotation = tryOrNull { elementUtils.getTypeElement(diffUtilPackageName).asType() }


            if (diffUtilType == null) {
                error("Please include androidx.recyclerview as a dependency")
                return
            }

            if (diffUtilFromAnnotation == null) {
                error(errorMessage)
                return
            }

            val superType = typeUtils.directSupertypes(diffUtilFromAnnotation).firstOrNull()

            if (superType == null) {
                error(errorMessage)
                return
            }

            val declaredDiffUtilCallback = tryOrNull { superType as DeclaredType }

            if (declaredDiffUtilCallback == null) {
                error(errorMessage)
                return
            }

            val superTypeDiffUtilCallback = declaredDiffUtilCallback.asElement()
            if (!typeUtils.isSameType(superTypeDiffUtilCallback.asType(), diffUtilType)) {
                error(errorMessage)
            }

            val declaredType = declaredDiffUtilCallback.typeArguments.firstOrNull()
            val pojoType = tryOrNull { elementUtils.getTypeElement("${packageName}.${pojoModelName}") }?.asType()

            if (declaredType == null){
                error(errorMessage)
                return
            }

            if (pojoType == null){
                error("Checkout your $pojoModelName class for errors")
                return
            }

            if (!typeUtils.isSameType(declaredType, pojoType)){
                error(errorMessage)
                return
            }
        }
    }

    private fun constructDiffUtilPackageName(element: Element, packageName: String, pojoModelName: String): String? {
        val diffUtilElements = element.enclosedElements.mapNotNull { it.getAnnotation(DiffUtilBinding::class.java) }

        return if (diffUtilElements.size >= 2) {
            error("Only one DiffUtil is allowed for the recyclerview")
            null
        } else {
            val diffUtilElement = element.enclosedElements.firstOrNull {
                it.getAnnotation(DiffUtilBinding::class.java) != null
            }
            val diffUtilPackageName = if (diffUtilElement != null) {
                "${packageName}.${pojoModelName}.${diffUtilElement.simpleName}"
            } else {
                null
            }

            checkIfDiffUtilCanBeProcessed(diffUtilPackageName, pojoModelName, packageName)

            if (diffUtilElement != null) {
                "${packageName}.${pojoModelName}.${diffUtilElement.simpleName}()"
            } else {
                null
            }
        }
    }

    private fun checkIfViewBindingCanBeProcessed(viewBindingPackage: String) {
        val errorMessageForViewBindingPackageNotCorrect = "You must provide a view binding package name as a variable !!! "
        val viewBindingType = tryOrNull { elementUtils.getTypeElement("androidx.viewbinding.ViewBinding").asType() }

        if (viewBindingType == null) {
            error("Please enable view binding in your app build gradle, buildFeatures { viewBinding = true  }")
            return
        }

        val viewBindingFromAnnotation = tryOrNull { elementUtils.getTypeElement(viewBindingPackage).asType() }


        if (viewBindingFromAnnotation == null) {
            error(errorMessageForViewBindingPackageNotCorrect)
            return
        }


        val isAssignable = typeUtils.isAssignable(viewBindingFromAnnotation, viewBindingType)
        if (!isAssignable)
            error(errorMessageForViewBindingPackageNotCorrect)
    }


    private fun debug(message: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.OTHER, "--> $message <--")
    }

    private fun error(message: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
    }

}
