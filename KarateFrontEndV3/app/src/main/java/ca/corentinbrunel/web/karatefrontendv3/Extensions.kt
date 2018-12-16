package ca.corentinbrunel.web.karatefrontendv3

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView

fun ImageView.setImageBase64(base64: String) {
    val decodedString = Base64.decode(base64, Base64.DEFAULT)
    val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

    this.setImageBitmap(decodedByte)
}

fun ImageView.setImageBase64WithHead(base64: String) {
    val decodedString = Base64.decode(base64.substring(base64.indexOf(',') + 1), Base64.DEFAULT)
    val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

    this.setImageBitmap(decodedByte)
}
