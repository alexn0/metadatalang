### A new technique to manage internal state of object, happened as embedding one language into Kotlin, shown to be easily integrated with Spring IoC and the Cake pattern, featuring a new style of programming

We will consider a new concept in programming, which was created as an embedding of another programming language into Kotlin, so it can be easily used in other Java-based languages. Using this new approach, we will show how it solves the problem of multiple inheritance by design, and for lovers of software design patterns, we will show how to implement the Cake pattern and to use Spring Dependency Injection as a very simple usage of the new technique. The main thing is that this new technique has a general purpose, so the domain of its usage coincides with one of any high-level programming language.

Technically, a new concept is about how we instantiate classes, implement methods and fields (i.e., where we keep the state of the object if needed), and what we get by using standard inheritance of classes. We will show how a new approach based on a very simple system of rules simplifies creating objects, including ones extending several classes/interfaces, keeping internal states of classes, adding new properties to classes, cloning objects, or creating objects that are backed by others. Now, let's get down to the details. Below, we will start explaining it in small, simple steps, trying not to dive into the specific syntax sugar of Kotlin and clarifying it where it is necessary.

The code of a simple object, by a new pattern, is to be created as an interface with all methods with default implementation, i.e.

```
interface SimpleObj {
    fun create(): X = /* code */
    //... other methods
}
```

For instantiating, we can optionally use a class with a fixed simple name like `O`, which should implement this interface, i.e.

```
interface SimpleObj {
    fun create(): X = /* code */

    private class O: SimpleObject
}
```

For instantiating the object, we define functions in the companion object of this interface, which are effectively static in Kotlin.
Using annotation, we can also turn it into static in the Java sense, but now we don't care about it:

```
interface SimpleObj {
    fun update(x: Boolean): Unit {
       /* ..*/
    }

    fun create(): X = /* code */

    private class O: SimpleObj // class O implements SimpleObj

    companion object {
        operator fun invoke(arg1: Boolean, vararg arg2: Int): SimpleObject = O().apply {
                                         // "apply" is a method for providing object SimpleObj.O as "this" object inside "apply" block
             update(arg1)
             /* other code for setting state of object*/
        }
    }
}
```

As a result, using the syntax sugar of Kotlin, i.e., the `operator` keyword, we can instantiate our object by the following line:

```
SimpleObj(true)
```

This line calls the `invoke` method, which calls an empty constructor of the `SimpleObj.O` class and executes the initialization of the object in the body of the `apply` function. Now the reader can see that the complexity of instantiating objects is removed from the constructor and moved into an effective static area, in fact, using the classical Builder software pattern. Doing so allows us to simplify object instantiation, avoiding complexity if we need post-initialization. We can also create objects implementing multiple interfaces using JDK dynamic proxy or anonymous object creation syntax, i.e., by line `object: SimpleObj1, SimpleObj2 {}`.

At first glance, everything looks good, but we need to define the internal state of the `SimpleObj` object somehow. As you know, you can not define  a field in an interface with its instantiation, and it is usually done in the class definition. Indeed, Kotlin's interface can have uninitialized fields, but we need to define corresponding fields in class. Evidently, we cannot use it since we will lose all advantages from our construction, i.e., we can forget about object instantiation simplicity and, in fact, multiple inheritance as well (to remember, a class can inherit multiple interfaces, but only one class). To say the truth, we can define an initialized field in a trait, Scala's interface, but it will add complexity to managing the state of such objects. Instead of it, to handle the problem, we will define a new interface, `KtLazy`, which contains a lot of useful functions helping to manage the internal state of an object, in fact, using a functional style. See example,

```
typealias Sp<T> = Function0<T> // Kotlin's syntax sugar

interface SimpleAddress: KtLazy {

    fun firstName(f: (() -> String)? = null): String = calc(f) { throw IllegalArgumentException() }
             // The above can be rewritten to "fun firstName(f: Sp<String>? = null) = calc(f) { throw IllegalArgumentException() }"

    fun familyName(f: (() -> String)? = null): String = calc(f) { throw IllegalArgumentException() }
             // The above can be rewritten to "fun familyName(f: Sp<String>? = null) = calc(f) { throw IllegalArgumentException() }"

    fun middleName(f: Sp<String?>? = null): String? = calc(f) { null } // we define a default value as null for the variable f;
                                                                       // this default value indicates a get operation. For not null f,
                                                                       // we execute updating of the middleName field

    private class O: SimpleAddress // class O implements SimpleAddress

    companion object {
        operator fun invoke(firstName: Boolean, familyName: String, middleName: String? = null): SimpleAddress = O().apply {
             firstName{ firstName }
             familyName{ familyName }
             middleName?.let { middleName{ it } }
        }
    }
}
```

The interface `SimpleAddress` inherits the predefined function `calc` from `KtLazy`, which is used for getting or setting properties.
So, for accessing the value of a field, we use the notation `ObjectName.fieldName()` and for updating, `ObjectName.fieldName{newValue}`. For example,

```
fun updateMiddleNameIfNotSet(address: SimpleAddress, middleName: String = "") {
    with (address) { // setting address as "this" object to omit prefix "address." in calling the middleName method.
        val prev = middleName() // getting value of middleName

        prev ?: middleName { middleName } // if prev == null, we set the field middleName
    }
}
```

To summarize what the `KtLazy` interface is doing, it maps an instance of the `SimpleAddress` class to the key-value map.
where key is the name of the field and value is the value of the field. So, the `SimpleAddress` object  is backed by a `Map<String, Any?>` object. For this,
we are using the fixed mapping `Map<Any, Map<String, Any?>>` (or some other approaches we will talk about further). Technically, to avoid a memory leak, we used a special version of the mapping `Map<Any, Map<String, Any?>>`,
So, if a key is not referenced externally, it is removed from this map.

Now, it is clear how object cloning and making an object backed by another are implemented (for objects implementing `KtLazy`).
Indeed, for object cloning, it is enough to recreate the object using an interface proxy and clone the key-value map from the original object.
To have an object backed by another object, we need to set the same key-value map for it as for the original object.

As we promised, we'll now show you how to safely add a new field (in a new sense) to the object created by the above pattern.
Indeed, it is as simple as creating an extension function for a class. See example,

```
fun SimpleAddress.phoneNumber(f: Sp<String?>? = null): String? = calc(f) { null }
```

This extension creates a new field, `phoneNumber`, for the `SimpleAddress` interface, and its usage safety is supported by Kotlin.

To summarize what we got until now, we have considered, at first approximation, a new pattern for creating flexible objects of general purpose using minimal instantiating code, with all method implementations bound to interfaces without any field!! but with an internal state backed by a `Map<String, Any?>` object defined by extending the `KtLazy` interface and using corresponding functions.

Next, we are going to consider the best practice of using the above pattern to avoid hidden problems in the code. After that, we consider integration with Spring IoC technology.

First, we would like to keep the information about how the object was created. The other thing is that there can be circular dependencies in the code. It can create a problem if we always use the fixed mapping `Map<Any, Map<String, Any?>>` to associate objects with their fields. Indeed, in this map we have to use weak references for keys and strong references for values, so using circular links can result in having keys in the values of the map, and it can prevent them from being garbage collected because this fixed mapping holds a strong reference for keys in this case. To avoid this problem and get the advantage of keeping the way an object was instantiated, it is recommended to use the following constructions:

1-st construction:
```
interface SimpleObj: KtLazy {
    fun value(f: Fn<Int>? = null): Int = pp(f) { 0 }

    private class O: KtLazy.E(), SimpleObj

    companion object {
        operator fun invoke(number: Int? = null) = O().postInit<SimpleObj> {
            number?.let { value { number } }
        }
    }
}
```
2-nd construction:

```
interface SimpleObj: KtLazy {
    fun value(f: Sp<Int>? = null): Int = calc(f) { 0 }
    
    private class O: SimpleObj

    companion object {
        operator fun invoke(number: Int? = null) = 0().postInit<SimpleObj> {
            number?.let { value { it } }
        }
    }
}

```
or

```
interface SimpleObj: KtLazy {
    fun value(f: Sp<Int>? = null): Int = calc(f) { 0 }

    companion object {
        operator fun invoke(number: Int? = null) = object : SimpleObj {}.postInit<SimpleObj> {
            number?.let { value { it } }
        }
    }
}
/*
 The only difference in the last variant is that we don't define class `SimpleObj.O`, 
 and instead of it, we use an anonymous object created by the line `object : SimpleObj {}`. 
 There are no other differences, and from the point of view of the new technique, 
 these variants are the same, so it is up to you which one to use
  (taking into account performance, code readability, and other arguments).
*/
```

The main difference between the two constructions is that the class `O` extends class `KtLazy.E`, the `ext` field of which is used for keeping the backed map for the fields of the object `SimpleObj` (instead of the aforementioned fixed mapping between `KtLazy` objects and their backed maps). This way, we avoid problems related to the memory leak. The same is true for the second construction; we
keep a backed map in an object created using JDK proxy technology. So, both approaches are safe for managing the internal state of objects, as class fields are safe to use and interchangeable in most situations. However, the first approach you need to use where JDK proxy technology is not fully supported, for example, if you need to compile the code into a binary executable using GraalVM technology. 

As you can notice, 
in both approaches, the `postInit` function is used for construction of an object. We also use it for keeping
the object initialization (i.e., which  happens in the curl block of the `postInit` function  
``` 
{  
  number?.let { value { it } } 
}
```
) as a function in the `init` field of the `KtLazy` interface. It can be handy in scenarios where you need to recreate the initial object with the same parameters.


Regarding the other best practices, the reader can notice that the `calc` function should calculate the name of the calling function to get the name of the field. For better performance, we can explicitly define
a field name in the `calc` function or use small unique hints (i.e., the `hint` argument) used in the `calc` function for optimizing the field name calculation. The same is true for other functions (`get`, `pp`, `pup`) of `KtLazy`, which are also used for defining fields and properties. The discussion about these functions is out of scope of this article, so for details, see the project code in the github repository (the link is at the end of the article).

The other interesting question is how to use the encapsulation principle since private methods cannot be defined in interfaces. However, we can use the encapsulation principle even in interfaces. For example, we use encapsulation for some methods in `KtLazy`, using the technique of extension functions defined on specific objects. This is a technique different from the classical implementation of encapsulation, but it promotes full understanding and readability of what is going on in the code. For example, we can differentiate functions, private in a new sense, by their extension objects.

Now, we consider how to integrate with Spring IoC technology and talk about the Cake pattern. For integration with Spring DI, we can use configuration service bean using the annotations `@Component` and `@Bean`. See example: 
```
interface HelloWorld: KtLazy {

	fun printer(f: Sp<Printer>? = null): Printer = calc(f) { throw IllegalArgumentException() }

	fun message(f: Sp<HelloWorldMessage>? = null): HelloWorldMessage = calc(f) { throw IllegalArgumentException() }

	fun printGreeting() { printer().print(message().text()) }
	
	private class O: HelloWorld

	companion object: Def()

        @Component
	open class Def {
		@Bean("helloWorld")
		open operator fun invoke(msg: HelloWorldMessage, printer: Printer) = O()
		               .postInit<HelloWorld> {
					message { msg }
					printer { printer }
				}
	}

}
```
or

```
interface HelloWorld: KtLazy {

	fun printer(f: Sp<Printer>? = null): Printer = calc(f) { throw IllegalArgumentException() }

	fun message(f: Sp<HelloWorldMessage>? = null): HelloWorldMessage = calc(f) { throw IllegalArgumentException() }

	fun printGreeting() { printer().print(message().text()) }
	
	private class O: HelloWorld

        @Component
	open class Def {
		@Bean
		open fun helloWorld(msg: HelloWorldMessage, printer: Printer) = O()
		               .postInit<HelloWorld> {
					message { msg }
					printer { printer }
				}
	}

} 
// This version can be used if we are not going to use the `invoke` method in
// the companion object defined in the first version of the code
```

By the above code, Spring DI should create a bean with the name `helloWorld` by calling the function `helloWorld`/`invoke` with arguments `msg`, `printer`, which were previously created by Spring as well, i.e., the `hellWorld` bean should be created with dependencies injected by Spring DI.

The next paragraph is devoted to the Cake software pattern, and is recommended to be omitted for most readers, at least until we present our variant of the Cake pattern for Kotlin, as it is a bit of a complicated topic for those who have never used the Scala language.

The main part of the Cake pattern, which originated from Scala, is a registry component, i.e., an interface (with its implementation) with fields keeping dependencies, service objects, and which implements all other abstract components; every one of them (a) contains a field with its own service object, (b) defines their dependencies, other service objects, as provided using the specific syntax of Scala, and (c) contains their own service implementations in its internal classes, so they (these service implementations) can access their service dependencies in the outer classes, i.e., in their components. A registry object implements all components, providing implementations for all service interfaces; for that, it can use components' service implementations defined as their internal classes, or, in tests, it can use alternative implementations. Our technique can simplify Cake pattern implementation in Kotlin as the following:
1) A registry interface implements all other abstract components.
2) Every component defines abstract methods to return service object dependencies for its own service object. In fact, it inherits these methods from service holder interfaces; every one of them contains only one method returning its own service object.
3) Every service interface implements its (own) component by delegation to an injected (own) component. 
4) A registry interface instantiates its fields, corresponding methods, injecting itself, as a registry object, into service objects.

Regarding condition 3, we can refuse component inheritance, but we are going to stick to it because it is convenient. Indeed, in this case, every service doesn't have to access service dependencies through its own component object.

Below is an example of a Cake pattern implementation in Kotlin with three components, each of which depends on two others:

```
interface AComponent: BHolder, CHolder
interface AHolder: KtLazy {
	fun aService(): AService
}

interface AService: AComponent {
	fun AService.reg(f: Sp<AComponent> ? = null): AComponent = calc(f) { throw IllegalArgumentException()}
	override fun bService(): BService = reg().bService()
	override fun cService(): CService = reg().cService()

	//your service methods

	private class O: AService

	companion object {
		operator fun invoke(r: AComponent) = O().postInit<AService>{
			reg {r}
		}
	}
}


interface BComponent: AHolder, CHolder
interface BHolder: KtLazy {
	fun bService(): BService
}

interface BService: BComponent {
	fun BService.reg(f: Sp<BComponent> ? = null): BComponent = calc(f) { throw IllegalArgumentException()}
	override fun aService(): AService = reg().aService()
	override fun cService(): CService = reg().cService()

	//your service methods

	private class O: BService

	companion object {
		operator fun invoke(r: BComponent) = O().postInit<BService>{
			reg {r}
		}
	}
}

// CService dependencies are defined here
interface CComponent: BHolder, AHolder
interface CHolder: KtLazy {
	fun cService(): CService
}

interface CService: CComponent {
	fun CService.reg(f: Sp<CComponent> ? = null): CComponent = calc(f) { throw IllegalArgumentException()}
	override fun aService(): AService = reg().aService()
	override fun bService(): BService = reg().bService()

	//your service methods

	private class O: CService

	companion object {
		operator fun invoke(r: CComponent) = O().postInit<CService>{
			reg {r}
		}
	}
}


interface Registry: AComponent, BComponent, CComponent {
	override fun aService(): AService = get { AService(this) }
	override fun bService(): BService = get { BService(this) }
	override fun cService(): CService = get { CService(this) }
	// The get function is used the same way as the calc function, but it remembers the result of its first invocation (i.e., the code that is in curl squares). 
	// The calc recalculates it until the result is explicitly set. These functions (get, calc) are provided with different signatures in KtLazy for convenience (i.e., they are overloaded).

	private class O: Registry

	companion object {
		operator fun invoke() = O().postInit<Registry>()
	}
}

```

Now assume that we have two service beans (`AService` and `BService`) injected by Spring DI or the Cake software pattern, and we have a third service (`CService`), which heavily depends on them, so it is reasonable that the third service should implement them, but, in this case, we have to inject all their service dependencies into the third, and, generally, it can be a difficult, error-prone task. See an example how our technique can solve this problem:

```
interface CService: AService, BService {
 
   //your methods

   class O(): CService

   companion object {
	 operator fun invoke(
		a: AService,
		b: BService
	 ) = O().postInit<CService>{
		  a.init()?.invoke(this)
		  b.init()?.invoke(this)
		}
   }
}
```

As you see, we easily inject all dependencies applying initialization code which was previously kept in the `init` fields for `AService` and `BService`. It is simple, but there is a caveat of the approach. Before using this technique, you should check that all dependencies are eagerly injected into `AService` and `BService`, otherwise you risk creating `CService` not fully initialized. The last can happen in case of having circular dependencies, so we have to initialize some dependencies lazy. However, even in this case you can use the above technique by wrapping lazy dependencies into eager ones i.e. using wrappers which have an internal state initialized lazy and can be easily injected into `CService`.


As you can see, the core of our technique is managing internal state using a functional approach and sticking to default interfaces along with other simple rules, which can be used with inheritance, composition, and other patterns as we can do in any high-level programming language in a more or less effective way. Talking about the paradigm of the new approach, it is interesting to distinguish between the technical implementation of the new approach, which was already discussed, and how we came to this technique.
The initial goals were around further developing the Opt2Code language, a language concept for creating DSL languages, and that was time-consuming, so I felt I didn't have the capacity for it. Considering alternative approaches, I realized that there is a natural embedding of the Opt2Code language into the Kotlin language. Indeed, omitting details, every DSL dialect created by the rules of the Opt2Code language ideally corresponds to the default methods of an interface, and script written by that dialect corresponds to the tree of method calls of that interface. So, the initial purpose of this new technology is to embed the Opt2Code language into the Kotlin language and take advantage of the Kotlin eco-system (which includes the Java eco-system!). You can find more information about the Opt2Code language [here](https://opt2code.com).


Let's consider the possible applications of the pattern in writing bash scripts. Comparing bash commands and top-level bash scripts, every bash command with a set of arguments can be considered a low-level interface with default functions, and a top-level bash script running with a different set of arguments can be considered a top-level interface depending on the low-level ones. The hierarchy of such interfaces can be constructed and used in DevOps scripts to manage infrastructure (IaC).

Finally, we can come to a new notion, code as infrastructure (CaI)! The last means that we develop code that creates code for managing infrastructure. Indeed, it seems that we can use the new technique to develop new libraries for generating code for Terraform, Helm, or even for high-level languages like Java and Kotlin. As a result, it can potentially lower barriers for developers to use other languages, making it more simple since we can take advantage of the Kotlin ecosystem and don't have to learn specific languages but libraries.

The code for using our technique as well as examples of its base usage are available here.


Copyright (c) 2024 metadatalang.com aka alexn0. All Rights Reserved.

For citing this article, you should use the link https://metadatalang.com.