package chats.cash.chats_field.utils.iris_scan

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import chats.cash.chats_field.R
import chats.cash.chats_field.utils.toast
import chats.cash.chats_field.views.auth.ui.ImageCaptureFragment
import com.eyecool.fragment.IrisFragment
import com.eyecool.fragment.IrisFragment.VerifyCallbackX
import com.eyecool.iris.Feature
import com.eyecool.iris.api.IrisResult
import com.eyecool.iris.api.IrisSDK
import com.eyecool.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.nio.charset.StandardCharsets

class IrisManager(
    private val context: Context,
    private val mIrisFragment: IrisFragment?,
    private val irisCallBack: IrisCallBack,
) : IrisFragment.CameraCallback {

    private val enrolledUsers = mutableListOf<Feature>()
    private var mIrisSDK: IrisSDK = IrisSDK.getInstance()

    fun initIrisSDK() {
        mIrisFragment?.addCallback(this)
        mIrisFragment?.setDebug(true)
        mIrisSDK.init(
            context,
            "",
            object : IrisSDK.IrisSDKCallback {
                override fun onSuccess() {
                    //                 context.toast("Iris Initialized")
                    irisCallBack.OnIrisSdkInitialized(mIrisSDK.version)
                }

                override fun onError(code: Int, msg: String) {
                    //                   context.toast("Iris couldn't be initialized because of $msg")
                    irisCallBack.OnIrisInitializationError(msg)
                }
            },
        )

        //}
        //  else{
        //    irisCallBack.OnIrisInitializationError("Fragment is null")
        //}
    }

    fun unit() {
        mIrisFragment?.cancel()
    }

    fun enroll() = CoroutineScope(Dispatchers.Main).async {
        mIrisFragment?.enroll(
            15000,
            object : IrisFragment.EnrollCallback {
                override fun onSuccess(template: ByteArray) {
                    irisCallBack.OnEnrollProgressChange(100, 100)
//                    val person =
//                        Person(name, template)
//                    enrolledUsers.add(person)
//                    FileUtils.writeFile(
//                        Environment.getExternalStorageDirectory().absolutePath + "/iris_template.txt",
//                        template,
//                    )
                    val userJson = template.toString(StandardCharsets.ISO_8859_1)
                    irisCallBack.OnEnrollSuccess(userJson)
                }

                override fun onError(errCode: Int, msg: String) {
                    irisCallBack.OnEnrollProgressChange(0, 0)
                    irisCallBack.OnEnrollError(msg)
                }

                override fun onProcess(progress: Int, state: Int) {
                    irisCallBack.OnEnrollProgressChange(progress, progress)
                    when (state) {
                        IrisFragment.DISTANCE_OK -> irisCallBack.AdjustmentOkay()
                        IrisFragment.DISTANCE_CLOSER -> irisCallBack.OnMakeAdjustMent(
                            context.getString(
                                R.string.move_away_from_camera,
                            ),
                        )
                        IrisFragment.DISTANCE_FUTHER -> irisCallBack.OnMakeAdjustMent(
                            context.getString(
                                R.string.move_closer_to_camera,
                            ),
                        )
                        else -> {}
                    }
                }
            },
        ) ?: run {
            irisCallBack.OnEnrollError(context.getString(R.string.fragment_is_null))
        }
    }

    private fun verifyIrisEntity() {
        if (enrolledUsers.isEmpty()) {
            context.toast("No enrolled users yet")
            return
        }
        irisCallBack.verifyingUser()

        mIrisFragment?.verify(
            5000,
            enrolledUsers,
            object : VerifyCallbackX {
                override fun onSuccess(position: Int) {
                    val person: Person =
                        enrolledUsers[position] as Person
                    irisCallBack.UserVerified(person.name)
                }

                override fun onProcess(state: Int) {
                    when (state) {
                        IrisFragment.DISTANCE_CLOSER -> irisCallBack.OnMakeAdjustMent("Move away from camera")
                        IrisFragment.DISTANCE_FUTHER -> irisCallBack.OnMakeAdjustMent("Move closer to camera")
                        else -> {}
                    }
                }

                override fun onError(errCode: Int, msg: String) {
                    irisCallBack.VerificationError(msg)
                }
            },
        )
    }

    private fun acquireImage(id: String) {
        if (TextUtils.isEmpty(id)) {
            return
        }
        val qualityStr: String = "30"
        if (TextUtils.isEmpty(qualityStr)) {
            return
        }
        // 质量分数
        var quality = 30
        try {
            quality = Integer.valueOf(qualityStr)
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        mIrisFragment?.acquireImage(
            (10 * 1000).toLong(),
            quality,
            object : IrisFragment.AcquireImageCallback {
                @SuppressLint("UnsafeOptInUsageError")
                override fun onSuccess(result: IrisResult) {
                    irisCallBack.OnEnrollProgressChange(100, 100)
                    val outputDirectory =
                        ImageCaptureFragment.getOutputDirectory(context)
                    val photoFileLeft = ImageCaptureFragment.createFile(
                        outputDirectory,
                        ImageCaptureFragment.FILENAME + "_left",
                        ".bmp",
                    )
                    val photoFileRight = ImageCaptureFragment.createFile(
                        outputDirectory,
                        ImageCaptureFragment.FILENAME + "_right",
                        ".bmp",
                    )
                    FileUtils.writeFile(
                        photoFileLeft.absolutePath,
                        result.leftImage,
                    )
                    FileUtils.writeFile(
                        photoFileRight.absolutePath,
                        result.rightImage,
                    )
                    FileUtils.writeFile(
                        photoFileLeft.absolutePath.replace(".bmp", "") + ".txt",
                        result.toString(),
                    )
                    if (photoFileLeft.exists() && photoFileRight.exists()) {
                        irisCallBack.OnEnrollSuccess(photoFileLeft.path, photoFileRight.path)
                    } else {
                        irisCallBack.OnEnrollError(context.getString(R.string.iris_file_not_found))
                    }
                }

                override fun onError(errCode: Int, msg: String) {
                    irisCallBack.OnEnrollError(msg)
                }

                override fun onProcess(state: Int) {
                    when (state) {
                        IrisFragment.DISTANCE_CLOSER -> irisCallBack.OnMakeAdjustMent(context.getString(R.string.move_away_from_camera))
                        IrisFragment.DISTANCE_FUTHER -> irisCallBack.OnMakeAdjustMent(context.getString(R.string.move_closer_to_camera))
                        IrisFragment.NO_IRIS -> irisCallBack.OnMakeAdjustMent(context.getString(R.string.no_iris_found_in_frame))
                        IrisFragment.LOCATION_FAILED -> irisCallBack.OnMakeAdjustMent(context.getString(R.string.no_iris_found_in_frame))
                        IrisFragment.POOL_QUALITY -> irisCallBack.OnMakeAdjustMent(context.getString(R.string.hold_steady_camera_blurry))
                        IrisFragment.OPEN_EYES -> irisCallBack.OnMakeAdjustMent(context.getString(R.string.open_your_eyes_wide))
                    }
                }
            },
        ) ?: run {
            irisCallBack.OnEnrollError(context.getString(R.string.fragment_is_null))
        }
    }

    override fun onOpen() {
        initIrisSDK()
    }

    override fun onOpenError(error: Int) {
        irisCallBack.CameraOpenError(error)
    }

    override fun onClose() {}
}

interface IrisCallBack {
    fun OnIrisSdkInitialized(version: String)
    fun OnIrisInitializationError(msg: String)
    fun OnEnrollProgressChange(leftEyeProgress: Int, rightEyeProgress: Int)
    fun OnEnrollError(reason: String)
    fun OnEnrollSuccess(user: String)
    fun OnEnrollSuccess(left: String, right: String)
    fun OnMakeAdjustMent(adjustment: String)
    fun AdjustmentOkay()
    fun verifyingUser()
    fun VerificationError(message: String)
    fun UserVerified(name: String)
    fun CameraOpenError(error: Int)
}

private class Person(val name: String, private val feature: ByteArray) : Feature {

    override fun getIrisFeature(): ByteArray {
        return feature
    }
}
