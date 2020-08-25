package com.crazylegend.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ViewBindingAdapter(val viewBindingPackage: String, val attachItemViewClickListener: Boolean = true, val attachItemViewLongClickListener: Boolean = false)