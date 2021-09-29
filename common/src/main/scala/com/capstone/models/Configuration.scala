package com.capstone.models

case class AppConfiguration(app: ApplicationConf,
                            akka: AkkaConfig,
                            dbConfig: DBConfig)

case class DBConfig(profile: String,
                    driver: String,
                    url: String,
                    user: String,
                    password: String,
                    schema: String,
                    threadsPoolCount: Int,
                    queueSize: Int,
                    searchLimit: Int)

case class ApplicationConf(host: String, port: Int)

case class AkkaConfig(futureAwaitDurationMins: Int, akkaWorkersCount: Int)
