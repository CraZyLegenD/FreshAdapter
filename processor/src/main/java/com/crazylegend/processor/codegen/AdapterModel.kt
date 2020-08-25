package com.crazylegend.processor.codegen

import com.crazylegend.annotations.clickListeners.ClickListenerType
import com.crazylegend.processor.color.ColorBindingData
import com.crazylegend.processor.image.ImageBindingData
import com.crazylegend.processor.text.TextBindingData
import com.crazylegend.processor.visibility.VisibilityBindingData
import com.squareup.kotlinpoet.ClassName


/**
 * Created by crazy on 8/15/20 to long live and prosper !
 */
internal data class AdapterModel(
        val packageName: String,
        val pojoModelName: String,
        val viewBindingPackageName: String,
        val viewBindingType: String,
        private val customDiffUtilClass: String?,
        private val customViewHolderClass: String?,
        val textBindingData: List<TextBindingData>,
        val imageBindingData: List<ImageBindingData>,
        val visibilityBindingData: List<VisibilityBindingData>,
        val colorBindingData: List<ColorBindingData>,
        val attachItemViewClickListener: Boolean,
        val attachItemViewLongClickListener: Boolean
) {

    internal data class CustomViewHolder(
            val viewHolderName: String,
            val viewHolderClassName: ClassName,
            val viewHolderQualifiedClassName: ClassName
    )

    fun generateCustomViewHolder(): CustomViewHolder? {
        return if (customViewHolderClass == null){
            null
        } else {
            val viewHolderName = customViewHolderClass.substringAfterLast(".")
            val viewHolderClassName = ClassName(packageName, viewHolderName)
            val viewHolderQualifiedClassName =  ClassName(packageName, "$pojoModelName.$viewHolderName")
            CustomViewHolder(customViewHolderClass, viewHolderClassName, viewHolderQualifiedClassName)
        }
    }

    val clickListeners
        get() = textBindingData.union(imageBindingData).union(visibilityBindingData)
                .union(colorBindingData).toList()

    val clickListenersConditionAreAllNone
        get() = clickListeners.all {
            it.clickListenerType == ClickListenerType.NONE
        }

    private val oldItemEqualsNewItem get() = "oldItem == newItem"

    val getDiffUtil
        get() = customDiffUtilClass
                ?: """object : androidx.recyclerview.widget.DiffUtil.ItemCallback<$pojoModelName>(){
            
        override fun areItemsTheSame(oldItem: $pojoModelName, newItem: $pojoModelName) = $oldItemEqualsNewItem
        
        override fun areContentsTheSame(oldItem: $pojoModelName, newItem: $pojoModelName) = $oldItemEqualsNewItem
        
    }
    """.trimIndent()
}

