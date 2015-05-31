package foo;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/** Simple example of how to start a new Camel context with a route based on
 * a Spring ApplcationContext from Java. 
 *
 * @author timbo
 */
public class SimpleSpring {
    
    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(2000);
        long t0 = System.currentTimeMillis();
        ClassPathXmlApplicationContext applicationContext1 = new ClassPathXmlApplicationContext(
                "META-INF/spring/simple-spring-context.xml");
        long t1 = System.currentTimeMillis();
        System.out.println("ApplicationContext creation took " + (t1 - t0));
        applicationContext1.start();
        long t2 = System.currentTimeMillis();
        System.out.println("ApplicationContext startup took " + (t2 - t1));
        Thread.sleep(5000);
        applicationContext1.stop();
    }
    
}
