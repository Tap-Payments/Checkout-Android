package company.tap.tapcheckout_android.scanner_activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import company.tap.cardscanner.CameraFragment
import company.tap.cardscanner.TapCard
import company.tap.cardscanner.TapScannerCallback
import company.tap.cardscanner.TapTextRecognitionCallBack
import company.tap.cardscanner.TapTextRecognitionML
import company.tap.tapcheckout_android.R
import company.tap.tapcheckout_android.TapCheckout


private const val SCAN_CARD_ID = 101

class ScannerActivity : AppCompatActivity(), TapTextRecognitionCallBack, TapScannerCallback{
    //InlineViewCallback {
    lateinit var webView: WebView
    private var textRecognitionML: TapTextRecognitionML? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scanner_activity)

        textRecognitionML = TapTextRecognitionML(this)
        textRecognitionML?.addTapScannerCallback(this)

//
//        val chooseImageIntent = Intent(
//            this,
//            CameraActivity::class.java
//        )
//        startActivity(chooseImageIntent)
      //  val intent = ScanCardIntent.Builder(this).build()
      //  startActivityForResult(intent, SCAN_CARD_ID)

        val cameraFragment = CameraFragment()
        cameraFragment.setCallBack(this,this)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.inline_container,cameraFragment)
            .commit()
//        supportFragmentManager
//            .beginTransaction()
//            .replace(company.tap.tapcardformkit.R.id.inline_container, CameraFragment())
//            .commit()
//

    }

    override fun onRestart() {
        super.onRestart()
        //finish()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SCAN_CARD_ID -> if (resultCode == Activity.RESULT_OK) {
               /* val card = data!!.getParcelableExtra<Card>(ScanCardIntent.RESULT_PAYCARDS_CARD)
                if (card != null) {
                    fillCardNumber(cardNumber = card?.cardNumber.toString(), cardHolderName =card?.cardHolderName ?: "" , cvv ="" , expiryDate =card?.expirationDate ?: "" )
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }*/
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }



    override fun onRecognitionSuccess(card: TapCard?) {
        println("cardNumber>>>>"+card)
        Toast.makeText(this@ScannerActivity, card?.cardNumber.toString(), Toast.LENGTH_LONG).show()
      //  if (card?.cardNumber != null && card.cardHolder != null && card.expirationDate != null) {
        if (card?.cardNumber != null ) {
            TapCheckout.fillCardNumber(
                cardNumber = card?.cardNumber.toString(),
                cardHolderName = card?.cardHolder ?: "",
                cvv = "",
                expiryDate = card.expirationDate ?: ""
            )
          //  incrementalCount = 0
        }
    }

    override fun onRecognitionFailure(error: String?) {
        Toast.makeText(this@ScannerActivity, error?.toString(), Toast.LENGTH_LONG).show()
    }

    override fun onReadSuccess(card: TapCard?) {
        println("cardNumber>>>>"+card)
//        Toast.makeText(this@ScannerActivity, card?.cardNumber.toString(), Toast.LENGTH_LONG).show()
        //  if (card?.cardNumber != null && card.cardHolder != null && card.expirationDate != null) {
        if (card?.cardNumber != null && card.expirationDate !=null ) {
            TapCheckout.fillCardNumber(
                cardNumber = card?.cardNumber.toString(),
                cardHolderName = card?.cardHolder ?: "",
                cvv = "",
                expiryDate = card.expirationDate ?: ""
            )
            //  incrementalCount = 0
        }
        finish()

    }

    override fun onReadFailure(error: String?) {
        Toast.makeText(this@ScannerActivity, error?.toString(), Toast.LENGTH_LONG).show()

    }

  /*  override fun onScanCardFailed(e: Exception?) {
        Toast.makeText(this@ScannerActivity, e?.message.toString(), Toast.LENGTH_LONG).show()
    }

    override fun onScanCardFinished(card: Card?, cardImage: ByteArray?) {
        Toast.makeText(this@ScannerActivity, card?.cardNumber.toString(), Toast.LENGTH_LONG).show()

    }*/
    override fun onBackPressed() {
        super.onBackPressed()
        finish()

    }

}
