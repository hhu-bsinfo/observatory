# Copyright (C) 2020 Heinrich-Heine-Universitaet Duesseldorf,
# Institute of Computer Science, Department Operating Systems
#
# This program is free software: you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation, either version 3 of the License,
# or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>

project(application)
message(STATUS "Project " ${PROJECT_NAME})

include_directories(${OBSERVATORY_SRC_DIR})

set(SOURCE_FILES
        ${OBSERVATORY_SRC_DIR}/application/util/args.hxx
        ${OBSERVATORY_SRC_DIR}/application/util/LoggingLayout.cpp
        ${OBSERVATORY_SRC_DIR}/application/util/SocketAddressConverter.cpp
        ${OBSERVATORY_SRC_DIR}/application/Application.cpp)

add_library(${PROJECT_NAME} ${SOURCE_FILES})

target_link_libraries(${PROJECT_NAME} log4cpp nlohmann_json::nlohmann_json benchmark)