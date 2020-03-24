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

#ifndef OBSERVATORY_LOGGINGLAYOUT_H
#define OBSERVATORY_LOGGINGLAYOUT_H

#include <log4cpp/Layout.hh>

class LoggingLayout : public log4cpp::Layout {

public:
    LoggingLayout() = default;
    ~LoggingLayout() override = default;

    std::string format(const log4cpp::LoggingEvent& event) override;

private:

    static std::string getAnsiColorCode(log4cpp::Priority::Value priority);
    static std::string getPriorityName(log4cpp::Priority::Value priority);
};

#endif
