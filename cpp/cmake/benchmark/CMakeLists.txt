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
        ${OBSERVATORY_SRC_DIR}/benchmark/util/SocketAddress.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/util/ValueFormatter.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/util/BenchmarkFactory.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/util/OperationFactory.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/result/Status.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/result/Measurement.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/result/OverheadMeasurement.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/result/ThroughputMeasurement.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/result/LatencyMeasurement.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/result/LatencyStatistics.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/phase/BenchmarkPhase.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/phase/InitializationPhase.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/phase/ConnectionPhase.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/phase/PreparationPhase.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/phase/FillReceiveQueuePhase.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/phase/WarmupPhase.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/phase/OperationPhase.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/phase/CleanupPhase.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/operation/Operation.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/operation/ThroughputOperation.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/operation/MessagingThroughputOperation.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/operation/RdmaThroughputOperation.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/operation/RdmaWriteThroughputOperation.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/operation/RdmaReadThroughputOperation.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/operation/BidirectionalThroughputOperation.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/operation/LatencyOperation.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/operation/MessagingLatencyOperation.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/operation/MessagingPingPongOperation.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/operation/RdmaLatencyOperation.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/operation/RdmaWriteLatencyOperation.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/operation/RdmaReadLatencyOperation.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/BuildConfig.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/Benchmark.cpp
        ${OBSERVATORY_SRC_DIR}/benchmark/Observatory.cpp)

add_library(${PROJECT_NAME} STATIC ${SOURCE_FILES})

target_link_libraries(${PROJECT_NAME} nlohmann_json::nlohmann_json detector ibverbs ibmad ibnetdisc pthread)