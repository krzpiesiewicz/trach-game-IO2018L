cmake_minimum_required(VERSION 3.8.2)

project(Trach)

# Find includes in the build directories
set(CMAKE_INCLUDE_CURRENT_DIR ON)


set(CMAKE_PREFIX_PATH "/opt/Qt5.12.1/5.12.1/gcc_64/lib/cmake")
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTOUIC ON)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -O2")

find_package(Qt5 REQUIRED COMPONENTS Widgets)

qt5_add_resources(QRCS resources.qrc)

add_executable(Trach main.cpp ${QRCS} UI/HealthBarUI.h UI/PlayerInfoUI.h)

target_link_libraries(Trach Qt5::Widgets)
