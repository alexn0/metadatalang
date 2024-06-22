/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.example

import org.opt2code.example.demo.HelloWorldMessagesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
open class HelloWorldApplication : CommandLineRunner {
    @Autowired
    lateinit var helloWorld: HelloWorldService

    @Autowired
    lateinit var helloWorldMessages: HelloWorldMessagesService

    @Autowired
    lateinit var helloWorldContext: HelloWorldContextService

    override fun run(args: Array<String?>) {
        println("Printing helloWorld message using HelloWorldService:")
        helloWorld.printGreeting()
        println("Printing helloWorld messages using HelloWorldContextService:")
        helloWorldContext.printGreeting()
        println("Printing helloWorld messages using HelloWorldMessagesService:")
        helloWorldMessages.print()

    }
    /*
The output of the program would be:

Printing helloWorld message using HelloWorldService:
Hello world
Printing helloWorld messages using HelloWorldMessagesService:
Hello world1
Hello world2
Hello world3
     */

}

fun main(args: Array<String>) {
    runApplication<HelloWorldApplication>(*args)
}
