package de.hhu.bsinfo.observatory.app;

import de.hhu.bsinfo.observatory.app.command.Root;
import de.hhu.bsinfo.observatory.app.util.InetSocketAddressConverter;
import java.net.InetSocketAddress;
import picocli.CommandLine;

public class Application {

    public static void main(String... args) {
        CommandLine cli = new CommandLine(new Root());
        cli.registerConverter(InetSocketAddress.class, new InetSocketAddressConverter(22222));
        cli.setCaseInsensitiveEnumValuesAllowed(true);
        cli.parseWithHandlers(
            new CommandLine.RunAll().useOut(System.out),
            CommandLine.defaultExceptionHandler().useErr(System.err),
            args);
    }
}
