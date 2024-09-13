package ru.netology.nmedia.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import ru.netology.nmedia.dto.Attachment

class AttachmentConverter {
    @TypeConverter
    fun fromAttachment(attachment: Attachment?): String? {
        return Gson().toJson(attachment)
    }

    @TypeConverter
    fun toAttachment(data: String?): Attachment? {
        return Gson().fromJson(data, Attachment::class.java)
    }
}
