package com.crazylegend.annotations.image

import com.crazylegend.annotations.clickListeners.ClickListenerType


/**
 * Created by crazy on 8/18/20 to long live and prosper !
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class BindImage(val viewName: String, val imageBindingType: ImageBindingType, val clickListenerType:ClickListenerType = ClickListenerType.NONE)