package de.hhu.bsinfo.observatory.app.command;

import de.hhu.bsinfo.observatory.Observatory;
import picocli.CommandLine;

@CommandLine.Command(
    name = "observatory",
    description = "",
    subcommands = { Benchmark.class }
)
public class Root implements Runnable {

    @Override
    public void run() {
        Observatory.printBanner();
    }
}