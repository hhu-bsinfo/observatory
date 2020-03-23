#include "Observatory.h"

namespace Observatory {

Observatory::Observatory(nlohmann::json &config, std::string &resultPath, bool isServer, int connectionRetries,
                         std::string &bindAddress, std::string &remoteAddress) :
        config(config),
        resultPath(resultPath),
        isServer(isServer),
        connectionRetries(connectionRetries),
        bindAddress(bindAddress),
        remoteAddress(remoteAddress) {}

void Observatory::start() {
    for(const auto &operationConfig : config["operations"]) {
        for(const auto &mode : operationConfig["modes"]) {
            std::string operationClassName = std::string(operationConfig["name"]).append("Operation");

            for(const auto &iterationConfig : operationConfig["iterations"]) {
                for(int i = 0; i < operationConfig["repetitions"]; i++) {
                    // TODO: Instantiate benchmark
                }
            }
        }
    }
}

}