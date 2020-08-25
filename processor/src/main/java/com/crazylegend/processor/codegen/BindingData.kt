package com.crazylegend.processor.codegen

import com.crazylegend.annotations.clickListeners.ClickListenerType


/**
 * Created by crazy on 8/18/20 to long live and prosper !
 */
interface BindingData {
    val fieldName: String
    val viewBindingName: String
    val clickListenerType: ClickListenerType

    val longClickListener get() = "${fieldName}LongClickListener"
    val itemClickListener get() = "${fieldName}ItemClickListener"
    val receiver get() = "binding"
}