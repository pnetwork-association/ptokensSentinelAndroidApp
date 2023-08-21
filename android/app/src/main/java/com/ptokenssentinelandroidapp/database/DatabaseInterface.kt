package com.ptokenssentinelandroidapp

interface DatabaseInterface {
  fun close()

  fun delete(key: ByteArray?)

  fun put(key: ByteArray?, value: ByteArray?, dataSensitivity: Byte)

  @Throws(DatabaseException::class)
  fun endTransaction()

  @Throws(DatabaseException::class)
  fun startTransaction()

  @Throws(Exception::class)
  operator fun get(key: ByteArray?, dataSensitivity: Byte): ByteArray?
}