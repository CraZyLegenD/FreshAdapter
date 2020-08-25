package com.crazylegend.processor.color

import com.crazylegend.annotations.clickListeners.ClickListenerType
import com.crazylegend.annotations.color.ColorBindingType
import com.crazylegend.processor.codegen.BindingData


/**
 * Created by crazy on 8/15/20 to long live and prosper !
 */
internal data class ColorBindingData(
        override val fieldName: String,
        override val viewBindingName: String,
        val bindingType: ColorBindingType,
        override val clickListenerType: ClickListenerType

) : BindingData
