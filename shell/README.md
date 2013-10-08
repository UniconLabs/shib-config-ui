# Shell

1. install groovy
2. add groovy to `PATH`
3. download https://github.com/UniconLabs/shib-config-ui/blob/master/shell/src/main/groovy/shell.groovy
3. run `./shell.groovy <shib_install_path>`

## variables

* `applicationContext`: the Shibboleth application Context
* `printProps`: closure that prints all the properties of an object

        printProps applicationContext

* `printPropsColor`: closure that prints all the properties of an object, in ANSI color
