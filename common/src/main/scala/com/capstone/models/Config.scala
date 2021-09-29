package com.capstone.models

case class JWTConfig(key: String, expireDurationSec: Int)

case class Configurations(jwtScalaCirce: JWTConfig)
