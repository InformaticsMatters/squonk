/**
 * Example of how to add a new route to a running Camel context.
 * The route is set as XML. The tricky bit is that any beans used in the route
 * need to be set to the registry before setting the route. The exact mecahnism 
 * for doing this is not clear.
*/

package foo

import org.apache.camel.model.language.XQueryExpression
import org.apache.camel.model.language.GroovyExpression
import org.apache.camel.model.*
import org.apache.camel.*
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.SimpleRegistry

// define route 1 as XML
String xml1 = '''<?xml version="1.0"?>
<routes xmlns="http://camel.apache.org/schema/spring">\n\
    <route id="bar">
        <from uri="timer://foo?fixedRate=true&amp;period=500"/>
        <process ref="simpleProcessor1"/>
        <log message="Hello from XML!"/>
    </route>
</routes>'''

// generate route 2 and convert to XML
RoutesDefinition routes = new RoutesDefinition()
RouteDefinition route = routes.route()
route.from("timer://foo?fixedRate=true&period=200")
route.log("Hello from Java!")
route.processRef("simpleProcessor2") // the reference to the bean in the registry

String xml2 = ModelHelper.dumpModelAsXml(null, routes)
println xml2

// create and start context
long t0 = System.currentTimeMillis();
SimpleRegistry registry = new SimpleRegistry()
CamelContext camelContext = new DefaultCamelContext(registry)
camelContext.start()
long t1 = System.currentTimeMillis();
System.out.println("Camel Context creation took " + (t1 - t0));

// add beans to the registry
registry.put("simpleProcessor1", new SimpleProcessor("simpleProcessor1"))
registry.put("simpleProcessor2", new SimpleProcessor("simpleProcessor2"))
// set route to context
RoutesDefinition routes1 = camelContext.loadRoutesDefinition(new ByteArrayInputStream(xml1.bytes))
camelContext.addRouteDefinitions(routes1.getRoutes())
long t3 = System.currentTimeMillis();
RoutesDefinition routes2 = camelContext.loadRoutesDefinition(new ByteArrayInputStream(xml2.bytes))
camelContext.addRouteDefinitions(routes2.getRoutes())
long t4 = System.currentTimeMillis();
System.out.println("New route creation took " + (t4 - t3));

// shutdown
sleep(5000)                               
camelContext.stop()
