package com.ptokenssentinelandroidapp

class DatabaseException : Exception {
    internal constructor(message: String?) : super(message) {}
    constructor(e: Exception?) : super(e) {}
}