package com.howell.action

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult
import android.os.CancellationSignal
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.util.Log

import com.howell.bean.FingerprintBean
import com.howell.utils.MyFingerprintUtil

import java.lang.reflect.InvocationTargetException


@SuppressLint("NewApi", "Override")
class FingerprintUiHelper(private val mContext: Context, private val mFingerprintManager: FingerprintManager, private val mCallback: Callback) : FingerprintManager.AuthenticationCallback() {
    private var mCancellationSignal: CancellationSignal? = null
    private var mSelfCancelled: Boolean = false
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            Log.i("123", "get msg msg.what=" + msg.what)
            super.handleMessage(msg)
        }
    }

    // TODO: Consider calling
    //    ActivityCompat#requestPermissions
    // here to request the missing permissions, and then overriding
    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
    //                                          int[] grantResults)
    // to handle the case where the user grants the permission. See the documentation
    // for ActivityCompat#requestPermissions for more details.
    // TODO: Consider calling
    //    ActivityCompat#requestPermissions
    // here to request the missing permissions, and then overriding
    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
    //                                          int[] grantResults)
    // to handle the case where the user grants the permission. See the documentation
    // for ActivityCompat#requestPermissions for more details.
    private val isFingerprintAuthAvailable: Boolean
        get() {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            return if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                false
            } else mFingerprintManager.isHardwareDetected && mFingerprintManager.hasEnrolledFingerprints()
        }

    fun startListening(cryptoObject: FingerprintManager.CryptoObject?) {
        if (!isFingerprintAuthAvailable) {
            Log.e("123", "finger print not available")
            return
        }
        mCancellationSignal = CancellationSignal()
        mSelfCancelled = false
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mFingerprintManager.authenticate(cryptoObject, mCancellationSignal, 0, this, mHandler)
    }

    fun stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true
            try {
                mCancellationSignal!!.cancel()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            mCancellationSignal = null
        }
    }


    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        Log.i("123", "onAuthenticationError:errorCode=$errorCode   errString=$errString")

        when (errorCode) {
            FingerprintManager.FINGERPRINT_ERROR_CANCELED -> {
                mSelfCancelled = true
                try {
                    mCallback.onFingerCancel()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            FingerprintManager.FINGERPRINT_ERROR_LOCKOUT -> try {
                mCallback.onError(errorCode, errString)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            else -> {
            }
        }
    }

    override fun onAuthenticationFailed() {
        Log.e("123", "onAuthenticationFailed")
        mCallback.onFailed()
    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
        Log.e("123", "onAuthenticationError:helpcode=$helpCode   helpString=$helpString")
        mCallback.onHelp(helpCode, helpString)
    }

    override fun onAuthenticationSucceeded(result: AuthenticationResult) {

        var bean: FingerprintBean? = null

        try {
            bean = MyFingerprintUtil.getFingerprint(result)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (bean != null) {
            Log.e("123", "onAuthenticationSucceeded   fingerID=" + bean.fpID)

            mCallback.onAuthenticated(bean.fpID)
        } else {
            Log.i("123", "bu zhi chi")
            mCallback.onFingerNoSupport()

        }
    }


    interface Callback {
        fun onAuthenticated(id: Int)
        fun onFailed()
        fun onHelp(helpCode: Int, str: CharSequence)
        fun onError(code: Int, s: CharSequence)
        fun onFingerCancel()
        fun onFingerNoSupport()
    }

    companion object {

        fun isFingerAvailable(context: Context): Boolean {

            val fm = context.getSystemService(FingerprintManager::class.java)
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false
            }
            return if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                false
            } else fm!!.isHardwareDetected && fm.hasEnrolledFingerprints()
        }
    }

}
