# **Spring**

## **JDK**​**动态代理和**​**CGLIB**​**区别**

「静态代理」

由程序员创建或由特定工具自动生成源代码，再对其编译。在程序运行前，代理类的.class文件就已经存在了

静态代理通常只代理一个类

静态代理事先知道要代理的是什么

「动态代理」

在程序运行时，运用反射机制动态创建而成

动态代理是代理一个接口下的多个实现类

动态代理不知道要代理什么东西，只有在运行时才知道

从性能上特性对比：JDK 动态代理要求目标对象必须实现至少一个接口，因为它基于接口生成代理类。而 CGLIB 动态代理不依赖于目标对象是否实现接口，可以代理没有实现接口的类，它通过继承或者代理目标对象的父类来实现代理。

从创建代理时的性能对比：JDK 动态代理通常比 CGLIB 动态代理创建速度更快，因为它不需要生成字节码文件。而 CGLIB 动态代理的创建速度通常比较慢，因为它需要生成字节码文件。另外，JDK 代理生成的代理类较小，占用较少的内存，而 CGLIB 生成的代理类通常较大，占用更多的内存。

从调用时的性能对比：JDK 动态代理在方法调用时需要通过反射机制来调用目标方法，因此性能略低于 CGLIB，尽管 JDK 动态代理在 Java 8 中有了性能改进，但 CGLIB 动态代理仍然具有更高的方法调用性能。CGLIB 动态代理在方法调用时不需要通过反射，直接调用目标方法，通常具有更高的方法调用性能，同时无需类型转换。

选择使用 JDK 动态代理还是 CGLIB 动态代理取决于具体需求。如果目标对象已经实现了接口，并且您更关注创建性能和内存占用，那么 JDK 动态代理可能是一个不错的选择。如果目标对象没有实现接口，或者您更关注方法调用性能，那么 CGLIB 动态代理可能更合适。综上所述，这两种代理方式各有优势，根据实际情况进行选择是明智的，Spring 默认情况如果目标类实现了接口用 JDK 代理否则用 CGLIB。而 SpringBoot 默认用 CGLIB，所以用哪个问题都不大。

## **对AOP的理解**

系统是由许多不同的组件所组成的，每一个组件各负责一块特定功能。除了实现自身核心功能之外，这些组件还经常承担着额外的职责。

例如日志、事务管理和安全这样的核心服务经常融入到自身具有核心业务逻辑的组件中去。这些系统服务经常被称为横切关注点，因为它们会跨越系统的多个组件。

当我们需要为分散的对象引入公共行为的时候，OOP则显得无能为力。也就是说，OOP允许你定义从上到下的关系，但并不适合定义从左到右的关系。例如日志功能。日志代码往往水平地散布在所有对象层次中，而与它所散布到的对象的核心功能毫无关系。在OOP设计中，它导致了大量代码的重复，而不利于各个模块的重用。

AOP：将程序中的交叉业务逻辑（比如安全，日志，事务等），封装成一个切面，然后注入到目标对象（具体业务逻辑）中去。AOP可以对某个对象或某些对象的功能进行增强，比如对象中的方法进行增强，可以在执行某个方法之前额外的做一些事情，在某个方法执行之后额外的做一些事情

## **介绍下 SpringA**​**OP**​**​ 的底层实现**

Spring AOP 是 Spring 框架的一个重要组成部分，用于实现面向切面编程。它通过在方法调用前、调用后或异常抛出时插入通知，允许开发者在核心业务逻辑之外执行横切关注点的代码。

底层实现主要分两部分：创建 AOP 动态代理和调用代理

在启动 Spring 会创建 AOP 动态代理：首先通过 AspectJ 解析切点表达式：在创建代理对象时，Spring AOP 使用 AspectJ 来解析切点表达式。它会根据定义的条件匹配目标 Bean 的方法。如果 Bean 不符合切点的条件，将跳过，否则将会通动态代理包装 Bean 对象：具体会根据目标对象是否实现接口来选择使用 JDK 动态代理或 CGLIB 代理。这使得 AOP 可以适用于各种类型的目标对象。

在调用阶段：

1. Spring AOP 使用责任链模式来管理通知的执行顺序。通知拦截链包括前置通知、后置通知、异常通知、最终通知和环绕通知，它们按照配置的顺序形成链式结构。
2. 通知的有序执行：责任链确保通知按照预期顺序执行。前置通知在目标方法执行前执行，后置通知在目标方法成功执行后执行，异常通知在方法抛出异常时执行，最终通知无论如何都会执行，而环绕通知包裹目标方法，允许在方法执行前后添加额外的行为。

综上所述，Spring AOP 在创建启动阶段使用 AspectJ 解析切点表达式如果匹配使用动态代理，而在调用阶段使用责任链模式确保通知的有序执行。这些机制共同构成了 Spring AOP 的底层实现。

## **Spring**​**AOP**​**失效**

内部方法调用：如果在同一个类中的一个方法调用另一个方法，AOP 通知可能不会触发，因为 AOP 通常是通过代理对象拦截外部方法调用的。解决方式是注入本类对象进行调用，或者设置暴露当前代理对象到本地线程，可以通过 AopContext.currentProxy() 拿到当前正在调用的动态代理对象。

静态方法：AOP 通常无法拦截静态方法的调用，因为静态方法不是通过对象调用的。解决方法是将静态方法调用替换为实例方法调用，或者考虑其他技术来实现横切关注点。

AOP 配置问题：错误的 AOP 配置可能导致通知不正确地应用于目标方法，或者在不希望的情况下应用。解决方法是仔细检查 AOP 配置，确保切点表达式和通知类型正确配置。

代理问题：如果代理对象不正确地创建或配置，AOP 通知可能无法生效。解决方法是调试底层源码确保代理对象正确创建，并且 AOP 通知能够拦截代理对象的方法调用。

## **对IOC的理解**

ioc容器：实际上就是个map（key，value），里面存的是各种对象（在xml里配置的bean节点、@repository、@service、@controller、@component），在项目启动的时候会读取配置文件里面的bean节点，根据全限定类名使用反射创建对象放到map里、扫描到打上上述注解的类还是通过反射创建对象放到map里。

这个时候map里就有各种对象了，接下来我们在代码里需要用到里面的对象时，再通过DI注入（autowired、resource等注解，xml里bean节点内的ref属性，项目启动的时候会读取xml节点ref属性根据id注入，也会扫描这些注解，根据类型或id注入；id就是对象名）。

**控制反转：**

没有引入IOC容器之前，对象A依赖于对象B，那么对象A在初始化或者运行到某一点的时候，自己必须主动去创建对象B或者使用已经创建的对象B。无论是创建还是使用对象B，控制权都在自己手上。

引入IOC容器之后，对象A与对象B之间失去了直接联系，当对象A运行到需要对象B的时候，IOC容器会主动创建一个对象B注入到对象A需要的地方。

通过前后的对比，不难看出来：对象A获得依赖对象B的过程,由主动行为变为了被动行为，控制权颠倒过来了，这就是“控制反转”这个名称的由来。

全部对象的控制权全部上缴给“第三方”IOC容器，所以，IOC容器成了整个系统的关键核心，它起到了一种类似“粘合剂”的作用，把系统中的所有对象粘合在一起发挥作用，如果没有这个“粘合剂”，对象与对象之间会彼此失去联系，这就是有人把IOC容器比喻成“粘合剂”的由来。

**依赖注入：**

“获得依赖对象的过程被反转了”。控制被反转之后，获得依赖对象的过程由自身管理变为了由IOC容器主动注入。依赖注入是实现IOC的方法，就是由IOC容器在运行期间，动态地将某种依赖关系注入到对象之中。

## **如何实现一个IOC容器**

1.配置文件中指定需要扫描的包路径

2.定义一些注解，分别表示访问控制层、业务服务层、数据持久层、依赖注入注解、获取配置文件注解

3.扫描当前路径下所有以.class结尾的文件加到Set集合,

4.遍历这个set集合，获取在类上有指定注解的类，并将其交给IOC容器，定义一个安全的Map用来存储这些对象  

5.遍历这个IOC容器，获取到每一个类的实例，判断是否依赖，然后进行递归注入

## **Spring IoC 的实现机制是什么？**

Spring 的 IoC 底层实现机制主要依赖于以下几个关键组件和技术：

反射：Spring 使用 Java 的反射机制来实现动态创建和管理 Bean 对象。通过反射，Spring 可以在运行时动态地实例化 Bean 对象、调用 Bean 的方法和设置属性值。

配置元数据：Spring 使用配置元数据来描述 Bean 的定义和依赖关系。配置元数据可以通过 XML 配置文件、注解和 Java 代码等方式进行定义。Spring 在启动时会解析配置元数据，根据配置信息创建和管理 Bean 对象。

Bean 定义：Spring 使用 Bean 定义来描述 Bean 的属性、依赖关系和生命周期等信息。Bean 定义可以通过 XML 配置文件中的`<bean>`元素、注解和 Java 代码中的 @Bean 注解等方式进行定义。Bean 定义包含了 Bean 的类名、作用域、构造函数参数、属性值等信息。

Bean 工厂：Spring 的 Bean 工厂负责创建和管理 Bean 对象。Bean 工厂可以是 BeanFactory 接口的实现，如 DefaultListableBeanFactory。Bean 工厂负责解析配置元数据，根据 Bean 定义创建 Bean 对象，并将其放入容器中进行管理。

依赖注入：Spring 使用依赖注入来解决 Bean 之间的依赖关系。通过依赖注入，Spring 容器负责将 Bean 所依赖的其他 Bean 实例注入到它们之中。Spring 使用反射和配置元数据来确定依赖关系，并在运行时进行注入。

总结起来，Spring 的 IoC 底层实现机制主要依赖于反射、配置元数据、Bean 定义、Bean 工厂和依赖注入等技术和组件。通过这些机制，Spring 实现了 Bean 的创建、配置和管理，以及 Bean 之间的解耦和依赖注入

## **SpringHttp请求过程**

1.浏览器的http请求，被Tomcat容器的监听器监听到

2.请求通过Filter链，到达前置分发器DispatcherServlet

3.前置分发器DispatcherServlet接收到HTTP请求之后，通过解析HTTP请求的URL获取URI，根据URI从处理器映射HandlerMappings当中获取请求对应的处理器Handler和处理器拦截器HandlerInterceptor

4.前置分发器DispatcherServlet根据获取得到的Handler选择合适的适配器HandlerAdapter。如果成功获取适配器HandlerAdapter，先调用HandlerInterceptor#preHandler，然后调用处理器Handler，也就是Controller方法

5.调用Service的业务处理方法,调用DAO的数据处理方法,最后依次返回结果

## **Spring Bean的生命周期**

1.解析类得到BeanDefinition

2.如果有多个构造方法，则要推断构造方法

3.确定好构造方法后，进行实例化得到一个对象

4.对对象中的加了@Autowired注解的属性进行属性填充

5.回调Aware方法，比如BeanNameAware，BeanFactoryAware

6.调用BeanPostProcessor的初始化前的方法

7.调用初始化方法

8.调用BeanPostProcessor的初始化后的方法，在这里会进行AOP

9.如果当前创建的bean是单例的则会把bean放入单例池

10.使用bean

11. Spring容器关闭时调用DisposableBean中destory()方法

## **BeanFactory和ApplicationContext有什么区别**

ApplicationContext是BeanFactory的子接口

ApplicationContext提供了更完整的功能：

①继承MessageSource，因此支持国际化。

②统一的资源文件访问方式。

③提供在监听器中注册bean的事件。

④同时加载多个配置文件。

⑤载入多个（有继承关系）上下文 ，使得每一个上下文都专注于一个特定的层次，比如应用的web层

BeanFactroy采用的是延迟加载形式来注入Bean的，即只有在使用到某个Bean时(调用
getBean())，才对该Bean进行加载实例化。这样，我们就不能发现一些存在的Spring的配置问题。如果Bean的某一个属性没有注入，BeanFacotry加载后，直至第一次使用调用getBean方法才会抛出异常

ApplicationContext，它是在容器启动时，一次性创建了所有的Bean。这样，在容器启动时，我们就可以发现Spring中存在的配置错误，这样有利于检查所依赖属性是否注入。
ApplicationContext启动后预载入所有的单实例Bean，通过预载入单实例bean ,确保当你需要的时候，你就不用等待，因为它们已经创建好了。

相对于基本的BeanFactory，ApplicationContext 唯一的不足是占用内存空间。当应用程序配置Bean较多时，程序启动较慢。

BeanFactory通常以编程的方式被创建，ApplicationContext还能以声明的方式创建，如使用ContextLoader。

BeanFactory和ApplicationContext都支持BeanPostProcessor、BeanFactoryPostProcessor的使用，但两者之间的区别是：BeanFactory需要手动注册，而ApplicationContext则是自动注册

## **单例Bean是线程安全的么**

如果 Bean 是 无状态的（没有可变的成员变量），比如大多数 Service 或 Controller，那么它是线程安全的；

如果 Bean 包含可变的共享状态（如实例变量），则需要开发者自行保证线程安全，比如使用 ThreadLocal、Atomic 类、加锁，或者避免使用成员变量。改为原型作用域（@Scope("prototype")）每次 new Bean()

## **Spring 框架中都用到了哪些设计模式**

​**简单工厂**​:

BeanFactory.getBean() ，Spring中的BeanFactory就是简单工厂模式的体现，根据传入一个唯一的标识来获得Bean对象，但是否是在传入参数后创建还是传入参数前创建这个要根据具体情况来定

​**工厂模式**​:

实现了FactoryBean接口，spring会在使用getBean()调用获得该bean时，会自动调用该bean的getObject()方法，所以返回的不是factory这个bean，而是这个bean.getOjbect()方法的返回值。

​**单例模式**​:

spring对单例的实现： spring中的单例模式完成了后半句话，即提供了全局的访问点BeanFactory。但没有从构造器级别去控制单例，这是因为spring管理的是任意的java对象

**代理模式 ​**

切面在应用运行的时刻被织入。一般情况下，在织入切面时，AOP容器会为目标对象创建动态的创建一个代理对象。SpringAOP就是以这种方式织入切面的

**策略模式 ​**

根据是否有接口，选择用jdk动态代理还是cglib

**观察者模式 ​**

spring的事件驱动模型使用的是 观察者模式 ，Spring中Observer模式常用的地方是listener的实现

模板方法

装饰器模式Decorator

## **为什么要使用三级缓存，二级缓存不能解决吗?**

![](file:////Users/chenjinxiang/Library/Containers/com.kingsoft.wpsoffice.mac/Data/tmp/wps-chenjinxiang/ksohtml//wps4.jpg)

![](file:////Users/chenjinxiang/Library/Containers/com.kingsoft.wpsoffice.mac/Data/tmp/wps-chenjinxiang/ksohtml//wps5.jpg)

Spring 主要通过 三级缓存机制 来解决 单例 Bean 的 setter 注入（属性注入）循环依赖问题。

具体来说，Spring 在创建 Bean 时会经历 实例化 → 属性填充 → 初始化 三个阶段。当 A 依赖 B、B 又依赖 A 时：

Spring 先调用 A 的构造方法，得到一个原始对象（尚未注入属性）；

然后将 A 的 ObjectFactory 工厂 放入三级缓存（singletonFactories）；

接着去创建 B，在 B 需要注入 A 时，Spring 会从三级缓存中取出 A 的工厂，提前暴露一个早期引用（可能是原始对象，也可能是 AOP 代理对象）；

这个早期引用会被放入二级缓存（earlySingletonObjects），供后续依赖使用；

最终 A 和 B 都能完成初始化，并放入一级缓存（singletonObjects）。

为什么需要三级缓存？

二级缓存只能存原始对象，但若 A 需要 AOP 代理，就必须在注入时生成代理。三级缓存通过 ObjectFactory 实现 按需创建代理，保证无论是否发生循环依赖，注入的都是最终正确的 Bean。

可以，三级缓存的功能是只有真正发生循环依赖的时候，才去提前生成代理对象，否则只会「创建一个工厂并将其放入到三级缓存」中，但是不会去通过这个工厂去真正创建对象。如果使用二级缓存解决循环依赖，意味着所有 Bean 在实例化后就要完成 AOP 代理，这样「违背了 Spring 设计的原则」，Spring 在设计之初就是在 Bean 生命周期的最后一步来完成 AOP 代理，而不是在实例化后就立马进行 AOP 代理。

## **@Autowired 和 @Resource 有什么区别?**

“@Autowired 是 Spring 提供的注解，默认按 类型（byType） 装配，支持构造器注入和 @Qualifier 指定名称，功能更强大；

@Resource 是 Java 标准注解（JSR-250），默认按 名称（byName） 装配，可移植性更好，但不支持构造器注入。

在 Spring 项目中，我们通常优先使用 @Autowired，尤其是配合构造器注入；如果需要跨框架兼容，才考虑 @Resource

## **Spring事务的实现方式**​**和传播行为**

在使用Spring框架时，可以有两种使用事务的方式，一种是编程式的transactionalTemplate，一种是申明式的，

@Transactional注解就是申明式的。

首先，事务这个概念是数据库层面的，Spring只是基于数据库中的事务进行了扩展，以及提供了一些能让程序员更加方便操作事务的方式。

比如我们可以通过在某个方法上增加@Transactional注解，就可以开启事务，这个方法中所有的sql都会在一个事务中执行，统一成功或失败。

在一个方法上加了@Transactional注解后，Spring会基于这个类生成一个代理对象，会将这个代理对象作为bean，当在使用这个代理对象的方法时，如果这个方法上存在@Transactional注解，那么代理逻辑会先把事务的自动提交设置为false，然后再去执行原本的业务逻辑方法，如果执行业务逻辑方法没有出现异常，那么代理逻辑中就会将事务进行提交，如果执行业务逻辑方法出现了异常，那么则会将事务进行回滚。

当然，针对哪些异常回滚事务是可以配置的，可以利用@Transactional注解中的rollbackFor属性进行配置，默认情况下会对RuntimeException和Error进行回滚。

![](file:////Users/chenjinxiang/Library/Containers/com.kingsoft.wpsoffice.mac/Data/tmp/wps-chenjinxiang/ksohtml//wps6.jpg)

## **spring事务什么时候会失效**

spring事务的原理是AOP，进行了切面增强，那么失效的根本原因是这个AOP不起作用了！常见情况有如下几种

1、发生自调用，类里面使用this调用本类的方法（this通常省略），此时这个this对象不是代理类，而是UserService对象本身！

- 本类中注入当前bean
- 设置暴露当前代理对象到本地线程，可以通过 AopContext.currentProxy () 拿到当前正在调用的动态代理对象

@EnableAspectJAutoProxy(exposeProxy = true)

配置问题

方法不是public的

@Transactional 只能用于 public 的方法上，否则事务不会失效，如果要用在非 public 方法上，可以开启 AspectJ 代理模式。

数据库不支持事务

没有被spring管理配置为bean

异常被吃掉，事务不会回滚

多线程事务（spring的事务都是本线程的threadLocal）

- 用编程式事务，手动提交回滚
- 分布式事务 2pc/3pc

## **Springboot自动装配原理**

1、当启动springboot应用程序的时候，会先创建SpringApplication的对象，在对象的构造方法中会进行某 些参数的初始化工作，最主要的是判断当前应用程序的类型以及初始化器和监听器，在这个过程中会加载整个应用 程序中的spring.factories文件，将文件的内容放到缓存对象中，方便后续获取。

2、SpringApplication对象创建完成之后，开始执行run方法，来完成整个启动，启动过程中最主要的有两个方法，第一叫做prepareContext,第二个叫做refreshContext,在这两个关键步骤中完整了自动装配的核心功能，前面的处理逻辑包含了上下文对象的创建，banner的打印，异常报告期的准备等各个准备工作，方便后续来进行调用.

3、在prepareContext方法中主要完成的是对上下文对象的初始化操作，包括了属性值的设置，在整个过程中有一个非常事要的方法，叫做load, load主要完成一件事，将当前启动类做为一个beanDefinition注册至Registry中，方便后续在进行BeanFactoryPostProcesso碉用时，找到对应的主类，来完@SpringBootApplicaiton,@EnableAutoConfiguration等注解的解析工作

4、在refreshcontext方法中会进行整个容器刷新过程，会调用中spring中的refresh方法，refresh中有13个 非常关键的方法，来完成整个spring用程序的启动，在自动装配过程中，会调用 invokeBeanFactoryPostProcessor,在此方法中主要是对ConfigurationClassPostProcussor类的处理，这次 是BFPP的子类也是BDRPP的子类，在调用的时候会先调用BDRPP中的postProcessBeanDefinitionRegistry方 法，然后调用postProcessBeanFactory方法，在执行postProcessBeanDefinitionRegistry的时候回解析处理各注解，包含@PropertySource,@ComponentScan,@ComponentScans, @Bean,@lmport等注解，最主要的是

@lmport注解的解析

5、在解析@lmport注解的时候，会有一个getlmports的方法，从主类开始递归解析注解，把所有包含 @lmport的注解都解析到，然后在processimport方法中对Import的类进行分类，在后续过程中会调用deferredlmportSelectorHandler中的process方法，来完整EnableAutoConfiguration的加载

## **如何理解 Spring Boot 中的 Starter**

使用spring + springmvc使用，如果需要引入mybatis等框架，需要到xml中定义mybatis需要的bean

starter就是定义一个starter的jar包，写一个@Configuration配置类、将这些bean定义在里面，然后在starter包的META-INF/spring.factories中写入该配置类，springboot会按照约定来加载该配置类

开发人员只需要将相应的starter包依赖进应用，进行相应的属性配置（使用默认配置时，不需要配置），就可以直接进行代码开发，使用对应的功能了，比如mybatis-spring-boot--starter，springboot-starter-redis

## **SpringBoot 如何自定义 Starter**

在开发分布式 Springboot 项目时，自定义 Starter 是一定会用到的。以下是创建自定义 Spring Boot Starter 的基本步骤：

1.创建项目结构：创建一个 Maven 或 Gradle 项目，确保项目结构符合标准的约定。通常，项目结构包括 src/main/java 用于存放 Java 代码和 src/main/resources 用于存放资源文件。

2.编写自动配置类：创建一个自动配置类，该类负责配置自定义 Starter 的功能。在自动配置类上使用 @Configuration 注解，并通过其他注解如 @ConditionalOnClass、@ConditionalOnProperty 等来定义条件，以确保只有在满足特定条件时才会应用配置。

3.提供属性配置：如果您的 Starter 需要配置属性，可以在 src/main/resources/application.properties 或 src/main/resources/application.yml 中定义属性。这些属性可以在自动配置类中使用 @Value 注解注入。

4.创建 META-INF/spring.factories 文件：在项目的资源目录中创建 META-INF/spring.factories 文件。在这个文件中，注册您的自动配置类，以便 Spring Boot 能够自动识别和加载它。

5.定义 Starter 依赖：在自定义 Starter 的 pom.xml 文件中，定义 Spring Boot 的核心依赖以及您的 Starter 所依赖的其他库。

6.测试和文档：编写单元测试和集成测试，以确保自定义 Starter 的功能和配置正确。同时，提供详细的文档和示例，以便用户能够正确配置和使用您的 Starter。

7.发布仓库：将自定义 Starter 打包，并发布到 Maven 中央仓库或私有仓库，以便其他项目可以引入和使用。

总之，自定义一个 Spring Boot Starter 需要遵循上述步骤，其中创建 META-INF/spring.factories 文件是关键，因为它告诉 Spring Boot 如何自动装配您的功能。这样，其他项目可以方便地引入您的 Starter，实现功能的快速集成。

## **MyBatis 与Hibernate 有哪些不同**

​**开发速度的对比**​：
Hibernate的真正掌握要比Mybatis难些。Mybatis框架相对简单很容易上手，但也相对简陋些。
比起两者的开发速度，不仅仅要考虑到两者的特性及性能，更要根据项目需求去考虑究竟哪一个更适合项目开发，比如：一个项目中用到的复杂查询基本没有，就是简单的增删改查，这样选择hibernate效率就很快了，因为基本的sql语句已经被封装好了，但是对于一个大型项目，复杂语句较多，这样再去选择hibernate就不是一个太好的选择，选择mybatis就会加快许多，而且语句的管理也比较方便。
​**开发工作量的对比**​：
Hibernate和MyBatis都有相应的代码生成工具。可以生成简单基本的DAO层方法。针对高级查询，Mybatis需要手动编写SQL语句，以及ResultMap。而Hibernate有良好的映射机制，开发者无需关心SQL的生成与结果映射，可以更专注于业务流程

**sql**​**优化方面：**
Hibernate的查询会将表中的所有字段查询出来，这一点会有性能消耗。Hibernate也可以自己写SQL来指定需要查询的字段，但这样就破坏了Hibernate开发的简洁性。而Mybatis的SQL是手动编写的，所以可以按需求指定查询的字段。
Hibernate HQL语句的调优需要将SQL打印出来，而Hibernate的SQL被很多人嫌弃因为太丑了。
MyBatis的SQL是自己手动写的所以调整方便。但Hibernate具有自己的日志统计。Mybatis本身不带日志统计，使用Log4j进行日志记录。
**对象管理的对比：**
Hibernate 是完整的对象/关系映射解决方案，它提供了对象状态管理（state management）的功能，
使开发者不再需要理会底层数据库系统的细节。也就是说，相对于常见的 JDBC/SQL 持久层方案中需要
管理 SQL 语句，Hibernate采用了更自然的面向对象的视角来持久化 Java 应用中的数据。
换句话说，使用 Hibernate 的开发者应该总是关注对象的状态（state），不必考虑 SQL 语句的执行。
这部分细节已经由 Hibernate 掌管妥当，只有开发者在进行系统性能调优的时候才需要进行了解。而
MyBatis在这一块没有文档说明，用户需要对对象自己进行详细的管理。
缓存机制对比：
相同点：都可以实现自己的缓存或使用其他第三方缓存方案，创建适配器来完全覆盖缓存行为。
不同点：Hibernate的二级缓存配置在SessionFactory生成的配置文件中进行详细配置，然后再在具体
的表-对象映射中配置是哪种缓存。
MyBatis的二级缓存配置都是在每个具体的表-对象映射中进行详细配置，这样针对不同的表可以自定义
不同的缓存机制。并且Mybatis可以在命名空间中共享相同的缓存配置和实例，通过Cache-ref来实现。
两者比较：因为Hibernate对查询对象有着良好的管理机制，用户无需关心SQL。所以在使用二级缓存
时如果出现脏数据，系统会报出错误并提示。
而MyBatis在这一方面，使用二级缓存时需要特别小心。如果不能完全确定数据更新操作的波及范围，
避免Cache的盲目使用。否则，脏数据的出现会给系统的正常运行带来很大的隐患。
Hibernate功能强大，数据库无关性好，O/R映射能力强，如果你对Hibernate相当精通，而且对
Hibernate进行了适当的封装，那么你的项目整个持久层代码会相当简单，需要写的代码很少，开发速
度很快，非常爽。
Hibernate的缺点就是学习门槛不低，要精通门槛更高，而且怎么设计O/R映射，在性能和对象模型之
间如何权衡取得平衡，以及怎样用好Hibernate方面需要你的经验和能力都很强才行。
iBATIS入门简单，即学即用，提供了数据库查询的自动对象绑定功能，而且延续了很好的SQL使用经
验，对于没有那么高的对象模型要求的项目来说，相当完美。
iBATIS的缺点就是框架还是比较简陋，功能尚有缺失，虽然简化了数据绑定代码，但是整个底层数据库
查询实际还是要自己写的，工作量也比较大，而且不太容易适应快速数据库修改
