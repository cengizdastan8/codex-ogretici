package dev.ekrankarartma.s2

import android.content.ComponentName
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.xmlpull.v1.XmlPullParser

@RunWith(RobolectricTestRunner::class)
class ManifestVeXmlTest {

    private val baglam get() = RuntimeEnvironment.getApplication()
    private val androidAd = "http://schemas.android.com/apk/res/android"

    @Test
    fun erisilebilirlikXmlPencereIcerigiOkumaz() {
        val xml: XmlResourceParser = baglam.resources.getXml(R.xml.karartma_erisilebilirlik)
        var olay = xml.eventType
        var bulundu = false
        while (olay != XmlPullParser.END_DOCUMENT) {
            if (olay == XmlPullParser.START_TAG && xml.name == "accessibility-service") {
                assertEquals("false", xml.getAttributeValue(androidAd, "canRetrieveWindowContent"))
                assertNull(xml.getAttributeValue(androidAd, "accessibilityEventTypes"))
                bulundu = true
                break
            }
            olay = xml.next()
        }
        assertTrue(bulundu)
    }

    @Test
    fun manifestteSystemAlertWindowVeInternetYok() {
        val bilgi = baglam.packageManager.getPackageInfo(
            baglam.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val izinler = bilgi.requestedPermissions?.toList().orEmpty()

        assertFalse(izinler.contains("android.permission.SYSTEM_ALERT_WINDOW"))
        assertFalse(izinler.contains("android.permission.INTERNET"))
        assertFalse(izinler.contains("android.permission.FOREGROUND_SERVICE"))
        assertFalse(izinler.contains("android.permission.FOREGROUND_SERVICE_SPECIAL_USE"))
    }

    @Test
    fun erisilebilirlikServisiBindIzniVar() {
        val bilesen = ComponentName(baglam, KarartmaErisilebilirlikServisi::class.java)
        val servisBilgisi = baglam.packageManager.getServiceInfo(bilesen, 0)

        assertNotNull(servisBilgisi)
        assertEquals("android.permission.BIND_ACCESSIBILITY_SERVICE", servisBilgisi.permission)
    }
}
