package com.impulse.impulse.activity

import android.os.Bundle
import android.widget.Toast
import com.impulse.impulse.R

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Toast.makeText(this, "Created!", Toast.LENGTH_SHORT).show()
    }
}