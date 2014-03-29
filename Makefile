SOURCE_VERSION = 1.7
JFLAGS ?= -g:source,lines,vars -encoding utf8
TOUCH_DIR = .touch


all: compile jar eclipse src

# Sources
SRC = core
src: $(SRC)
core::               jcip.annotations slf4j
alert::              core
stream::             core slf4j
messenger::          core slf4j
messenger.stream::   messenger stream slf4j
parsing.properties:: core slf4j
parsing.xml::        core
parsing.yaml::       core
gui::
gui.swing::          gui

# COTS
COTS = jcip.annotations slf4j slf4j.simple slf4j.jdk14
cots: $(COTS)
jcip.annotations::
slf4j::
slf4j.simple::     slf4j
slf4j.jdk14::      slf4j

clean:
	$(RM) -rf $(BUILD_DIR) $(DIST_DIR) $(GENERATED_DIR) $(TOUCH_DIR)

SRC_DIRS = src/
MODULES = $(SRC) $(COTS)
include Java-make/java.mk

