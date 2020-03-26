#include "Status.h"

namespace Observatory {

const char *getStatusString(Status status) {
    switch (status) {
        case OK:
            return "OK";
        case NOT_IMPLEMENTED:
            return "NOT_IMPLEMENTED";
        case SYNC_ERROR:
            return "SYNC_ERROR";
        case NETWORK_ERROR:
            return "NETWORK_ERROR";
        case FILE_ERROR:
            return "FILE_ERROR";
        case UNKNOWN_ERROR:
            return "UNKNOWN_ERROR";
    }

    return "INVALID_STATUS";
}

}