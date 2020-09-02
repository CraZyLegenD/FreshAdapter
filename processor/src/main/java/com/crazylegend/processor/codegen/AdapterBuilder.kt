package com.crazylegend.processor.codegen

import com.crazylegend.annotations.clickListeners.ClickListenerType
import com.crazylegend.annotations.color.ColorBindingType
import com.crazylegend.annotations.image.ImageTransformationType
import com.crazylegend.annotations.text.TextBindingType
import com.crazylegend.annotations.visibility.VisibilityBindingType
import com.crazylegend.processor.color.ColorBindingData
import com.crazylegend.processor.image.ImageBindingData
import com.crazylegend.processor.text.TextBindingData
import com.crazylegend.processor.visibility.VisibilityBindingData
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

/**
 * Created by crazy on 8/15/20 to long live and prosper !
 */
internal class AdapterBuilder(
        private val adapterFileName: String, private val adapterData: AdapterModel
) {

    private val customViewHolder = adapterData.generateCustomViewHolder()
    private val viewHolderName = customViewHolder?.viewHolderName ?: "ViewHolder"
    private val viewHolderClassName = customViewHolder?.viewHolderClassName
            ?: ClassName(adapterData.packageName, viewHolderName)
    private val viewHolderQualifiedClassName = customViewHolder?.viewHolderQualifiedClassName
            ?: ClassName(adapterData.packageName, "$adapterFileName.$viewHolderName")
    private val modelClassName = ClassName(adapterData.packageName, adapterData.pojoModelName)

    private fun FunSpec.Builder.attachOnItemViewClickListener(itemClickListener: String = "onItemViewClickListener",
                                                              viewBindingName: String = "itemView",
                                                              receiver: String = "holder"): FunSpec.Builder {
        if (adapterData.attachItemViewClickListener) {
            addStatement("$receiver.$viewBindingName.setOnClickListener {")
            addStatement("if (holder.adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION){")
            addStatement("$itemClickListener?.forItem(holder.adapterPosition, getItem(holder.adapterPosition), it)")
            addStatement("}")
            addStatement("}")
            addStatement("\n")
        }
        return this
    }

    private fun FunSpec.Builder.attachOnItemViewLongClickListener(longClickListener: String = "onLongItemViewClickListener", viewBindingName: String = "itemView",
                                                                  receiver: String = "holder"): FunSpec.Builder {
        if (adapterData.attachItemViewLongClickListener) {
            addStatement("$receiver.$viewBindingName.setOnLongClickListener {")
            addStatement("if (holder.adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION){")
            addStatement("$longClickListener?.forItem(holder.adapterPosition, getItem(holder.adapterPosition), it)")
            addStatement("}")
            addStatement("true")
            addStatement("}")
        }
        return this
    }

    private fun FunSpec.Builder.attachOnBoundClickListeners(): FunSpec.Builder {
        adapterData.clickListeners.asSequence().forEach {
            when (it.clickListenerType) {
                ClickListenerType.CLICK -> {
                    attachOnItemViewClickListener(it.itemClickListener, it.viewBindingName, it.receiver)
                }
                ClickListenerType.LONG_CLICK -> {
                    attachOnItemViewLongClickListener(it.longClickListener, it.viewBindingName, it.receiver)
                }
                else -> {
                }
            }
        }

        return this
    }


    fun build(): TypeSpec =
            TypeSpec.classBuilder(adapterFileName)
                    .generateSuperClass()
                    .generateOnCreateViewHolder()
                    .generateOnBindViewHolder()
                    .addViewHolderClass()
                    .attachItemViewClickListener()
                    .attachLongItemViewClickListener()
                    .attachAdditionalListeners()
                    .addClickInterface()
                    .build()


    private fun TypeSpec.Builder.attachAdditionalListeners(): TypeSpec.Builder {

        adapterData.clickListeners.asSequence().forEach {
            when (it.clickListenerType) {
                ClickListenerType.LONG_CLICK -> {
                    addClickListenerWithType(it.longClickListener)
                }
                ClickListenerType.CLICK -> {
                    addClickListenerWithType(it.itemClickListener)
                }
                else -> {
                }
            }
        }

        return this
    }

    private fun TypeSpec.Builder.addClickListenerWithType(type: String) {
        addProperty(
                PropertySpec.builder(type, ClassName("",
                        "forItemClickListener")
                        .copy(nullable = true))
                        .mutable()
                        .initializer("null")
                        .build())
    }

    private fun TypeSpec.Builder.attachItemViewClickListener(): TypeSpec.Builder {
        if (adapterData.attachItemViewClickListener) {
            addProperty(
                    PropertySpec.builder("onItemViewClickListener", ClassName("",
                            "forItemClickListener")
                            .copy(nullable = true))
                            .mutable()
                            .initializer("null")
                            .build())
        }
        return this
    }

    private fun TypeSpec.Builder.attachLongItemViewClickListener(): TypeSpec.Builder {
        if (adapterData.attachItemViewLongClickListener) {
            addProperty(
                    PropertySpec.builder("onLongItemViewClickListener", ClassName("",
                            "forItemClickListener")
                            .copy(nullable = true))
                            .mutable()
                            .initializer("null")
                            .build())
        }
        return this

    }


    private fun TypeSpec.Builder.generateOnCreateViewHolder(): TypeSpec.Builder =
            addFunction(FunSpec.builder("onCreateViewHolder")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("parent", ClassName("android.view", "ViewGroup"))
                    .addParameter("viewType", INT)
                    .returns(viewHolderQualifiedClassName)
                    .addStatement("val inflater = android.view.LayoutInflater.from(parent.context)")
                    .addStatement("val binding = ${adapterData.viewBindingPackageName}.${adapterData.viewBindingType}.inflate(inflater, parent, false)")
                    .addStatement("val holder =  $viewHolderName(binding)")
                    .attachOnItemViewClickListener()
                    .attachOnItemViewLongClickListener()
                    .attachOnBoundClickListeners()
                    .addStatement("return holder")
                    .build())


    private fun TypeSpec.Builder.generateSuperClass(): TypeSpec.Builder = superclass(
            ClassName("androidx.recyclerview.widget", "ListAdapter")
                    .parameterizedBy(modelClassName, viewHolderQualifiedClassName))
            .addSuperclassConstructorParameter(adapterData.getDiffUtil)


    private fun TypeSpec.Builder.addViewHolderClass(): TypeSpec.Builder {
        if (customViewHolder == null) {
            addType(
                    TypeSpec.classBuilder(viewHolderClassName)
                            .primaryConstructor(FunSpec.constructorBuilder()
                                    .addParameter("binding", ClassName(adapterData.viewBindingPackageName, adapterData.viewBindingType))
                                    .build())
                            .addProperty(PropertySpec.builder(
                                    "binding", ClassName(adapterData.viewBindingPackageName, adapterData.viewBindingType)
                            )
                                    .initializer("binding")
                                    .addModifiers(KModifier.PRIVATE)
                                    .mutable(false)
                                    .build())
                            .addProperty(
                                    PropertySpec.builder("context", ClassName("android.content", "Context"))
                                            .getter(FunSpec.getterBuilder()

                                                    .addStatement("return %L", "binding.root.context")
                                                    .build())
                                            .addModifiers(KModifier.PRIVATE)
                                            .build()
                            )
                            .superclass(ClassName("androidx.recyclerview.widget.RecyclerView", "ViewHolder"))
                            .addSuperclassConstructorParameter("binding.root")
                            .addBindFunction()
                            .addGetColorFunction()
                            .build()
            )
        }

        return this
    }


    private fun TypeSpec.Builder.generateOnBindViewHolder(): TypeSpec.Builder =
            addFunction(FunSpec.builder("onBindViewHolder")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("holder", viewHolderQualifiedClassName)
                    .addParameter("position", INT)
                    .addStatement("val item = getItem(position)")
                    .addStatement("holder.bind(item)")
                    .build())

    private fun TypeSpec.Builder.addGetColorFunction() = addFunction(
            FunSpec.builder("getCompatColor")
                    .addParameter("colorRes", INT)
                    .addModifiers(KModifier.PRIVATE)
                    .returns(INT)
                    .apply {
                        addStatement("return androidx.core.content.ContextCompat.getColor(context, colorRes)")
                    }
                    .build()
    )


    private fun TypeSpec.Builder.addBindFunction() = addFunction(FunSpec.builder("bind")
            .addParameter("model", modelClassName)
            .apply {
                addTextBinds(this, adapterData.textBindingData)
                addImageBinds(this, adapterData.imageBindingData)
                addVisibilityBinds(this, adapterData.visibilityBindingData)
                addColorBinds(this, adapterData.colorBindingData)
            }
            .build())

    private fun addColorBinds(builder: FunSpec.Builder, colorBindingData: List<ColorBindingData>) {
        colorBindingData.asSequence().forEach {
            val getCompatColorString = "getCompatColor(model.${it.fieldName})"

            when (it.bindingType) {
                ColorBindingType.TEXT_COLOR -> {
                    builder.addStatement("binding.${it.viewBindingName}.setTextColor($getCompatColorString)")
                }
                ColorBindingType.CARD_VIEW_BACKGROUND -> {
                    builder.addStatement("binding.${it.viewBindingName}.setBackgroundColor($getCompatColorString)")
                }
                ColorBindingType.IMAGE_TINT -> {
                    builder.addStatement("androidx.core.widget.ImageViewCompat.setImageTintList(binding.${it.viewBindingName},android.content.res.ColorStateList.valueOf($getCompatColorString))")
                }
            }
        }
    }

    private fun addVisibilityBinds(builder: FunSpec.Builder, visibilityBindingData: List<VisibilityBindingData>) {
        visibilityBindingData.asSequence().forEach {
            addVisibilityCheck(builder, it.checkPreviousVisibilityToSetNew, it.bindingType, it.viewBindingName, it.fieldName, it.elseClauseSetToTheOppositeVisibility, it.visibleToGone)
        }
    }

    private fun addVisibilityCheck(builder: FunSpec.Builder, checkPreviousVisibilityToSetNew: Boolean, visibility: VisibilityBindingType, viewBindingName: String, fieldName: String, elseClauseSetToTheOppositeVisibility: Boolean, visibleToGone: Boolean) {
        if (checkPreviousVisibilityToSetNew) {
            builder.addStatement("if(binding.${viewBindingName}.visibility != android.view.View.${visibility.name})")
            builder.addStatement("{")
            builder.addStatement("if(model.$fieldName)")
            builder.addStatement("{")
            builder.addStatement("binding.${viewBindingName}.visibility = android.view.View.${visibility}")
            builder.addStatement("}")
            if (elseClauseSetToTheOppositeVisibility) {
                builder.addStatement("else {")
                builder.addStatement("binding.${viewBindingName}.visibility = android.view.View.${getOppositeVisibility(visibility, visibleToGone)}")
                builder.addStatement("}")
            }
            builder.addStatement("}")
        } else {
            builder.addStatement("if(model.$fieldName)")
            builder.addStatement("{")
            builder.addStatement("binding.${viewBindingName}.visibility = android.view.View.${visibility}")
            builder.addStatement("}")
            if (elseClauseSetToTheOppositeVisibility) {
                builder.addStatement("else {")
                builder.addStatement("binding.${viewBindingName}.visibility = android.view.View.${getOppositeVisibility(visibility, visibleToGone)}")
                builder.addStatement("}")
            }
        }
    }

    private fun getOppositeVisibility(visibility: VisibilityBindingType, visibleToGone: Boolean): String {
        return when (visibility) {
            VisibilityBindingType.GONE -> {
                VisibilityBindingType.VISIBLE
            }
            VisibilityBindingType.INVISIBLE -> {
                VisibilityBindingType.VISIBLE
            }

            VisibilityBindingType.VISIBLE -> {
                if (visibleToGone) VisibilityBindingType.GONE else VisibilityBindingType.VISIBLE
            }
            else -> {
                VisibilityBindingType.VISIBLE
            }
        }.name
    }

    private fun addImageBinds(builder: FunSpec.Builder, imageBindingData: List<ImageBindingData>) {

        imageBindingData.asSequence().forEach {
            builder.apply {
                addStatement("com.bumptech.glide.Glide.with(context)")
                addStatement(".load(model.${it.fieldName})")
                addStatement(".diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.${it.cachingStrategyImage.name})")
                appendTransformationType(builder, it.transformationType)
                if (it.errorRes != -1) {
                    addStatement(".error(${it.errorRes})")
                }
                if (it.placeHolderRes != -1) {
                    addStatement(".placeholder(${it.placeHolderRes})")
                }
                addStatement(".into(binding.${it.viewBindingName})")
            }
        }
    }

    private fun appendTransformationType(builder: FunSpec.Builder, transformationType: ImageTransformationType){
        when(transformationType){
            ImageTransformationType.CENTER_CROP -> {
                builder.addStatement(".centerCrop()")
            }
            ImageTransformationType.CENTER_INSIDE -> {
                builder.addStatement(".centerInside()")
            }
            ImageTransformationType.CIRCLE_CROP -> {
                builder.addStatement(".circleCrop()")
            }
            ImageTransformationType.FIT_CENTER -> {
                builder.addStatement(".fitCenter()")
            }
            else->{}
        }
    }

    private fun addTextBinds(builder: FunSpec.Builder, textBindingData: List<TextBindingData>) {
        textBindingData.asSequence().forEach {
            when (it.bindingType) {
                TextBindingType.STRING -> {
                    builder.addStatement("binding.${it.viewBindingName}.text = model.${it.fieldName}.toString()")
                }
                TextBindingType.STRING_RES -> {
                    builder.addStatement("binding.${it.viewBindingName}.text = context.getString(model.${it.fieldName})")
                }
            }
        }
    }

    private fun TypeSpec.Builder.addClickInterface(): TypeSpec.Builder {
        val defaultClickListenersCondition = adapterData.attachItemViewClickListener || adapterData.attachItemViewLongClickListener
        val clickListenersCondition = !adapterData.clickListenersConditionAreAllNone

        if (defaultClickListenersCondition || clickListenersCondition) {
            addType(TypeSpec.funInterfaceBuilder("forItemClickListener")
                    .addFunction(
                            FunSpec.builder("forItem")
                                    .addParameter("position", INT)
                                    .addParameter("item", ClassName(adapterData.packageName, adapterData.pojoModelName))
                                    .addParameter("view", ClassName("android.view", "View"))
                                    .addModifiers(KModifier.ABSTRACT)
                                    .build())
                    .build())
        }

        return this
    }


}









