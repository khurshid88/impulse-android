package uz.impulse.impulse.activity

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import uz.impulse.impulse.R
import uz.impulse.impulse.adapter.IntroPageItemAdapter
import uz.impulse.impulse.databinding.ActivityIntroPageBinding
import uz.impulse.impulse.manager.PrefsManager
import uz.impulse.impulse.model.IntroPageItem


/*
* This activity only will be showed first time users
* */
class IntroPageActivity : uz.impulse.impulse.activity.BaseActivity() {
    private lateinit var binding: ActivityIntroPageBinding
    private var isFirstTime = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.apply {
            viewPager.adapter = IntroPageItemAdapter(context, getItems())
            dotsIndicator.setViewPager2(viewPager)
            btnContinue.setOnClickListener {
                vibrate()
                viewPager.currentItem = ++viewPager.currentItem
            }
            tvSkip.setOnClickListener {
                vibrate()
                viewPager.currentItem = getItems().size - 1
            }
            btnGetStarted.setOnClickListener {
                vibrate()
                saveLoggedState()
                callSignInActivity(this@IntroPageActivity)
                finish()
            }
        }
        applyPageStateChanges()
    }

    private fun saveLoggedState() {
        isFirstTime = false
        PrefsManager.getInstance(context)!!.setBoolean("isFirstTime", isFirstTime)
    }

    private fun applyPageStateChanges() {
        binding.apply {
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    if (position == getItems().size - 1) {
                        btnContinue.visibility = View.GONE
                        btnGetStarted.visibility = View.VISIBLE
                    } else {
                        btnContinue.visibility = View.VISIBLE
                        btnGetStarted.visibility = View.GONE
                    }

                }
            })
        }
    }

    private fun getItems(): ArrayList<IntroPageItem> {
        val items = ArrayList<IntroPageItem>()

        items.add(
            IntroPageItem(
                "save_time.json",
                getString(R.string.str_save_your_time),
                getString(R.string.str_description_time)
            )
        )
        items.add(
            IntroPageItem(
                "advice_of_doctors.json",
                getString(R.string.str_advice_doctors),
                getString(R.string.str_description_doctors)
            )
        )
        items.add(
            IntroPageItem(
                "active_support.json",
                getString(R.string.str_active_support),
                getString(R.string.str_description_support)
            )
        )
        return items
    }
}


