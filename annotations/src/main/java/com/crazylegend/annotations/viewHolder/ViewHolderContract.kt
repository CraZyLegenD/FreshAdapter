package com.crazylegend.annotations.viewHolder


/**
 * Created by crazy on 8/21/20 to long live and prosper !
 */
interface ViewHolderContract<T> {
    fun bind(model:T)
}