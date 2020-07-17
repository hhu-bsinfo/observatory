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

#ifndef OBSERVATORY_VERSION_H
#define OBSERVATORY_VERSION_H

namespace Observatory {

/**
 * Holds information about the build configuration and program version.
 *
 * @author Fabian Ruhland, Fabian.Ruhland@hhu.de
 * @date March 2020
 */
class BuildConfig {

private:

    static const constexpr char banner[] =
            "   ____  ____ _____ __________ _    _____  __________  ______  __  # Version         : %s\n"
            "  / __ \\/ __ ) ___// ____/ __ \\ |  / /   |/_  __/ __ \\/ __ \\ \\/ /  # Build Date      : %s\n"
            " / / / / __  \\__ \\/ __/ / /_/ / | / / /| | / / / / / / /_/ /\\  /   # Git Branch      : %s\n"
            "/ /_/ / /_/ /__/ / /___/ _, _/| |/ / ___ |/ / / /_/ / _, _/ / /    # Git Commit      : %s\n"
            "\\____/_____/____/_____/_/ |_| |___/_/  |_/_/  \\____/_/ |_| /_/     # Implementation  : %s (%s)\n";

public:

    static const char *VERSION;
    static const char *GIT_REV;
    static const char *GIT_BRANCH;
    static const char *BUILD_DATE;
    static const char *BENCHMARK_HEADER_FILE;
    static const char *BENCHMARK_CLASS_NAME;

public:

    static void printBanner();

};

}

#endif
