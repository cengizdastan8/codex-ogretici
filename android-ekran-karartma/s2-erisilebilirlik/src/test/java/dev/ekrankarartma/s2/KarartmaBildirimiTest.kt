package dev.ekrankarartma.s2

import android.app.Notification
import android.app.NotificationManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class KarartmaBildirimiTest {

    private val baglam get() = RuntimeEnvironment.getApplication()

    private fun bildirimler() =
        baglam.getSystemService(NotificationManager::class.java).activeNotifications

    @Before
    fun hazirla() {
        bildirimIzniVer(false)
        KarartmaBildirimi.iptal(baglam)
    }

    @After
    fun temizle() {
        KarartmaBildirimi.iptal(baglam)
        bildirimIzniVer(false)
    }

    @Test
    fun izinYoksaBildirimGosterilmez() {
        assertFalse(KarartmaBildirimi.izinVar(baglam))

        KarartmaBildirimi.goster(baglam)

        assertEquals(0, bildirimler().size)
    }

    @Test
    fun izinVarsaKapatEylemliBildirimGosterilir() {
        bildirimIzniVer(true)
        assertTrue(KarartmaBildirimi.izinVar(baglam))

        KarartmaBildirimi.goster(baglam)

        val liste = bildirimler()
        assertEquals(1, liste.size)
        val bildirim: Notification = liste[0].notification
        assertEquals(1, bildirim.actions.size)
        assertEquals(baglam.getString(R.string.bildirim_kapat), bildirim.actions[0].title.toString())
        assertTrue(bildirim.flags and Notification.FLAG_ONGOING_EVENT != 0)
    }

    @Test
    fun ayniBildirimTekrarGosterildigindeCogalmaz() {
        bildirimIzniVer(true)

        KarartmaBildirimi.goster(baglam)
        KarartmaBildirimi.goster(baglam)

        assertEquals(1, bildirimler().size)
    }

    @Test
    fun iptalBildirimiKaldirir() {
        bildirimIzniVer(true)
        KarartmaBildirimi.goster(baglam)

        KarartmaBildirimi.iptal(baglam)

        assertEquals(0, bildirimler().size)
    }
}
