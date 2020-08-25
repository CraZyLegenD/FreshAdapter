package com.crazylegend.processor.utils

import javax.lang.model.element.Element


/**
 * Created by crazy on 8/16/20 to long live and prosper !
 */


val String.getFieldName get() = substring(0, indexOf('$'))

fun <E> List<E>.stringifyList(): String {
    return toString().replace("[", "").replace("]", "").replace(",", "")
}


inline fun <reified BINDER : Annotation, E : Any> Element.bindEnclosedElements(mapToElement: (fieldElementName: String, callback: BINDER) -> E): List<E> {
    return enclosedElements.mapNotNull {
        val bindElement = it.getAnnotation(BINDER::class.java)
        if (bindElement == null) null
        else {
            val elementName = it.simpleName.toString()
            val fieldName = elementName.getFieldName
            mapToElement(fieldName, bindElement)
        }
    }
}

inline fun <T> tryOrNull(block: () -> T): T? = try {
    block()
} catch (e: Exception) {
    null
}