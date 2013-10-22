import com.google.inject.AbstractModule
import edu.internet2.middleware.shibboleth.common.relyingparty.RelyingPartyConfiguration
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.runtime.NullObject
import org.springframework.context.ApplicationContext
import org.springframework.context.support.FileSystemXmlApplicationContext

import java.lang.reflect.Array

import static org.ratpackframework.groovy.RatpackScript.ratpack
import static org.ratpackframework.groovy.Template.groovyTemplate

class PrintStuffService {
    def doKeyValue = { key, value, context, int depth, int maxDepth ->
        if (depth < maxDepth) {
            delegate = context
            li {
                span key, class: "key"
                mkp.yield ": "
                span value, class: "value"
            }
        }
    }

    void printItem(String label, ApplicationContext applicationContext, MarkupBuilder context, int depth, int maxDepth) {
        doKeyValue label, applicationContext.class.name, context, depth, maxDepth
    }

    void printItem(String label, Object[] item, MarkupBuilder context, int depth, int maxDepth) {
        doKeyValue label, "Array", context, depth, maxDepth
    }

    void printItem(String label, Number item, MarkupBuilder context, int depth, int maxDepth) {
        doKeyValue label, item.toString(), context, depth, maxDepth
    }

    void printItem(String label, String item, MarkupBuilder context, int depth, int maxDepth) {
        doKeyValue label, item, context, depth, maxDepth
    }

    void printItem(String label, Boolean item, MarkupBuilder context, int depth, int maxDepth) {
        printItem(label, item.toString(), context, depth, maxDepth)
    }

    void printItem(String label, Class clazz, MarkupBuilder context, int depth, int maxDepth) {
        doKeyValue label, clazz.getName(), context, depth, maxDepth
    }

    void printItem(String label, Collection item, MarkupBuilder context, int depth, int maxDepth) {
        if (depth < maxDepth) {
            context.li {
                if (depth + 1 < maxDepth && item.size() > 0) {
                    input type: "checkbox"
                }
                span label, class: "key"
                mkp.yield ": "
                span item.getClass().getName(), class: "value"
                if (item.size() < 1) {
                    span "<empty>"
                } else {
                    if (depth + 1 < maxDepth) {
                        ul {
                            item.each {
                                printItem "<list item>", it != null ? it : "<null>", context, depth + 1, maxDepth
                            }
                        }
                    }
                }
            }
        }
    }

    void printItem(String label, Object item, MarkupBuilder context, int depth, int maxDepth) {
        if (depth < maxDepth) {
            context.li {
                if (depth + 1 < maxDepth) {
                    input type: "checkbox"
                }
                span label, class: "key"
                mkp.yield ": "
                span item.getClass().getName(), class: "value"
                printItem item?.properties, context, depth + 1, maxDepth
            }
        }
    }

    void printItem(String label, Map item, MarkupBuilder context, int depth, int maxDepth) {
        if (depth < maxDepth) {
            context.li {
                if (depth + 1 < maxDepth && !item.isEmpty()) {
                    input type: "checkbox"
                }
                span label, class: "key"
                mkp.yield ": "
                span item.getClass().getName(), class: "value"
                if (item.isEmpty()) {
                    span "<empty>"
                } else {
                    if (depth + 1 < maxDepth) {
                        printItem item, context, depth + 1, maxDepth
                    }
                }
            }
        }
    }

    void printItem(Map item, MarkupBuilder context, int depth, int maxDepth) {
        if (item.isEmpty()) {
            context.span "<empty"
        } else {
            if (depth < maxDepth) {
                context.ul {
                    item.each { key, value ->
                        printItem(key, value != null ? value : "<null>", context, depth, maxDepth)
                    }
                }
            }
        }
    }
}

class PrintStuffModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PrintStuffService)
    }
}

def applicationContext
try {
    applicationContext = new FileSystemXmlApplicationContext(["file:${System.getProperty("shib.home")}/conf/internal.xml", "file:${System.getProperty("shib.home")}/conf/service.xml"] as String[])
} catch (org.springframework.beans.factory.BeanDefinitionStoreException e) {
    applicationContext = null
}

ratpack {
    modules {
        register new PrintStuffModule()
    }
    handlers { PrintStuffService service ->
        handler {
            if (!applicationContext) {
                response.send("invalid Shibboleth configuration; Check the `shib.home` property")
            } else {
                next()
            }
        }
        get {
            render groovyTemplate("index.html", title: "My Ratpack App")
        }

        get("info/:maxDepth?/:bean?") {
            def bean = getPathTokens().bean
            def writer = new StringWriter()
            def m = new MarkupBuilder(writer)

            m.body {
                def contextMap = [:]
                if (bean) {
                    contextMap[bean] = applicationContext.getBean(bean)
                } else {
                    applicationContext.beanDefinitionNames.each {
                        contextMap[it] = applicationContext.getBean(it)
                    }
                }

                service.printItem contextMap, m, 0, getPathTokens().maxDepth?.toInteger() ?: 2
            }
            render groovyTemplate("blank.html", title: "Test me", body: writer.toString())
        }

        assets "public"
    }
}
