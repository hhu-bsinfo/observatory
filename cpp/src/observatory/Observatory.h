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

#ifndef OBSERVATORY_OBSERVATORY_H
#define OBSERVATORY_OBSERVATORY_H

#include <log4cpp/Category.hh>
#include <nlohmann/json.hpp>

namespace Observatory {

class Observatory {

public:

    Observatory(nlohmann::json &config, std::string &resultPath, bool isServer, int connectionRetries,
                std::string &bindAddress, std::string &remoteAddress);

    Observatory(const Observatory &other) = delete;

    Observatory &operator=(const Observatory &other) = delete;

    ~Observatory() = default;

    void start();

private:

    log4cpp::Category &LOGGER = log4cpp::Category::getInstance("OBSERVATORY");

    const nlohmann::json config;
    const std::string resultPath;
    const bool isServer;
    const int connectionRetries;
    const std::string bindAddress;
    const std::string remoteAddress;

};

}

#endif
