package com.sam.askme.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import javax.inject.Inject
import javax.inject.Named

class AskMeRepository @Inject constructor(
    @Named("text") private val generativeModel: GenerativeModel,
    @Named("photo") private val generativeModelImage: GenerativeModel
) {

    val chetList = generativeModel.startChat(history = listOf())

    fun getImageResponse(inputImage: Content) =
        generativeModelImage.generateContentStream(inputImage)

}