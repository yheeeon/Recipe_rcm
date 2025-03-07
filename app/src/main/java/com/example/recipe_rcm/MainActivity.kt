package com.example.recipe_rcm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.recipe_rcm.ClovaOCR.Camera_ocr
import com.example.recipe_rcm.databinding.ActivityMainBinding
import com.example.recipe_rcm.Favorite.Heart
import com.example.recipe_rcm.RecipeAPI.Home
import com.example.recipe_rcm.RecipeAPI.List_ingredient
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // BottomNavigationView 설정
        initBottomNavigation(savedInstanceState)
    }

    private fun initBottomNavigation(savedInstanceState: Bundle?) {
        val bottomNavigationView: BottomNavigationView = binding.bottomLayout

        // 처음 화면은 Home 프래그먼트로 설정
        if (savedInstanceState == null) {
            replaceFragment(Home())
        }

        // BottomNavigationView 클릭 리스너 설정
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    replaceFragment(Home())
                    true
                }

                R.id.menu_heart -> {
                    replaceFragment(Heart())
                    true
                }

                R.id.menu_camera -> {
                    replaceFragment(Camera_ocr())
                    true
                }

                R.id.menu_list -> {
                    replaceFragment(List_ingredient())
                    true
                }

                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // 항상 프래그먼트 교체
            .commitNowAllowingStateLoss()
    }
}
