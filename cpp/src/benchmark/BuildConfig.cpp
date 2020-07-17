#include <cstdio>
#include "BuildConfig.h"

#define STRINGIFY(a) #a
#define XSTRINGIFY(a) STRINGIFY(a)

namespace Observatory {

const constexpr char BuildConfig::banner[];

#ifdef OBSERVATORY_VERSION
const char *BuildConfig::VERSION = XSTRINGIFY(OBSERVATORY_VERSION);
#else
const char *BuildConfig::VERSION = "v0.0.0";
#endif

#ifdef OBSERVATORY_GIT_REV
const char *BuildConfig::GIT_REV = XSTRINGIFY(OBSERVATORY_GIT_REV);
#else
const char *BuildConfig::GIT_REV = "unknown";
#endif

#ifdef OBSERVATORY_GIT_BRANCH
const char *BuildConfig::GIT_BRANCH = XSTRINGIFY(OBSERVATORY_GIT_BRANCH);
#else
const char *BuildConfig::GIT_BRANCH = "unknown";
#endif

#ifdef OBSERVATORY_BUILD_DATE
const char *BuildConfig::BUILD_DATE = XSTRINGIFY(OBSERVATORY_BUILD_DATE);
#else
const char *BuildConfig::BUILD_DATE = "0000-00-00 00:00:00";
#endif

#ifdef OBSERVATORY_BENCHMARK_CLASS_NAME
const char *BuildConfig::BENCHMARK_CLASS_NAME = XSTRINGIFY(OBSERVATORY_BENCHMARK_CLASS_NAME);
#else
#error OBSERVATORY_BENCHMARK_CLASS_NAME is not set!
#endif

#ifdef OBSERVATORY_BENCHMARK_HEADER_FILE
const char *BuildConfig::BENCHMARK_HEADER_FILE = XSTRINGIFY(OBSERVATORY_BENCHMARK_HEADER_FILE);
#else
#error OBSERVATORY_BENCHMARK_HEADER_FILE is not set!
#endif

void BuildConfig::printBanner() {
    printf("\n");
    printf(banner, VERSION, BUILD_DATE, GIT_BRANCH, GIT_REV, BENCHMARK_CLASS_NAME, BENCHMARK_HEADER_FILE);
    printf("\n\n");
}

}

#undef STRINGIFY
#undef XSTRINGIFY