/*
 * FragmentWrapperActivity.kt
 *
 * Copyright (C) 2012 Eric Butler
 *
 * Authors:
 * Eric Butler <eric@codebutler.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package au.id.micolous.metrodroid.activity

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.LinearLayout

import au.id.micolous.farebot.R

abstract class FragmentWrapperActivity : MetrodroidActivity() {

    protected val fragment: Fragment?
        get() = fragmentManager.findFragmentByTag("fragment")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDisplayHomeAsUpEnabled(true)

        val container = FrameLayout(this)
        container.layoutParams = LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT)
        container.id = R.id.content
        setContentView(container)

        if (fragment == null) {
            val transaction = fragmentManager.beginTransaction()
            transaction.add(R.id.content, createFragment(), "fragment")
            transaction.commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            return true
        }
        return false
    }

    protected abstract fun createFragment(): Fragment
}
