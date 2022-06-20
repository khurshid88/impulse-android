package com.impulse.impulse.viewmodel.repository

import com.impulse.impulse.data.local.dao.MessageDao
import com.impulse.impulse.data.local.entity.Message

class MessageRepository(private val messageDao: MessageDao) {

    fun addMessage(message: Message) = messageDao.addMessage(message)
    fun deleteMessage() = messageDao.deleteMessage()
    fun getMessage() = messageDao.getMessage()
}