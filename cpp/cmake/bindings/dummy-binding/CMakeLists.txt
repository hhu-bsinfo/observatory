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

project(dummy-binding)
message(STATUS "Project " ${PROJECT_NAME})

set(OBSERVATORY_BENCHMARK_HEADER_FILE "bindings/dummy-binding/Benchmark.h" PARENT_SCOPE)
set(OBSERVATORY_BENCHMARK_CLASS_NAME "Dummy::Benchmark" PARENT_SCOPE)

include_directories(${OBSERVATORY_SRC_DIR})

set(SOURCE_FILES
        ${OBSERVATORY_SRC_DIR}/bindings/dummy-binding/Benchmark.cpp)

add_executable(${PROJECT_NAME} ${SOURCE_FILES})
set_target_properties(${PROJECT_NAME} PROPERTIES OUTPUT_NAME "observatory")

target_link_libraries(${PROJECT_NAME} log4cpp benchmark application)
install(TARGETS ${PROJECT_NAME})
