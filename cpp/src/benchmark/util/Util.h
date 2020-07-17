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

#ifndef OBSERVATORY_UTIL_H
#define OBSERVATORY_UTIL_H

namespace Observatory {

class Util {

public:

    Util() = delete;

    Util(const Util &other) = delete;

    Util& operator=(const Util &other) = delete;

    ~Util() = delete;

    template<typename Base, typename T>
    static inline bool instanceof(const T *ptr) {
        return dynamic_cast<const Base*>(ptr) != nullptr;
    }

};

}

#endif
