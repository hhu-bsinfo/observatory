#include <sstream>
#include <iomanip>
#include "LoggingLayout.h"

std::string LoggingLayout::format(const log4cpp::LoggingEvent &event) {
    std::ostringstream message;

    time_t seconds = event.timeStamp.getSeconds();
    tm *time = localtime(&seconds);

    message << getAnsiColorCode(event.priority)
            << "[" << std::put_time(time, "%H:%M:%S") << "." << event.timeStamp.getMilliSeconds() << "]"
            << "[" << event.threadName << "]"
            << "[" << getPriorityName(event.priority) << "]"
            << "[" << event.categoryName << "]"
            << " " << event.message << "\033[0m\n";

    return message.str();
}

std::string LoggingLayout::getAnsiColorCode(log4cpp::Priority::Value priority) {
    const auto fullName = log4cpp::Priority::getPriorityName(priority);

    if(fullName == "ERROR") {
        return "\033[31m";
    } else if(fullName == "WARN") {
        return "\033[33m";
    } else if(fullName == "INFO") {
        return "\033[34m";
    } else if(fullName == "DEBUG") {
        return "\033[32m";
    } else if(fullName == "ALERT") {
        return "\033[33m";
    } else if(fullName == "CRIT") {
        return "\033[31m";
    } else if(fullName == "FATAL") {
        return "\033[31m";
    }

    return "\033[0m";
}

std::string LoggingLayout::getPriorityName(log4cpp::Priority::Value priority) {
    const auto fullName = log4cpp::Priority::getPriorityName(priority);

    if(fullName == "ERROR") {
        return "ERR";
    } else if(fullName == "WARN") {
        return "WRN";
    } else if(fullName == "INFO") {
        return "INF";
    } else if(fullName == "DEBUG") {
        return "DBG";
    } else if(fullName == "ALERT") {
        return "ALT";
    } else if(fullName == "CRIT") {
        return "CRT";
    } else if(fullName == "FATAL") {
        return "FTL";
    }

    return fullName;
}