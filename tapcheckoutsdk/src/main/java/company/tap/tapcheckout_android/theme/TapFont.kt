package company.tap.tapcheckout_android.theme


/**

Copyright (c) 2020    Tap Payments.
All rights
reserved.
 **/
enum class TapFont {
    latoThin,
    latoLight,
    latoMedium,
    latoRegular,
    latoBold,

    RobotoThin,
    RobotoLight,
    RobotoMedium,
    RobotoRegular,
    RobotoBold,

    TajawalThin,
    TajawalLight,
    TajawalMedium,
    TajawalRegular,
    TajawalBold,


    CirceExtraLight,
    CirceLight,
    CirceRegular,
    CirceBold,


    ArabicTajawalRegular,
    ArabicTajawalMedium,
    ArabicTajawalBold,

    SystemDefault;



    companion object {
        fun tapFontType(tapFont: TapFont):String {
            return when(tapFont){
                RobotoRegular -> "fonts/Roboto-Regular.ttf"
                RobotoThin ->"fonts/Roboto-Thin.ttf"
                RobotoLight ->"fonts/Roboto-Light.ttf"
                RobotoMedium ->"fonts/Roboto-Medium.ttf"
                RobotoBold ->"fonts/Roboto-Bold.ttf"

                TajawalRegular -> "fonts/Tajawal-Medium.ttf"
                TajawalThin ->"fonts/Tajawal-Thin.ttf"
                TajawalLight ->"fonts/Tajawal-Regular.ttf"
                TajawalMedium ->"fonts/Tajawal-Medium.ttf"
                TajawalBold ->"fonts/Tajawal-Bold.ttf"

                CirceExtraLight ->"fonts/Circe-ExtraLight.ttf"
                CirceLight ->"fonts/Circe-Light.ttf"
                CirceRegular ->"fonts/Circe-Regular.ttf"
                CirceBold ->"fonts/Circe-Bold.ttf"

                latoBold ->"fonts/Lato-Bold.ttf"
                latoLight ->"fonts/Lato-Light.ttf"
                latoRegular ->"fonts/Lato-Regular.ttf"
                latoThin ->"fonts/Lato-Light.ttf"
                latoMedium ->"fonts/Lato-Medium.ttf"

                ArabicTajawalRegular ->"fonts/Tajawal-Medium.ttf"
                ArabicTajawalMedium ->"fonts/Tajawal-Medium.ttf"
                ArabicTajawalBold ->"fonts/Tajawal-Bold.ttf"

                else->"fonts/Roboto-Regular.ttf"
            }
        }
    }


}





