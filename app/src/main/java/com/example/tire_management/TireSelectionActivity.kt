package com.example.tire_management

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast // Import Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONObject
import java.util.Random

// Data class to hold contact information
data class Contact(val name: String, val phoneNumber: String)

class TireSelectionActivity : AppCompatActivity() {

    private lateinit var spinnerTireSize: Spinner
    private lateinit var spinnerTireBrand: Spinner
    private lateinit var recyclerViewTireImages: RecyclerView
    private lateinit var tvTireDescription: TextView
    private lateinit var btnBuyTire: Button
    private lateinit var tvContactUsTitle: TextView
    private lateinit var llContactNumbersContainer: LinearLayout

    private lateinit var tireImageAdapter: TireImageAdapter

    private var selectedTireSize: String = ""
    private var selectedTireBrand: String = ""

    private companion object {
        private const val TAG = "TireSelectionActivity"
    }

    private val tireDataMap:
            MutableMap<String, MutableMap<String, MutableList<Pair<String, String>>>> = mutableMapOf()

    private val defaultNoImage: String = "https://drive.google.com/uc?export=download&id=1mm8op2iNmwINvU1qdXMhgcjHV4mAzBUV"

    // Fixed list of contact persons and their phone numbers
    private val contactList = listOf(
        Contact("آقای مرتضی شیرازی", "09129058898"),
        Contact("آقای کامران پارسا", "09122493874"),
        Contact("آقای محمد بختیاری", "09122432507"),
        Contact("آقای پژمان فلاحی", "09122263179"),
        Contact("آقای حمیدرضا شیرازی", "09127052936")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tire_selection)

        spinnerTireSize = findViewById(R.id.spinner_tire_size)
        spinnerTireBrand = findViewById(R.id.spinner_tire_brand)
        recyclerViewTireImages = findViewById(R.id.recycler_view_tire_images)
        tvTireDescription = findViewById(R.id.tv_tire_description)
        btnBuyTire = findViewById(R.id.btn_buy_tire)
        tvContactUsTitle = findViewById(R.id.tv_contact_us_title)
        llContactNumbersContainer = findViewById(R.id.ll_contact_numbers_container)

        parseTireDataFromJson()
        Log.d(TAG, "Full tireDataMap after parsing: $tireDataMap")

        recyclerViewTireImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        tireImageAdapter = TireImageAdapter(emptyList())
        recyclerViewTireImages.adapter = tireImageAdapter

        val uniqueSizes = tireDataMap.keys.sorted().toTypedArray()
        Log.d(TAG, "Unique Sizes for spinner: ${uniqueSizes.contentToString()}")
        val sizeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, uniqueSizes)
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTireSize.adapter = sizeAdapter

        val allBrands = tireDataMap.values.flatMap { it.keys }.toSet().sorted().toTypedArray()
        Log.d(TAG, "All Brands for initial spinner: ${allBrands.contentToString()}")
        val brandAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, allBrands)
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTireBrand.adapter = brandAdapter

        spinnerTireSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTireSize = parent.getItemAtPosition(position).toString()
                Log.d(TAG, "Size selected: $selectedTireSize")
                filterBrandsForSelectedSize()
                updateTireDisplay()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedTireSize = ""
                Log.d(TAG, "Nothing selected for size.")
                filterBrandsForSelectedSize()
                updateTireDisplay()
            }
        }

        spinnerTireBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTireBrand = parent.getItemAtPosition(position).toString()
                Log.d(TAG, "Brand selected: $selectedTireBrand")
                updateTireDisplay()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedTireBrand = ""
                Log.d(TAG, "Nothing selected for brand.")
                updateTireDisplay()
            }
        }

        if (uniqueSizes.isNotEmpty()) {
            selectedTireSize = uniqueSizes[0]
            spinnerTireSize.setSelection(0)
            Log.d(TAG, "Initial selected size: $selectedTireSize")
            filterBrandsForSelectedSize()
            updateTireDisplay()
        } else {
            Log.w(TAG, "No unique sizes found after parsing JSON data.")
        }

        btnBuyTire.setOnClickListener {
            displayContactNumbers() // Call new function to display all contacts
        }
    }

    /**
     * Parses the tire data from the hardcoded JSON string.
     * Correctly converts Google Drive 'view' links to 'download' links.
     */
    private fun parseTireDataFromJson() {
        val jsonString = """
[
  {
    "SIZE ": "1200R24",
    "BRAND ": "grandstone ",
    "برند ": "گرندستون ",
    "لینک عکس": "https://drive.google.com/file/d/1o_L7vbS2Udeu26zC_mwCwstzcHDCoCp-/view?usp=sharing",
    "توضیحات عکس": "گرندستون معدنی همراه با تیوپ ونوارکامل\nباگارانتی معتبر شرکتی و کارت"
  },
  {
    "SIZE ": "1200R24",
    "BRAND ": "grandstone ",
    "برند ": "گرندستون ",
    "لینک عکس": "https://drive.google.com/file/d/1_tSIF2ulGVC1laOkxzX8_nnIZzVMkq-Z/view?usp=sharing",
    "توضیحات عکس": "سوپرگرندستون گل جلو۲۲لا\nهمراه باتیوپ ونوارباوزن ۹۶کیلوگرم \n۱۰کیلوسنگین ترازلاستیک های۲۰لا\nباگارانتی ۴ساله ایمن تایرزاگروس"
  },
  {
    "SIZE ": "1200R24",
    "BRAND ": "EFIIPLUS",
    "برند ": "افی پلاس ",
    "لینک عکس": "https://drive.google.com/file/d/16qh3QE5TBuyuJPZ6cwQVJlnINVc2yK3r/view?usp=sharing",
    "توضیحات عکس": "افی پلاس گل کامل معدنی ۲۴ لا ‌با عرض ۲۶ سانتیمتر ۲ سانتیمتر پهن تر از همه لاستیک های چینی ووزن ۱۱۰ کیلوگرم برای دستگاههایی که تا 38تن بارحمل می کنند\nهمراه با تیوپ ونوار کامل باگارانتی ۵ ساله شرکت ایمن تایر زاگرس\nقابل مقایسه با هیچ برندچینی نمی باشد."
  },
  {
    "SIZE ": "1200R24",
    "BRAND ": "TOWNHALL",
    "برند ": "تانهال ",
    "لینک عکس": "https://drive.google.com/file/d/1jxEEQ9OyLyn-1QcJ72ffkf6dLMiaNjn_/view?usp=sharing",
    "توضیحات عکس": "تانهال (برنداروپایی کارخانه ووسن)\nساخت کشور چین‌ بابالاترین وزن وکیفیت نسبت به برندهای تولیدی در این کارخانه  همراه باتیوپ ونوارکامل معدنی باگارانتی ۴ساله شرکت ایمن طایر زاگرس "
  },
  {
    "SIZE ": "1200R24",
    "BRAND ": "TOWNHALL",
    "برند ": "تانهال ",
    "لینک عکس": "https://drive.google.com/file/d/1kpwHU1RqdWSF40TgE2GL9doKtjwGcrVy/view?usp=sharing",
    "توضیحات عکس": "تانهال (برنداروپایی کارخانه ووسن)\nگل جلو بریجستونی \nساخت کشور چین‌ بابالاترین وزن وکیفیت نسبت به برندهای تولیدی دراین کارخانه همراه باتیوپ ونوارکامل چهارخط‌ با گارانتی ۴ساله شرکت ایمن طایر زاگرس "
  },
  {
    "SIZE ": "1200R24",
    "BRAND ": "TOWNHALL",
    "برند ": "تانهال ",
    "لینک عکس": "https://drive.google.com/file/d/18mds_UkBROqRMp9TPNR10OXC3GzNYU9X/view?usp=sharing",
    "توضیحات عکس": "تانهال (برنداروپایی کارخانه ووسن)\nگل جلو آپولویی\nساخت کشور چین‌ بابالاترین وزن وکیفیت نسبت به برندهای تولیدی دراین کارخانه  همراه باتیوپ ونوارکامل چهارخط‌ با گارانتی ۴ساله شرکت ایمن طایر زاگرس \n"
  },
  {
    "SIZE ": "1200R24",
    "BRAND ": "TOWNHALL",
    "برند ": "تانهال ",
    "لینک عکس": "https://drive.google.com/file/d/1C0gr0RWYSu1e3aNYgMBMCOy0hWXJTwS3/view?usp=sharing",
    "توضیحات عکس": "تانهال (برنداروپایی کارخانه ووسن)\nگل جلو چهارخط \nساخت کشور چین‌ بابالاترین وزن وکیفیت نسبت به برندهای تولیدی دراین کارخانه همراه باتیوپ ونوارکامل چهارخط‌ با گارانتی ۴ساله شرکت ایمن طایر زاگرس \n"
  },
  {
    "SIZE ": "315/80R22.5",
    "BRAND ": "EFIIPLUS",
    "برند ": "افی پلاس ",
    "لینک عکس": "https://drive.google.com/file/d/1gV1XaTIOtu81b_NzjSQ8_OPcBQDJuzmv/view?usp=sharing",
    "توضیحات عکس": "افی پلاس (تکینگ) ۲۰ لا \nبرند معتبر دنیا ساخت کشورچین باعرض۲۸سانتیمتر گل عقب مناسب برای ماشین های باری وکمپرسی ومیکسر وتریلی عمق اج و وزن ۲۰درصدبالاترازهمه برندهای موجود دربازار \nباگارانتی۴ساله ایمن طایر زاگرس \n"
  },
  {
    "SIZE ": "315/80R22.5",
    "BRAND ": "EFIIPLUS",
    "برند ": "افی پلاس ",
    "لینک عکس": "https://drive.google.com/file/d/1vxXiddYynrqbm6dMkmf5H2lZk2qtyna/view?usp=sharing",
    "توضیحات عکس": "افی پلاس گل عقب۲۲لا بهترین برند چینی و غیر قابل مقایسه با همه برند های چینی با وزن ۷۵کیلو گرم \nبا عرض ۲۸ سانت \n  با کارکرد بسیار بالا با گارانتی ۵ساله شرکت ایمن طایر زاگرس"
  },
  {
    "SIZE ": "315/80R22.5",
    "BRAND ": "EFIIPLUS",
    "برند ": "افی پلاس ",
    "لینک عکس": "https://drive.google.com/file/d/1Eoyx2XMVIdBJZ18EDL4NQu5cWFvdkOJ-/view?usp=sharing",
    "توضیحات عکس": "افی پلاس گل جلو۲۲لا بهترین برند چینی و غیر قابل مقایسه با همه برند های چینی با وزن ۷۳کیلو گرم\nبا عرض ۲۷ سانت \n  با کارکرد بسیار بالا با گارانتی ۵ساله شرکت ایمن طایر زاگرس \n"
  },
  {
    "SIZE ": "315/80R22.5",
    "BRAND ": "grandstone ",
    "برند ": "گرندستون ",
    "لینک عکس": "https://drive.google.com/file/d/1_tK8lCsyZCbY_q2xZ1r2fVB5yFiHL26i/view?usp=sharing",
    "توضیحات عکس": "گل معدنی  ساخت کشور چین \nاز بهترین برندهای  موجود در ایران \n  باگارانتی ۴ساله شرکت ایمن تایر زاگرس"
  },
  {
    "SIZE ": "315/80R22.5",
    "BRAND ": "grandstone ",
    "برند ": "گرندستون ",
    "لینک عکس": "https://drive.google.com/file/d/1XfMT8cKtROl53JrgLyQCxm77O6px0-cI/view?usp=sharing",
    "توضیحات عکس": "گرندستون گل عقب   ساخت کشور چین \nاز بهترین برندهای  موجود در ایران \n  باگارانتی ۴ساله شرکت ایمن تایر زاگرس"
  },
  {
    "SIZE ": "315/80R22.5",
    "BRAND ": "grandstone ",
    "برند ": "گرندستون ",
    "لینک عکس": "https://drive.google.com/file/d/1CxY1IhoStI0kmb1kC-MHu9f4kpuS6sJI/view?usp=sharing",
    "توضیحات عکس": "گرندستون گل جلو ساخت کشور چین \nاز بهترین برندهای  موجود در ایران \n باگارانتی ۴ساله شرکت ایمن تایر زاگرس"
  },
  {
    "SIZE ": "385/65R22.5",
    "BRAND ": "EFIIPLUS",
    "برند ": "افی پلاس ",
    "لینک عکس": "https://drive.google.com/file/d/1kF5YR5vmanhQOVt9hLowZ0PHQ0vkmVu9/view?usp=sharing",
    "توضیحات عکس": "افی پلاس \nساخت کشورچین ۲۴ لا \n \nباگارانتی ۴ساله شرکت ایمن تایر"
  },
  {
    "SIZE ": "235/75R17/5",
    "BRAND ": "EFIIPLUS",
    "برند ": "افی پلاس ",
    "لینک عکس": "https://drive.google.com/file/d/1hQfvRYSKRxtufAW8OBC3qkv1TrAjgRDT/view?usp=sharing",
    "توضیحات عکس": "افی پلاس   گل جلو ( ۱۶ لا ) \nساخت کشور چین    \nباگارانتی ۴ساله شرکت ایمن تایر"
  },
  {
    "SIZE ": "235/75R17/5",
    "BRAND ": "grandstone ",
    "برند ": "گرندستون ",
    "لینک عکس": "https://drive.google.com/file/d/15gT7IMUMi3KnKAxoR3l_tBSpUtTt8mZf/view?usp=sharing",
    "توضیحات عکس": "گرندستون گل عقب \n۱۸ لا \nبا کارت طلایی گارانتی ۵ ساله که هنگام خرید از فروشگاه دریافت میکنید"
  },
  {
    "SIZE ": "235/75R17/5",
    "BRAND ": "grandstone ",
    "برند ": "گرندستون ",
    "لینک عکس": "https://drive.google.com/file/d/1bkfN4rn3qGuaA82Es_6NvrZPQj45IC-o/view?usp=sharing",
    "توضیحات عکس": "گرندستون گل جلو \nبا کارت طلایی گارانتی ۵ ساله که هنگام خرید از فروشگاه دریافت میکنید"
  },
  {
    "SIZE ": "215/75R17/5",
    "BRAND ": "grandstone ",
    "برند ": "گرندستون ",
    "لینک عکس": "https://drive.google.com/file/d/1ex_6f8sVrHM-yrGMAhF8nKDuxEVYGWH6/view?usp=sharing",
    "توضیحات عکس": "گرندستون 18 لایه ساخت کشور چین با گارانتی 4 ساله"
  },
  {
    "SIZE ": "9/5R17.5",
    "BRAND ": "grandstone ",
    "برند ": "گرندستون ",
    "لینک عکس": "https://drive.google.com/file/d/1fHH2h2fC2CUKxqd0EC_tbm3PAgwNmWl5/view?usp=sharing",
    "توضیحات عکس": "گرندستون سیمی ساخت کشورچین ۱۸ لا سوپر\nدارای گارانتی چهار ساله شرکت ایمن تایرزاگرس "
  },
  {
    "SIZE ": "8.25R16",
    "BRAND ": "grandstone ",
    "برند ": "گرندستون ",
    "لینک عکس": "https://drive.google.com/file/d/1n0E5-klJJvZ8VFgg28iqXAsEKkKLN47o/view?usp=sharing",
    "توضیحات عکس": "گرندستون سیمی ساخت کشورچین همراه با تیوپ و نوار قابل استفاده در همه محورها خیلی مقاوم هم  تراز لاستیک های چهارخط گل عقب \nدارای گارانتی چهارساله شرکت ایمن تایر زاگرس "
  },
  {
    "SIZE ": "7/50R16",
    "BRAND ": "EFIIPLUS",
    "برند ": "افی پلاس ",
    "لینک عکس": "https://drive.google.com/file/d/1r6J4AumceJZ6mipygIzFUYYBgBp92OM4/view?usp=sharing",
    "توضیحات عکس": "افی پلاس ساخت کشورچین همراه با تیوپ ونوار ۱۴لا با عمق و وزن بالا ، قابلیت کارکرد در جاده‌ای آسفالت و خاکی و کوهستانی بهترین برند موجود دردنیا باگارانتی ۵ساله شرکت ایمن تایر زاگرس \n"
  },
  {
    "SIZE ": "700R16",
    "BRAND ": "EFIIPLUS",
    "برند ": "افی پلاس ",
    "لینک عکس": "https://drive.google.com/file/d/1pdtHw0CXlhQvpT74XEc67nlCCKdCCmjn/view?usp=sharing",
    "توضیحات عکس": "افی پلاس سیمی ۱۴لا \nتیوپلس \nباگارانتی ۴ساله"
  }
]
"""
        try {
            val jsonArray = JSONArray(jsonString)
            Log.d(TAG, "Parsing JSON array with ${jsonArray.length()} items.")

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                // Extract and trim values. Use .optString to handle missing keys gracefully, though not expected here.
                // IMPORTANT: Note the space in key names like "SIZE " and "BRAND "
                val size = jsonObject.optString("SIZE ").trim()
                val brandPersian = jsonObject.optString("برند ").trim()
                val imageUrl = jsonObject.optString("لینک عکس").trim()
                val description = jsonObject.optString("توضیحات عکس").trim().removeSurrounding("\"")

                // Only add data if size and brand are not empty
                if (size.isNotEmpty() && brandPersian.isNotEmpty()) {
                    // Corrected Google Drive 'view' link to 'download' link conversion
                    val convertedImageUrl = if (imageUrl.startsWith("https://drive.google.com/file/d/")) {
                        val id = imageUrl.substringAfterLast("/d/").substringBefore("/view")
                        "https://drive.google.com/uc?export=download&id=$id"
                    } else {
                        imageUrl // Use as is if not a Google Drive view link (e.g., if it's already a direct link)
                    }

                    val brandMap = tireDataMap.getOrPut(size) { mutableMapOf() }
                    val imageList = brandMap.getOrPut(brandPersian) { mutableListOf() }
                    imageList.add(Pair(convertedImageUrl, description))
                    Log.d(TAG, "Added: Size=$size, Brand=$brandPersian, ImageURL=$convertedImageUrl")
                } else {
                    Log.w(TAG, "Skipping empty or malformed JSON entry at index $i: Size='$size', Brand='$brandPersian'")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON data: ${e.message}", e)
        }
    }

    /**
     * Filters the brands spinner based on the currently selected tire size.
     */
    private fun filterBrandsForSelectedSize() {
        val availableBrandsForSize = tireDataMap[selectedTireSize]?.keys?.sorted()?.toTypedArray() ?: emptyArray()
        val brandAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, availableBrandsForSize)
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTireBrand.adapter = brandAdapter

        // If the previously selected brand is not available for the new size, select the first available brand
        if (availableBrandsForSize.isNotEmpty() && !availableBrandsForSize.contains(selectedTireBrand)) {
            selectedTireBrand = availableBrandsForSize[0]
            spinnerTireBrand.setSelection(0)
        } else if (availableBrandsForSize.isEmpty()) {
            selectedTireBrand = ""
        }
        Log.d(TAG, "Brands for size '$selectedTireSize': ${availableBrandsForSize.contentToString()}")
        Log.d(TAG, "Selected brand after filter: $selectedTireBrand")
    }

    /**
     * Updates the displayed tire images and description based on selected size and brand.
     */
    private fun updateTireDisplay() {
        // Hide previous notification when selections change
        tvContactUsTitle.visibility = View.GONE // Hide title too
        llContactNumbersContainer.visibility = View.GONE // Hide contact container

        val imagesAndDescriptions = tireDataMap[selectedTireSize]?.get(selectedTireBrand)

        val imagesToDisplay = mutableListOf<String>()
        var descriptionToDisplay = getString(R.string.no_description_available)

        if (imagesAndDescriptions != null && imagesAndDescriptions.isNotEmpty()) {
            imagesToDisplay.addAll(imagesAndDescriptions.map { it.first }) // Extract URLs
            descriptionToDisplay = imagesAndDescriptions[0].second // Get description from the first item
            Log.d(TAG, "Displaying ${imagesToDisplay.size} images for $selectedTireBrand ($selectedTireSize). Description: $descriptionToDisplay")
        } else {
            // If no specific data for the size/brand, use the default placeholder image
            imagesToDisplay.add(defaultNoImage)
            descriptionToDisplay = getString(R.string.no_description_available)
            Log.d(TAG, "No specific data for $selectedTireBrand ($selectedTireSize), showing default image.")
        }

        // Update the RecyclerView adapter with the new list of images
        tireImageAdapter.updateImages(imagesToDisplay)

        // Update the description TextView
        tvTireDescription.text = descriptionToDisplay
    }

    /**
     * Displays all fixed contact numbers as clickable TextViews.
     */
    private fun displayContactNumbers() {
        llContactNumbersContainer.removeAllViews() // Clear any existing views
        tvContactUsTitle.visibility = View.VISIBLE // Show "Contact Us" title
        llContactNumbersContainer.visibility = View.VISIBLE // Show container

        for (contact in contactList) {
            val contactTextView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.contact_item_margin_bottom) // Add some margin
                }
                text = "${contact.name}: ${contact.phoneNumber}"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(resources.getColor(R.color.md_theme_primary, theme)) // Use primary color for links
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.contact_item_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.contact_item_padding_vertical),
                    resources.getDimensionPixelSize(R.dimen.contact_item_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.contact_item_padding_vertical)
                )
                setBackgroundResource(R.drawable.rounded_edittext) // Use existing rounded background
                gravity = View.TEXT_ALIGNMENT_CENTER // Center the text
                isClickable = true // Make it explicitly clickable
                isFocusable = true // Make it explicitly focusable
                setOnClickListener {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${contact.phoneNumber}")
                    }
                    if (dialIntent.resolveActivity(packageManager) != null) {
                        startActivity(dialIntent)
                    } else {
                        Log.w(TAG, "No app to handle dial intent for ${contact.phoneNumber}")
                        Toast.makeText(this@TireSelectionActivity, "No app to make calls found.", Toast.LENGTH_SHORT).show() // Show toast
                    }
                }
            }
            llContactNumbersContainer.addView(contactTextView)
        }
    }
}
