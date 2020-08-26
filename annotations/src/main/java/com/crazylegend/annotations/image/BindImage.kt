package com.crazylegend.annotations.image

import com.crazylegend.annotations.clickListeners.ClickListenerType


/**
 * Created by crazy on 8/18/20 to long live and prosper !
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
annotation class BindImage(val viewName: String, val placeHolderRes: Int = -1, val errorRes: Int = -1,
                           val cachingStrategyImage: ImageCacheType = ImageCacheType.NONE,
                           val transformationType: ImageTransformationType = ImageTransformationType.NONE,
                           val clickListenerType: ClickListenerType = ClickListenerType.NONE)