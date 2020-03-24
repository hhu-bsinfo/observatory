/*
 * Copyright (C) 2020 Heinrich-Heine-Universitaet Duesseldorf,
 * Institute of Computer Science, Department Operating Systems
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

#include <observatory/BuildConfig.h>
#include <log4cpp/OstreamAppender.hh>
#include <log4cpp/Category.hh>
#include <nlohmann/json.hpp>
#include <fstream>
#include <observatory/Observatory.h>
#include <application/util/SocketAddressConverter.h>
#include "application/util/args.hxx"
#include "application/util/LoggingLayout.h"

static const constexpr uint16_t DEFAULT_PORT = 2998;

static void setupLogging() {
    log4cpp::Appender *consoleAppender = new log4cpp::OstreamAppender("console", &std::cout);
    consoleAppender->setLayout(new LoggingLayout());

    log4cpp::Category& root = log4cpp::Category::getRoot();
    root.setPriority(log4cpp::Priority::DEBUG);
    root.addAppender(consoleAppender);
}

int main(int argc, char **argv) {
    Observatory::BuildConfig::printBanner();

    setupLogging();

    Observatory::Observatory::registerPrototypes();

    log4cpp::Category &LOGGER = log4cpp::Category::getInstance("APPLICATION");

    args::ArgumentParser parser("", "");
    parser.LongPrefix("--");
    parser.LongSeparator(" ");
    parser.ShortPrefix("-");

    args::HelpFlag help(parser, "help", "Show this help menu", {'h', "help"});
    args::Flag isServer(parser, "server", "Runs this instance in server mode.", {'s', "server"});
    args::ValueFlag<Observatory::SocketAddress, SocketAddressConverter> remoteAddress(parser, "remote", "The address to connect to.", {'r', "remote"});
    args::ValueFlag<Observatory::SocketAddress, SocketAddressConverter> bindAddress(parser, "address", "The address to bind to.", {'a', "address"}, Observatory::SocketAddress(DEFAULT_PORT));
    args::ValueFlag<std::string> configPath(parser, "config", "Path to the config JSON file. If empty, observatory will try to load 'config.json'", {'c', "config"}, "config.json");
    args::ValueFlag<std::string> resultPath(parser, "output", "Output path for the result files. If empty, observatory will save results in './result/'.", {'o', "output"}, "./result/");
    args::ValueFlag<uint32_t> connectionRetries(parser, "retries", "The amount of connection attempts.", {'t', "retries"}, 10);

    try {
        parser.ParseCLI(argc, argv);
    } catch(const args::Help &e) {
        std::cout << parser;
        return 0;
    } catch(const args::Completion &e) {
        LOGGER.error("Unable to parse arguments\n\033[0m %s\n", e.what());
        return 1;
    } catch(const args::ParseError &e) {
        std::ostringstream stream;
        stream << parser;
        LOGGER.error("Unable to parse arguments\n\033[0m %s\n\n\n%s", e.what(), stream.str().c_str());
        return 1;
    } catch(const args::ValidationError &e) {
        std::ostringstream stream;
        stream << parser;
        LOGGER.error("Unable to parse arguments\n\033[0m %s\n\n\n%s", e.what(), stream.str().c_str());
        return 1;
    }

    if(isServer && !remoteAddress) {
        LOGGER.error("Please specify the server address");
        return 1;
    }

    LOGGER.info("Loading configuration");

    std::ifstream configFile;
    configFile.open(configPath.Get());

    if(!configFile.is_open()) {
        LOGGER.error("Unable to open configuration file '%s'", configPath.Get().c_str());
        return 1;
    }

    nlohmann::json benchmarkConfig;

    try {
        configFile >> benchmarkConfig;
    } catch (nlohmann::detail::exception &e) {
        LOGGER.error("Unable to parse configuration file\n\033[0m %s\n", e.what());
        return 1;
    }

    LOGGER.info("Creating observatory instance");

    Observatory::Observatory observatory(benchmarkConfig, resultPath.Get(), isServer, connectionRetries.Get(),
            bindAddress.Get(), remoteAddress.Get());

    observatory.start();

    return 0;
}