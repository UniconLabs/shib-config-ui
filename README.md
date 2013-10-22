# shib-config

## Shibboleth Context Explorer

To quickly run the context explorer, run

```shell
./gradlew -Pshib.home=$SHIBBOLETH_HOME :ratpack:run
```

_NOTE_: You should either set the variable `SHIBBOLETH_HOME` or substitute with the location of your installation.

After starting the server, go to http://localhost:5050/info. This will show you the context with the default configuration,
which is to show the full application context, 2 deep. The general form of the url is `http://localhost:5050/$depth/$bean`
where `$depth` is an integer (probably > 2) of how deep you want the tree to be (this must be limited to prevent cyclic
calls) and `$bean` is the name of the bean you want to explore. If you use `$bean`, you must specify `$depth`, but you can
specify only `$depth` to explore the full context.

### Example URLs

* http://localhost:5050/info/2
* http://localhost:5050/info/3/shibboleth.RelyingPartyConfigurationManager
* http://localhost:5050/info/5/shibboleth.AttributeResolver
