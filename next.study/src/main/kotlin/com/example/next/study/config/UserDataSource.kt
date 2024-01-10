package com.example.next.study.config

import com.example.next.study.dao.Template
import org.springframework.boot.jdbc.DataSourceBuilder

object UserDataSourceConfig {
    val userTemplate: Template by lazy {
        Template(
            DataSourceBuilder.create()
                .apply {
                    url("jdbc:h2:mem:testdb")
                    username("sa")
                    password("")
                    driverClassName("org.h2.Driver")
                }.build()
        )
    }
}
