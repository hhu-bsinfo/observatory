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

cmake_minimum_required(VERSION 3.5)

include(ExternalProject)

# Set version
execute_process(COMMAND git describe --tags --abbrev=0 WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        OUTPUT_VARIABLE OBSERVATORY_VERSION ERROR_QUIET)
string(STRIP "${OBSERVATORY_VERSION}" OBSERVATORY_VERSION)
if (OBSERVATORY_VERSION STREQUAL "")
    set(OBSERVATORY_VERSION "v0.0.0")
endif ()

# Set git revision
execute_process(COMMAND git log -1 --format=%h --date=short HEAD WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        OUTPUT_VARIABLE OBSERVATORY_GIT_REV ERROR_QUIET)
string(STRIP "${OBSERVATORY_GIT_REV}" OBSERVATORY_GIT_REV)
if (OBSERVATORY_GIT_REV STREQUAL "")
    set(OBSERVATORY_GIT_REV "not available")
endif ()

# Set git branch
execute_process(COMMAND git rev-parse --symbolic-full-name --abbrev-ref HEAD
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        OUTPUT_VARIABLE OBSERVATORY_GIT_BRANCH ERROR_QUIET)
string(STRIP "${OBSERVATORY_GIT_BRANCH}" OBSERVATORY_GIT_BRANCH)
if (OBSERVATORY_GIT_BRANCH STREQUAL "")
    set(OBSERVATORY_GIT_BRANCH "master")
endif ()

# Set build date
string(TIMESTAMP OBSERVATORY_BUILD_DATE "%Y-%m-%d %H:%M:%S")

# Print build information
message("Observatory ${OBSERVATORY_VERSION} - git ${OBSERVATORY_GIT_REV}(${OBSERVATORY_GIT_BRANCH}), \
build date: ${OBSERVATORY_BUILD_DATE}")

# Set preprocessor defines
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DOBSERVATORY_VERSION='${OBSERVATORY_VERSION}'\
    -DOBSERVATORY_GIT_REV='${OBSERVATORY_GIT_REV}' -DOBSERVATORY_GIT_BRANCH='${OBSERVATORY_GIT_BRANCH}' \
    -DOBSERVATORY_BUILD_DATE='${OBSERVATORY_BUILD_DATE}'")

# Set source directory variables
set(OBSERVATORY_ROOT_DIR "${CMAKE_CURRENT_LIST_DIR}/..")
set(OBSERVATORY_SRC_DIR "${OBSERVATORY_ROOT_DIR}/src")

# Set output directory variables
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/ar)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

# Force out of source build
message(STATUS "Force out of source build check...")
string(COMPARE EQUAL "${CMAKE_SOURCE_DIR}" "${CMAKE_BINARY_DIR}" insource)
if (insource)
    message(FATAL_ERROR "Please do not build in your source dir. Use a dedicated build folder!")
endif ()

# Set compiler-flags
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Wall -I/usr/include/infiniband")

# For development: Uncomment this line to compile with sanitizers to debug memory errors and leaks
# set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -g -fno-omit-frame-pointer -fsanitize=address")

# Add nlohmann_json as external project
find_package(nlohmann_json REQUIRED)

# Add detector as external project
ExternalProject_Add(detector_git
        GIT_REPOSITORY https://github.com/hhu-bsinfo/detector.git
        PREFIX ${CMAKE_CURRENT_BINARY_DIR}/detector
        INSTALL_COMMAND "")

ExternalProject_Get_Property(detector_git binary_dir)
ExternalProject_Get_Property(detector_git source_dir)

add_library(detector STATIC IMPORTED)
set_property(TARGET detector PROPERTY IMPORTED_LOCATION ${binary_dir}/ar/libdetector.a)

add_dependencies(detector detector_git)

include_directories(${source_dir}/src)

# Add subdirectories
add_subdirectory(observatory)
add_subdirectory(benchmark)
add_subdirectory(socket-binding)
add_subdirectory(verbs-binding)
