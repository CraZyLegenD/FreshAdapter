# Fresh adapter
### Recycler view adapter generator

[![](https://jitpack.io/v/CraZyLegenD/FreshAdapter.svg)](https://jitpack.io/#CraZyLegenD/FreshAdapter)
 [![Kotlin](https://img.shields.io/badge/Kotlin-1.4.0-blue.svg)](https://kotlinlang.org) [![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/guide/)
 [![sad](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?url=https%3A%2F%2Fgithub.com%2FCraZyLegenD%2FFreshAdapter&text=Check%20out%20this%20RecyclerView%20adapter%20generator%20library%20and%20don%27t%20write%20a%20single%20RecyclerView%20adapter%20anymore)
![API](https://img.shields.io/badge/Min%20API-21-green)
![API](https://img.shields.io/badge/Compiled%20API-30-green)

## Features

- [x] Basic ListAdapter generation
- [x] Ability for itemView click or long itemView click listener support
- [x] Item click or long item click listener support for each view
- [x] Default diff util generation or ability to provide a custom one
- [x] View holder generation or ability to provide a custom one
- [x] Incremental processing
- [x] Using the latest view binding library by Google
- [x] Ability to bind text, image (using Glide), color, visibility and add customizations
- [ ] Suggest something?

## Usage
1. Add JitPack to your project build.gradle

```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
   }
}
```

2. Add the dependencies in the application build.gradle

```gradle
dependencies {
    def freshAdapterVersion = "1.0.6" //or check jitpack for latest version
    //annotations which will be used to mark your classes for processing
    implementation "com.github.CraZyLegenD.FreshAdapter:annotations:$freshAdapterVersion"
    //where the magic happens
    kapt "com.github.CraZyLegenD.FreshAdapter:processor:$freshAdapterVersion"
  }
```

3. In your application build.gradle add
```gradle
   apply plugin: 'kotlin-kapt'
```
```gradle
   compileOptions {
        sourceCompatibility = 1.8
        targetCompatibility = 1.8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        viewBinding = true
    }
```


5. Below is a simple usage example for a bit more further than the simple see the sample app, 
for more advanced and customizing usage, visit the [Wiki](https://github.com/CraZyLegenD/FreshAdapter/wiki)

Annotate your pojo class model with @ViewBindingAdapter
```kotlin
@ViewBindingAdapter(
    viewBinding = ItemviewPersonBinding::class, //sets the binding that's gonna be a constructor for the view holder
    attachItemViewClickListener = true, //whether to generate click listener on itemView click
    attachItemViewLongClickListener = true //same as itemViewClick listener but long click,
    generateViewBindingStaticNames = true //creates static object for your view binding so that you shouldn't paste the names around, you need to build the project so that this can be generated, you can use viewName = "" at the beginning after this is generated replace with the names you'll use to bind
)
data class Person
```

```kotlin
@ViewBindingAdapter(
    viewBinding = ItemviewPersonBinding::class,
    attachItemViewClickListener = true,
    attachItemViewLongClickListener = true
)
data class Person(

    /*sets the variable as a text to a TextView binding.title.text = name*/
    @BindText(viewName = "title") 
    val name: String,

    /*Same as above but it generates long click listener for binding.content*/
    @BindText(viewName = "content", clickListenerType = ClickListenerType.LONG_CLICK)
    val surname: String
)
```
This simple usage will generate
```kotlin
package com.crazylegend.freshadapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.crazylegend.freshadapter.databinding.ItemviewPersonBinding

class PersonAdapter : ListAdapter<Person, PersonAdapter.ViewHolder>(object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Person>() {
  override fun areItemsTheSame(oldItem: Person, newItem: Person) = oldItem == newItem
  override fun areContentsTheSame(oldItem: Person, newItem: Person) = oldItem == newItem
}) {
    var onItemViewClickListener: forItemClickListener? = null

    var onLongItemViewClickListener: forItemClickListener? = null

    var surnameLongClickListener: forItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonAdapter.ViewHolder {
        val inflater = android.view.LayoutInflater.from(parent.context)
        val binding = com.crazylegend.freshadapter.databinding.ItemviewPersonBinding.inflate(inflater, parent, false)
        val holder = ViewHolder(binding)
        holder.itemView.setOnClickListener {
            if (holder.adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                onItemViewClickListener?.forItem(holder.adapterPosition, getItem(holder.adapterPosition), it)
            }
        }
        holder.itemView.setOnLongClickListener {
            if (holder.adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                onLongItemViewClickListener?.forItem(holder.adapterPosition, getItem(holder.adapterPosition), it)
            }
            true
        }
        binding.content.setOnLongClickListener {
            if (holder.adapterPosition != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                surnameLongClickListener?.forItem(holder.adapterPosition, getItem(holder.adapterPosition), it)
            }
            true
        }
        return holder
    }

    override fun onBindViewHolder(holder: PersonAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ViewHolder(private val binding: ItemviewPersonBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        fun bind(model: Person) {
            binding.title.text = model.name.toString()
            binding.content.text = model.surname.toString()
        }
    }

    fun interface forItemClickListener {
        fun forItem(position: Int, item: Person, view: View)
    }
}
```
Then inside your Fragment/Activity you can use
```kotlin
private val personAdapter by lazy {
        PersonAdapter()
    }
    .
    .
    .
 override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      
        binding.recycler.adapter = personAdapter
        personAdapter.submitList(personList)
        
        personAdapter.onItemViewClickListener = PersonAdapter.forItemClickListener { position, item, view -> Log.d("CLICKED AT $position", "LONG ITEM VIEW CLICK ${item.name} at view ${view.id}") }
        
        personAdapter.surnameLongClickListener = PersonAdapter.forItemClickListener { position, item, view -> Log.d("CLICKED AT $position", "LONG SURNAME VIEW CLICK ${item.surname} at view ${view.id}") }
    }
```


## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[GPL 3.0](https://www.gnu.org/licenses/gpl-3.0.en.html)
