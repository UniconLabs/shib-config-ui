package net.unicon.iamlabs.ssh

import org.apache.sshd.SshServer
import org.apache.sshd.server.Command
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.shell.ProcessShellFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class SshShellService implements ApplicationContextAware, InitializingBean, DisposableBean {
    ApplicationContext applicationContext

    int port = 2222
    Thread thread
    SshServer sshServer
    org.apache.sshd.common.Factory<Command> shellFactory = new GroovyShellFactory()

    @Override
    void afterPropertiesSet() throws Exception {
        assert port != 0
        assert shellFactory != null

        sshServer = SshServer.setUpDefaultServer().with {
            port = this.port
            shellFactory = this.shellFactory
            keyPairProvider = new SimpleGeneratorHostKeyProvider("/tmp/hostkey.ser")
            return it
        }
        sshServer.start()
    }

    @Override
    void destroy() throws Exception {
        sshServer.stop()
    }
}

class GroovyShellCommand implements Command {
    InputStream inputStream
    OutputStream outputStream
    OutputStream errorStream
    ExitCallback exitCallback

    EnumSet<ProcessShellFactory.TtyOptions> ttyOptions

    @Override
    void start(Environment env) throws IOException {
    }

    @Override
    void destroy() {

    }
}

class GroovyShellFactory implements org.apache.sshd.common.Factory<Command> {
    EnumSet<ProcessShellFactory.TtyOptions> ttyOptions

    @Override
    Command create() {
        return new GroovyShellCommand(ttyOptions: ttyOptions)
    }
}