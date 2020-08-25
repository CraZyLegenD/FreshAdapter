package com.crazylegend.annotations.color

import com.crazylegend.annotations.clickListeners.ClickListenerType


/**
 * Created by crazy on 8/19/20 to long live and prosper !
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class BindColor(val viewName: String, val type: ColorBindingType, val clickListenerType: ClickListenerType = ClickListenerType.NONE)