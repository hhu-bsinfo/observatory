package de.hhu.bsinfo.infinibench.app.command;

import de.hhu.bsinfo.infinibench.InfiniBench;
import picocli.CommandLine;

@CommandLine.Command(
    name = "infinibench",
    description = "",
    subcommands = { Benchmark.class }
)
public class Root implements Runnable {

    @Override
    public void run() {
        InfiniBench.printBanner();
    }
}