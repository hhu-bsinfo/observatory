#include <cstdio>
#include "BuildConfig.h"

#define XSTRINGIFY(a) STRINGIFY(a)
#define STRINGIFY(a) #a

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

void BuildConfig::printBanner() {
    printf("\n");
    printf(banner, VERSION, BUILD_DATE, GIT_BRANCH, GIT_REV);
    printf("\n\n");
}

}

#undef STRINGIFY
#undef XSTRINGIFY