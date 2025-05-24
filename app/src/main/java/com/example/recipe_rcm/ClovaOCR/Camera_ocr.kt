package com.example.recipe_rcm.ClovaOCR

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.recipe_rcm.R
import java.io.File

class Camera_ocr : Fragment() {

    private lateinit var captureButton: Button  //촬영 버튼
    private var imageUri: Uri? = null           //선택된 이미지 url 저장

    companion object {
        private const val REQUEST_CODE_GALLERY = 2 //갤러리 호출
        private const val FILE_PROVIDER_AUTHORITY = "com.example.recipe_rcm.fileprovider"
    }
    //fragment view 초기화 및 버튼 클릭 이벤트
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.camera_ocr, container, false)

        captureButton = rootView.findViewById(R.id.button_capture)
        //버튼 클릭 시 이미지 선택 다이얼로그 표시
        captureButton.setOnClickListener {
            showImageSourceDialog()
        }

        return rootView
    }

    // 이미지 선택 다이얼로그 (카메라 촬영 또는 갤러리 선택)
    private fun showImageSourceDialog() {
        val options = arrayOf("카메라로 촬영", "갤러리에서 선택")
        AlertDialog.Builder(requireContext())
            .setTitle("이미지 가져오기")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    // CameraX를 사용한 카메라 촬영 기능
    private fun openCamera() {
        val photoFile = File(requireContext().filesDir, "captured_image.jpg")
        imageUri = FileProvider.getUriForFile(requireContext(), FILE_PROVIDER_AUTHORITY, photoFile)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }

        cameraLauncher.launch(cameraIntent)
    }

    // 갤러리에서 이미지 선택 기능
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    // 카메라 결과 처리
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri?.let {
                    moveToOcrResult(it)
                }
            } else {
                Toast.makeText(requireContext(), "사진 촬영에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    //갤러리 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                moveToOcrResult(uri)
            }
        } else {
            Toast.makeText(requireContext(), "이미지 선택에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // OCR 결과 화면으로 이동
    private fun moveToOcrResult(uri: Uri) {
        val intent = Intent(requireContext(), OcrResultActivity::class.java)
        intent.putExtra("image_uri", uri.toString())
        startActivity(intent)
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
