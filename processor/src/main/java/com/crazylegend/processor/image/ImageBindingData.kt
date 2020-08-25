package com.crazylegend.processor.image

import com.crazylegend.annotations.clickListeners.ClickListenerType
import com.crazylegend.annotations.image.ImageBindingType
import com.crazylegend.processor.codegen.BindingData


/**
 * Created by crazy on 8/15/20 to long live and prosper !
 */
internal data class ImageBindingData(
        override val fieldName: String,
        override val viewBindingName: String,
        val bindingType: ImageBindingType,
        override val clickListenerType: ClickListenerType
) : BindingData
