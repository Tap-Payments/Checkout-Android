package company.tap.tapcheckout_android.theme

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import company.tap.nfcreader.open.reader.TapEmvCard
import company.tap.nfcreader.open.reader.TapNfcCardReader
import company.tap.nfcreader.open.utils.TapCardUtils
import company.tap.nfcreader.open.utils.TapNfcUtils

import company.tap.tapcheckout_android.R

import company.tap.taplocalizationkit.LocalizationManager

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable



/**
 * Created by AhlaamK on 7/2/20.

Copyright (c) 2020    Tap Payments.
All rights reserved.
 **/
class NFCFragment : Fragment() {
    private lateinit var customNFC: TapNFCView
    private lateinit var scanNFC: TapTextViewNew
    private lateinit var aboutNFC: TapTextViewNew
    private var tapNfcCardReader: TapNfcCardReader? = null
    private var cardReadDisposable: Disposable = Disposable.empty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.custom_sheet_nfc, container, false)

        initView(view)
        return view

    }

    @SuppressLint("SetTextI18n")
    private fun initView(view: View) {
        customNFC = view.findViewById(R.id.custom_nfc)
        scanNFC = customNFC.scanNfc.findViewById(R.id.scan_nfc)
        aboutNFC = customNFC.aboutNFC.findViewById(R.id.aboutNFC)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        tapNfcCardReader = TapNfcCardReader(this.activity)
    }

    override fun onResume() {
        super.onResume()

        if (TapNfcUtils.isNfcAvailable(context)) {
            if (TapNfcUtils.isNfcEnabled(context)) {
                tapNfcCardReader?.enableDispatch() //Activates NFC  to read NFC Card details .

            } else {
                enableNFC()
            }
        } else {
            Toast.makeText(context,LocalizationManager.getValue("nfcUnsupported", "NFC") as String , Toast.LENGTH_SHORT).show()

        }



    }

    private fun enableNFC() {

        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(LocalizationManager.getValue("enableNFC", "NFC") as String)
        alertDialog.setMessage(LocalizationManager.getValue("disabledNFC", "NFC") as String)


        alertDialog.setPositiveButton(
            getString(R.string.msg_ok)
        ) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }
        alertDialog.setNegativeButton(
            getString(R.string.msg_dismiss)
        ) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            val fragmentTransaction: FragmentTransaction? = fragmentManager?.beginTransaction()
            fragmentTransaction?.remove(this)?.commit()
            // onBackPressed()
        }
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun displayError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showCardInfo(emvCard: TapEmvCard) {
        val text = TextUtils.join(
            "\n", arrayOf(
                TapCardUtils.formatCardNumber(emvCard.cardNumber, emvCard.type),
                DateFormat.format("M/y", emvCard.expireDate),
                "---",
                "Bank info (probably): ",
                emvCard.atrDescription,
                "---",
                emvCard.toString().replace(", ", ",\n")
            )
        )
      // Toast.makeText(context, text, Toast.LENGTH_LONG).show()
      Toast.makeText(context, LocalizationManager.getValue("scanSuccess", "NFC") as String, Toast.LENGTH_LONG).show()

        val fragmentTransaction: FragmentTransaction? = fragmentManager?.beginTransaction()
        fragmentTransaction?.remove(this)?.commit()
        Log.e("showCardInfo:", text)

    }

    fun processNFC(intent: Intent?) {
        if (tapNfcCardReader?.isSuitableIntent(intent)!!) {

            cardReadDisposable = tapNfcCardReader!!
                .readCardRx2(intent)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ emvCard: TapEmvCard? ->
                    if (emvCard != null) {
                        this.showCardInfo(emvCard)
                    }
                },
                    { throwable -> throwable.message?.let { displayError(it) } })
        }

    }

    override fun onPause() {
        cardReadDisposable.dispose()
        tapNfcCardReader?.disableDispatch()
        super.onPause()
    }
}