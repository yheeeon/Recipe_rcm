package com.example.recipe_rcm.ClovaOCR

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.recipe_rcm.R

class Camera_ocr : Fragment() {

    private lateinit var captureButton: Button

    companion object {
        private const val REQUEST_CODE_GALLERY = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.camera_ocr, container, false)

        captureButton = rootView.findViewById(R.id.button_capture)

        captureButton.setOnClickListener {
            openGallery()
        }

        return rootView
    }

    private fun openGallery() {
        // 갤러리에서 이미지를 선택하도록 인텐트 시작
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // 갤러리에서 선택된 이미지 URI를 가져와서 OCR로 처리
                val intent = Intent(requireContext(), OcrResultActivity::class.java)
                intent.putExtra("image_uri", uri.toString()) // 이미지 URI를 전달
                activity?.let { activity ->
                    startActivity(intent)
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        } else {
            Toast.makeText(requireContext(), "이미지 선택에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
