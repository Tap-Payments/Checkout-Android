package company.tap.tapcheckout_android.nfcbottomsheet

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import company.tap.nfcreader.open.reader.TapEmvCard
import company.tap.nfcreader.open.reader.TapNfcCardReader
import company.tap.nfcreader.open.utils.TapCardUtils
import company.tap.nfcreader.open.utils.TapNfcUtils
import company.tap.tapcheckout_android.R
import company.tap.tapcheckout_android.TapCheckout
import company.tap.tapcheckout_android.TapCheckoutDataConfiguration

import company.tap.taplocalizationkit.LocalizationManager

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import java.io.IOException
import java.net.SocketException
import java.util.*

class NFCBottomSheetActivity : AppCompatActivity() {
    private lateinit var tapNfcCardReader: TapNfcCardReader
    private var cardReadDisposable = Disposable.empty()
    lateinit var nfcBottomSheet: NfcBottomSheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_three_ds_web_view)
        tapNfcCardReader = TapNfcCardReader(this)

        LocalizationManager.setLocale(this, Locale(TapCheckoutDataConfiguration.lanuage.toString()))

        nfcBottomSheet = NfcBottomSheet()
       // nfcBottomSheet.loadLottie() // stopped was creating NPE
        nfcBottomSheet.show(supportFragmentManager,"")
    }


    override fun onNewIntent(intent: Intent?) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent)
        // if this activity is in stack , this method will be called
        handleNFCResult(intent)
    }



    fun handleNFCResult(intent: Intent?) {
        if (tapNfcCardReader?.isSuitableIntent(intent)== true) {
            cardReadDisposable = tapNfcCardReader
                .readCardRx2(intent)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ emvCard: TapEmvCard? ->
                    if (emvCard != null) {
                        // tapCheckoutFragment.viewModel?.handleNFCScannedResult(emvCard)
                        println("emvCard${emvCard}")
                        println("emvCardexpireDate ${emvCard.cardNumber
                        }")
                        convertDateString(emvCard)

                    }
                },
                    {
                            throwable ->

                        RxJavaPlugins.setErrorHandler(Consumer { e: Throwable ->
                            var e = e
                            if (e is UndeliverableException) {
                                e = e.cause!!
                            }
                            if (e is IOException || e is SocketException) {
                                // fine, irrelevant network problem or API that throws on cancellation
                                return@Consumer
                            }
                            if (e is InterruptedException) {
                                // fine, some blocking code was interrupted by a dispose call
                                return@Consumer
                            }
                            if (e is NullPointerException || e is IllegalArgumentException) {
                                // that's likely a bug in the application
                                println("eee$e")
                                Thread.currentThread().uncaughtExceptionHandler
                                    .uncaughtException(Thread.currentThread(), e)
                                Log.e("warn", "NullPointerException to do", e)
                                return@Consumer
                            }
                            if (e is IllegalStateException) {
                                // that's a bug in RxJava or in a custom operator
                                Thread.currentThread().uncaughtExceptionHandler.toString()
                                //  .handleException(Thread.currentThread(), e);
                                Log.e("warn", "IllegalStateExceptiont to do", e)
                                return@Consumer
                            }
                            Log.e("warn", "Undeliverable exception received, not sure what to do", e)
                        })
                    })
        }else {
            RxJavaPlugins.setErrorHandler { e ->
                if (e is UndeliverableException) {
                    // Merely log undeliverable exceptions
                    Log.e("Try check",e.message.toString())
                } else {
                    // Forward all others to current thread's uncaught exception handler
                    Thread.currentThread().also { thread ->
                        thread.uncaughtExceptionHandler.uncaughtException(thread, e)
                    }
                }
            }
        }

    }

    private fun displayError(message: String?) {
        Toast.makeText(this, message.toString(), Toast.LENGTH_SHORT).show()
    }
    private fun convertDateString(emvCard: TapEmvCard) {
        var expDateString :String?=null
        //  println("emvCard.getExpireDate()"+emvCard.getExpireDate())
        val dateParts: CharSequence? = DateFormat.format("M/y", emvCard.getExpireDate())
        println("dateparts" + dateParts?.length)
        if (dateParts?.contains("/") == true) {
            if (dateParts.length <= 3) {
                return
            } else {
                if (dateParts.length >= 5 || dateParts.length >= 4) {
                    val month = (dateParts).substring(0, 1).toInt()
                    val year = (dateParts).substring(2, 4)
                    if (year.contains("/")) {

                        return
                    } else {

                        if(month<10) expDateString= "0$month/$year"
                        else expDateString = "$month/$year"

                        if (emvCard != null) {
                            TapCheckout.fillCardNumber(
                                cardNumber = emvCard?.cardNumber.toString(),
                                cardHolderName = emvCard?.holderFirstname ?: "",
                                cvv = "",
                                expiryDate = expDateString ?: ""
                            )
                            TapCheckout.NFCopened = false
                            finish()
                        }



                    }

                }

            }
        }

    }

    override fun onResume() {
        if (TapNfcUtils.isNfcAvailable(this)) {
            if (TapNfcUtils.isNfcEnabled(this)) {
                tapNfcCardReader?.enableDispatch();
                //  scancardContent.setVisibility(View.VISIBLE);
            }
            // else
            // enableNFC();
        } else {
//            scancardContent.setVisibility(View.GONE);
//            cardreadContent.setVisibility(View.GONE);
//            noNfcText.setVisibility(View.VISIBLE);
        }
        super.onResume();
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()

    }

    override fun onPause() {
        super.onPause()
        if (TapCheckout.NFCopened) {
            cardReadDisposable.dispose()
            tapNfcCardReader?.disableDispatch()


        }

    }

    private fun showCardInfo(emvCard: TapEmvCard) {
        val text: String = TextUtils.join(
            "\n", arrayOf<Any>(
                TapCardUtils.formatCardNumber(emvCard.cardNumber, emvCard.type),
                DateFormat.format("M/y", emvCard.expireDate),
                "---",
                "Bank info (probably): ",
                emvCard.atrDescription,
                "---",
                emvCard.toString().replace(", ", ",\n")
            )
        )
        Log.e("showCardInfo:", text)

    }

}
