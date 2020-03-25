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

#ifndef OBSERVATORY_INETSOCKETADDRESS_H
#define OBSERVATORY_INETSOCKETADDRESS_H

#include <string>
#include <netinet/in.h>

namespace Observatory {

class SocketAddress {

public:

    SocketAddress() = default;

    SocketAddress(const std::string &hostname, uint16_t port);

    explicit SocketAddress(uint16_t port);

    explicit SocketAddress(const sockaddr_in &address);

    SocketAddress(const SocketAddress &other) = default;

    SocketAddress &operator=(const SocketAddress &other);

    ~SocketAddress() = default;

    const char* getHostname() const;

    uint16_t getPort() const;

    sockaddr_in getAddress() const;

    explicit operator std::string() const;

private:

    static const constexpr char *DEFAULT_ADDRESS = "0.0.0.0";

    std::string hostname;
    uint16_t port{};

    sockaddr_in address{};

};

}

#endif
