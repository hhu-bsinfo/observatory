package de.hhu.bsinfo.observatory.app.command;

import de.hhu.bsinfo.observatory.benchmark.Observatory;
import picocli.CommandLine;

@CommandLine.Command(
    name = "observatory",
    description = "",
    subcommands = { Benchmark.class, Clean.class }
)
public class Root implements Runnable {

    @Override
    public void run() {
        Observatory.printBanner();
    }
}