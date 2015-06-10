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



// generate route 2 and convert to XML
RoutesDefinition routes = new RoutesDefinition()
RouteDefinition route = routes.route()
route.from("timer://foo?fixedRate=true&period=200")
route.log("Hello from Java!")

CamelContext camelContext = new DefaultCamelContext()
camelContext.start()

println "# routes 1 = " + camelContext.getRoutes().size()

camelContext.addRouteDefinition(route)

println "# routes 2 = " + camelContext.getRoutes().size()




// shutdown
sleep(2000)                               
camelContext.stop()
