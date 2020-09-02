package com.crazylegend.processor.codegen

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * Created by crazy on 9/2/20 to long live and prosper !
 */
class ViewHolderNamesBuilder(private val fieldNames: List<String>, private val viewBindingFileName: String) {

    fun build(): TypeSpec {
        return TypeSpec.objectBuilder(viewBindingFileName)
                .addFieldNames()
                .build()
    }

    private fun TypeSpec.Builder.addFieldNames(): TypeSpec.Builder {
        fieldNames.asSequence().forEach {
            addProperty(PropertySpec.builder(it, String::class)
                    .addModifiers(KModifier.CONST)
                    .initializer("%S", it)
                    .build())
        }
        return this
    }
}


