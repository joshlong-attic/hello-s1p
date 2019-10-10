package com.example.app

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.annotation.Id
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux

@SpringBootApplication
class AppApplication

fun main(args: Array<String>) {
    runApplication<AppApplication>(*args) {
        val context = beans {
            bean {
                val repository = ref<CustomerRepository>()
                ApplicationRunner {
                    Flux
                            .just("James", "Matt", "Josh")
                            .map { Customer(name = it) }
                            .flatMap { repository.save(it) }
                            .thenMany(repository.findAll())
                            .subscribe { println(it) }
                }
            }
            bean {
                val repo = ref<CustomerRepository>()
                router {
                    GET("/hello") {
                        val reply = it
                                .principal()
                                .map { it as OAuth2AuthenticationToken }
                                .map { it.principal as OidcUser }
                                .map { it.fullName }
                        ServerResponse.ok().body(reply)
                    }
                    GET("/customers") {
                        ServerResponse.ok().body(repo.findAll())
                    }
                }
            }
        }
        addInitializers(context)
    }
}


interface CustomerRepository : ReactiveCrudRepository<Customer, Integer>

data class Customer(@Id var id: String? = null, val name: String)