package com.panaadhaar.extractor.india.utils

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.IOException

class TextExtractionUtils {
    companion object {
        suspend fun extractTextFromImage(imageUri: Uri, context: Context): String {
            return try {
                val image = InputImage.fromFilePath(context, imageUri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                
                val result = recognizer.process(image).await()
                recognizer.close()
                
                result.text.ifEmpty {
                    throw Exception("No text found in the image")
                }
            } catch (e: IOException) {
                throw Exception("Failed to load image: ${e.message}", e)
            } catch (e: Exception) {
                throw Exception("Text extraction failed: ${e.message}", e)
            }
        }
    }
}
