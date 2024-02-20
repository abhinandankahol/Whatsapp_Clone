package com.abhinandankahol.whatsappclone

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.abhinandankahol.whatsappclone.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        remoteConfig.setConfigSettingsAsync(configSettings)


        val draw = remoteConfig.getString("toolbarImg")
        Glide.with(this).load(draw).into(object : CustomTarget<Drawable>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                binding.appBarLayout.background = resource
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }

        })





        if (Firebase.auth.currentUser == null) {
            startActivity(Intent(this, NumberActivity::class.java))
            finish()
        }

        viewPager = binding.viewPager
        tabLayout = binding.tabs


        viewPager.adapter =
            ViewPagerAdapter(this@MainActivity, ChatFragment(), StatusFragment(), CallFragment())


        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Chat"
                1 -> tab.text = "Status"
                2 -> tab.text = "Call"
            }
        }.attach()
    }

    companion object {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
            build()
        }

    }
}