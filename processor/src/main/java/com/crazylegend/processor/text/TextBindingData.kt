package com.crazylegend.processor.text

import com.crazylegend.annotations.clickListeners.ClickListenerType
import com.crazylegend.annotations.text.TextBindingType
import com.crazylegend.processor.codegen.BindingData


/**
 * Created by crazy on 8/15/20 to long live and prosper !
 */
internal data class TextBindingData(
        override val fieldName: String,
        override val viewBindingName: String,
        val bindingType: TextBindingType,
        override val clickListenerType: ClickListenerType
) : BindingData
