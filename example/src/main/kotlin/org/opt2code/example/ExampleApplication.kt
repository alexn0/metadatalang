/*
 * Copyright (c) 2024 opt2code.com aka alexn0. All Rights Reserved.
 */

package org.opt2code.example

import org.opt2code.example.demo.HelloWorldMessages
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
open class ExampleApplication : CommandLineRunner {
    @Autowired
    lateinit var helloWorld: HelloWorld

    @Autowired
    lateinit var helloWorldMessages: HelloWorldMessages

    override fun run(args: Array<String?>) {
        println("Printing helloWorld message:")
        helloWorld.printGreeting()
        println("Printing helloWorldMessages:")
        helloWorldMessages.print()
    }

}

fun main(args: Array<String>) {
    runApplication<ExampleApplication>(*args)
}
