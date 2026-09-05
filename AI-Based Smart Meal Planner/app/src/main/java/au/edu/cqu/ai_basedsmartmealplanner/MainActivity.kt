package au.edu.cqu.ai_basedsmartmealplanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import au.edu.cqu.ai_basedsmartmealplanner.ui.GroceryListFragment
import au.edu.cqu.ai_basedsmartmealplanner.ui.HomeFragment
import au.edu.cqu.ai_basedsmartmealplanner.ui.MealPlanFragment
import au.edu.cqu.ai_basedsmartmealplanner.ui.ProfileFragment
import au.edu.cqu.ai_basedsmartmealplanner.ui.RecipesFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation =
            findViewById<BottomNavigationView>(R.id.bottomNavigation)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        bottomNavigation.setOnItemSelectedListener { item ->

            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_meal_plan -> MealPlanFragment()
                R.id.nav_recipes -> RecipesFragment()
                R.id.nav_grocery -> GroceryListFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> null
            }

            if (fragment != null) {
                loadFragment(fragment)
                true
            } else {
                false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}