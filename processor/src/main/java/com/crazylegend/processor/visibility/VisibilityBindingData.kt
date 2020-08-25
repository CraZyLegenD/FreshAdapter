package com.crazylegend.processor.visibility

import com.crazylegend.annotations.clickListeners.ClickListenerType
import com.crazylegend.annotations.visibility.VisibilityBindingType
import com.crazylegend.processor.codegen.BindingData


/**
 * Created by crazy on 8/15/20 to long live and prosper !
 */
internal data class VisibilityBindingData(
        override val fieldName: String,
        override val viewBindingName: String,
        val bindingType: VisibilityBindingType,
        val checkPreviousVisibilityToSetNew: Boolean,
        val elseClauseSetToTheOppositeVisibility:Boolean,
        val visibleToGone:Boolean,
        override val clickListenerType: ClickListenerType
) : BindingData
