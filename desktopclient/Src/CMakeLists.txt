cmake_minimum_required(VERSION 3.8.2)

project(Trach)

#REST sdk
set(CMAKE_PREFIX_PATH "/usr/lib/x86_64-linux-gnu/cmake")

find_package(cpprestsdk REQUIRED cpprest )

#Qt5
set(CMAKE_PREFIX_PATH "/opt/Qt5.12.1/5.12.1/gcc_64/lib/cmake")
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTOUIC ON)

find_package(Qt5 REQUIRED COMPONENTS Widgets)

#source files
set(CMAKE_INCLUDE_CURRENT_DIR ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -O2")

add_executable(Trach main.cpp ${QRCS} UI/HealthBarUI.h UI/PlayerInfoUI.h)

target_link_libraries(Trach Qt5::Widgets)

target_link_libraries(Trach cpprestsdk::cpprest)