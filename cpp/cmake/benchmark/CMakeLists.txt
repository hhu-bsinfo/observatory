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

project(benchmark)
message(STATUS "Project " ${PROJECT_NAME})

include_directories(${OBSERVATORY_SRC_DIR})

set(SOURCE_FILES
        ${OBSERVATORY_SRC_DIR}/observatory/util/SocketAddress.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/util/ValueFormatter.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/util/BenchmarkFactory.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/util/OperationFactory.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/result/Status.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/result/Measurement.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/result/OverheadMeasurement.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/result/ThroughputMeasurement.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/result/LatencyMeasurement.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/result/LatencyStatistics.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/phase/BenchmarkPhase.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/phase/InitializationPhase.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/phase/ConnectionPhase.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/phase/PreparationPhase.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/phase/FillReceiveQueuePhase.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/phase/WarmupPhase.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/phase/OperationPhase.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/phase/CleanupPhase.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/operation/Operation.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/operation/ThroughputOperation.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/operation/MessagingThroughputOperation.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/operation/RdmaThroughputOperation.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/operation/RdmaWriteThroughputOperation.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/operation/RdmaReadThroughputOperation.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/operation/BidirectionalThroughputOperation.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/operation/LatencyOperation.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/operation/MessagingLatencyOperation.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/operation/MessagingPingPongOperation.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/operation/RdmaLatencyOperation.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/operation/RdmaWriteLatencyOperation.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/operation/RdmaReadLatencyOperation.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/BuildConfig.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/Benchmark.cpp
        ${OBSERVATORY_SRC_DIR}/observatory/Observatory.cpp)

add_library(${PROJECT_NAME} STATIC ${SOURCE_FILES})

target_link_libraries(${PROJECT_NAME} nlohmann_json::nlohmann_json socket-binding detector ibverbs ibmad ibnetdisc pthread)