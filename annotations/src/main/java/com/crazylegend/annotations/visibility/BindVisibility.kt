package com.crazylegend.annotations.visibility

import com.crazylegend.annotations.clickListeners.ClickListenerType


/**
 * Created by crazy on 8/18/20 to long live and prosper !
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class BindVisibility(val viewName: String, val visibilityType: VisibilityBindingType, val checkPreviousVisibilityToSetNew: Boolean = false, val elseClauseSetToTheOppositeVisibility: Boolean = false, val visibleToGone:Boolean = false, val clickListenerType: ClickListenerType = ClickListenerType.NONE)