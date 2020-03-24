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

#ifndef OBSERVATORY_SOCKETADDRESSCONVERTER_H
#define OBSERVATORY_SOCKETADDRESSCONVERTER_H

#include <string>
#include <observatory/util/SocketAddress.h>

class SocketAddressConverter {

public:

    void operator()(const std::string &names, const std::string &value, Observatory::SocketAddress &destination);

private:

    static std::vector<std::string> splitAddressString(std::string address);

private:

    static const constexpr char *DEFAULT_ADDRESS = "0.0.0.0";
    static const constexpr uint16_t DEFAULT_PORT = 2998;

};

#endif
