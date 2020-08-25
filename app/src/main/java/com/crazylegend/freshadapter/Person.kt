package com.crazylegend.freshadapter


import com.crazylegend.annotations.ViewBindingAdapter
import com.crazylegend.annotations.clickListeners.ClickListenerType
import com.crazylegend.annotations.color.BindColor
import com.crazylegend.annotations.color.ColorBindingType
import com.crazylegend.annotations.image.BindImage
import com.crazylegend.annotations.image.ImageBindingType
import com.crazylegend.annotations.text.BindText
import com.crazylegend.annotations.visibility.BindVisibility
import com.crazylegend.annotations.visibility.VisibilityBindingType
import java.text.MessageFormat
import kotlin.random.Random


/**
 * Created by crazy on 8/15/20 to long live and prosper !
 */

@ViewBindingAdapter("com.crazylegend.annotatedadapter.databinding.ItemviewPersonBinding")
data class Person(
    @BindText("title") val name: String,

    @BindText("content", clickListenerType = ClickListenerType.LONG_CLICK)
        val surname: String

) {

    //if this is true set visibility to invisible, hides all the content with percentage that's of one number
    @BindVisibility("content", VisibilityBindingType.VISIBLE, true, true, true)
    val visibility = surname.substringAfter("%").length == 1

    @BindImage("image", ImageBindingType.REMOTE)
    val image: String
        get() = createRandomImageUrl()

    @BindColor("card", ColorBindingType.CARD_VIEW_BACKGROUND)
    val imageColor
        get() = android.R.color.darker_gray


    private fun random(start: Int, end: Int): Int {
        return start + Random.nextInt(end - start + 1)
    }

    private fun createRandomImageUrl(): String {

        val landscape = Random.nextBoolean()
        val endpoint = Random.nextBoolean()

        val width = random(300, 400)
        val height = random(200, 300)

        return MessageFormat.format(
                if (endpoint)
                    "https://lorempixel.com/{0}/{1}/"
                else
                    "https://picsum.photos/{0}/{1}/",
                if (landscape) width else height, if (landscape) height else width
        )
    }


}