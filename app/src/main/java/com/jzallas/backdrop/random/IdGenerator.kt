package com.jzallas.backdrop.random

import java.util.UUID

class IdGenerator() {
  fun createRandomId() = UUID.randomUUID().toString()
}
