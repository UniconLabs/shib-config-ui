#!/usr/bin/env groovy
import org.codehaus.groovy.tools.shell.Groovysh
import org.codehaus.groovy.tools.shell.IO
import jline.ANSIBuffer

def cli = new CliBuilder(usage: 'shell.groovy <dir>')

def options = cli.parse(args)
assert options.arguments() && options.arguments().size() == 1
def shibPath = options.arguments()[0]

def addLibs = { File dir ->
    dir.listFiles(new FilenameFilter() {
        @Override
        boolean accept(File d, String name) {
            return name.endsWith(".jar")
        }
    }).each { f ->
        this.class.classLoader.rootLoader.addURL(f.toURI().toURL())
    }
}

[new File(shibPath, "lib"), new File(shibPath, "lib/endorsed")].each {
    addLibs it
}

def binding = new Binding().with {
    setVariable("addLibs", addLibs)
    setVariable("shibPath", shibPath)
    setVariable("applicationContext", Class.forName("org.springframework.context.support.FileSystemXmlApplicationContext").newInstance(["file:${shibPath}/conf/internal.xml", "file:${shibPath}/conf/service.xml"] as String[]))
    setVariable("printProps", { o -> o.properties.each { key, value -> println "${key}(${value.getClass()}):\n\t${value}" } })
    setVariable("printPropsColor", { o -> o.properties.each { String key, value -> println new ANSIBuffer().yellow(key).blue("(${value.getClass()})").yellow(":").append("\n\t").append(value.toString()).toString() } })
    return it
}

def io = new IO().with {
    return it
}

def shell = new Groovysh(this.class.classLoader.rootLoader, binding, io)
shell.run([] as String[])