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
#include "args.hxx"
#include "LoggingLayout.h"

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

    log4cpp::Category& LOGGER = log4cpp::Category::getInstance("APPLICATION");

    args::ArgumentParser parser("benchmark", "");
    args::HelpFlag help(parser, "help", "Show this help menu", {'h', "help"});

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
    }

    return 0;
}