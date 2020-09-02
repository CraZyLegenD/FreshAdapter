package com.crazylegend.annotations.text

import com.crazylegend.annotations.clickListeners.ClickListenerType


/**
 * Created by crazy on 8/18/20 to long live and prosper !
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class BindText(val viewName: String = "", val textBindingType: TextBindingType = TextBindingType.STRING, val clickListenerType: ClickListenerType = ClickListenerType.NONE)