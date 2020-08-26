package com.crazylegend.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ViewBindingAdapter(val viewBinding: KClass<*>, val attachItemViewClickListener: Boolean = true, val attachItemViewLongClickListener: Boolean = false)