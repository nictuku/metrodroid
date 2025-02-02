/*
 * BaseInstrumentedTestPlatform.kt
 *
 * Copyright 2018-2019 Michael Farrell <micolous+git@gmail.com>
 * Copyright 2019 Google
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
package au.id.micolous.metrodroid.test

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Build
import android.text.Spanned
import android.text.style.TtsSpan
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import au.id.micolous.metrodroid.MetrodroidApplication
import au.id.micolous.metrodroid.util.Preferences
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import java.io.DataInputStream
import java.io.InputStream
import java.util.*

actual fun <T> runAsync(block: suspend () -> T) {
    runBlocking { block() }
}

@RunWith(AndroidJUnit4::class)
actual abstract class BaseInstrumentedTestPlatform {
    val context : Context
            get() = InstrumentationRegistry.getInstrumentation().context

    /**
     * Sets the Android and Java locales to a different language
     * and country. Does not clean up after execution, and should
     * only be used in tests.
     *
     * @param languageTag ITEF BCP-47 language tag string
     */
    actual fun setLocale(languageTag: String) {
        val l = compatLocaleForLanguageTag(languageTag)
        Locale.setDefault(l)
        setResourcesLocale(l, context.resources)
    }

    /**
     * Sets the system language used by [MetrodroidApplication] resources.
     *
     * This is needed for things that call
     * [au.id.micolous.metrodroid.multi.Localizer.localizeString].
     *
     * @param languageTag ITEF BCP-47 language tag string
     */
    fun setAndroidLanguage(languageTag: String?) {
        val l = languageTag?.let { compatLocaleForLanguageTag(it) }
        setResourcesLocale(l, MetrodroidApplication.instance.resources)
    }

    private fun setResourcesLocale(l: Locale?, r: Resources) {
        val c = r.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            c.setLocale(l)
        } else {
            c.locale = l
        }
        r.updateConfiguration(c, r.displayMetrics)
    }

    /**
     * Sets a boolean preference.
     * @param preference Key to the preference
     * @param value Desired state of the preference.
     */
    private fun setBooleanPref(preference: String, value: Boolean) {
        val prefs = Preferences.getSharedPreferences()
        prefs.edit()
                .putBoolean(preference, value)
                .apply()
    }

    actual fun showRawStationIds(state: Boolean) {
        setBooleanPref(Preferences.PREF_SHOW_RAW_IDS, state)
    }

    actual fun showLocalAndEnglish(state: Boolean) {
        setBooleanPref(Preferences.PREF_SHOW_LOCAL_AND_ENGLISH, state)
    }

    actual fun loadAssetSafe(path: String) : InputStream? {
        try {
            return DataInputStream(context.assets.open(path, AssetManager.ACCESS_RANDOM))
        } catch (e: Exception) {
            return null
        }
    }

    actual fun listAsset(path: String) : List <String>? = context.assets.list(path)?.toList()

    fun assertSpannedEquals(expected: String, actual: Spanned) {
        // nbsp -> sp
        val actualString = actual.toString().replace(' ', ' ')
        assertEquals(expected, actualString)
    }

    fun assertSpannedThat(actual: Spanned, matcher: Matcher<in String>) {
        // nbsp -> sp
        val actualString = actual.toString().replace(' ', ' ')
        assertThat<String>(actualString, matcher)
    }

    fun assertTtsMarkers(currencyCode: String, value: String, span: Spanned) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return
        }

        val ttsSpans = span.getSpans(0, span.length, TtsSpan::class.java)
        assertEquals(1, ttsSpans.size)

        assertEquals(TtsSpan.TYPE_MONEY, ttsSpans[0].type)
        val bundle = ttsSpans[0].args
        assertEquals(currencyCode, bundle.getString(TtsSpan.ARG_CURRENCY))
        assertEquals(value, bundle.getString(TtsSpan.ARG_INTEGER_PART))
    }

    companion object {
        private val LOCALES = mapOf(
                "en" to Locale.ENGLISH,
                "en-AU" to Locale("en", "AU"),
                "en-CA" to Locale.CANADA,
                "en-GB" to Locale.UK,
                "en-US" to Locale.US,
                "fr" to Locale.FRENCH,
                "fr-CA" to Locale.CANADA_FRENCH,
                "fr-FR" to Locale.FRANCE,
                "ja" to Locale.JAPANESE,
                "ja-JP" to Locale.JAPAN,
                "ru" to Locale("ru"),
                "ru-RU" to Locale("ru", "RU"),
                "zh-CN" to Locale.SIMPLIFIED_CHINESE,
                "zh-TW" to Locale.TRADITIONAL_CHINESE)

        private fun compatLocaleForLanguageTag(languageTag: String): Locale {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Locale.forLanguageTag(languageTag)
            } else {
                LOCALES[languageTag]
                        ?: throw IllegalArgumentException("For API < 21, add entry to LOCALES for: $languageTag")
            }
        }
    }
}