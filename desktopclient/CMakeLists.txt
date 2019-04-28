cmake_minimum_required(VERSION 3.8.2)

project(Trach)

#REST sdk
set(CMAKE_PREFIX_PATH "/usr/lib/x86_64-linux-gnu/cmake")

find_package(cpprestsdk REQUIRED cpprest )

#Qt5
set(CMAKE_PREFIX_PATH "/opt/Qt5.12.1/5.12.1/gcc_64/lib/cmake")
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTORCC ON)

find_package(Qt5 REQUIRED COMPONENTS Widgets)

#source files
set(CMAKE_INCLUDE_CURRENT_DIR ON)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall")
set(QRCS resources.qrc Src/UI/MainTableUI.h)

add_executable(Trach ${QRCS} Src/main.cpp Src/UI/HealthBarUI.h Src/UI/PlayerInfoUI.h Src/Core/Card.h Src/Core/Player.h Src/Core/GameState.h Src/Core/CardNode.h Src/Core/PlayedStartingCard.h Src/Core/PlayedCardInTree.h Src/ServerConnection.h Src/Core/Requests/QuickMultiplayerGameRequest.h Src/Core/Requests/GameStateRequest.h Src/Core/Responses/GameStateUpdate.h Src/Core/Responses/GameplayStateUpdate.h Src/UI/InGameUI.h Src/UI/HandUI.h Src/UI/CardUI.h Src/Core/CardTree.h Src/UI/CurrentTreeTableUI.cpp Src/UI/CurrentTreeTableUI.h Src/UI/PlayersUI.h Src/UI/CurrentCardTreeUI.h Src/Core/GUIUpdater.h Src/UI/HandUI.cpp Src/Core/CardTreeInternalNode.h Src/UI/PlayerInfoUI.cpp)

qt5_add_resources(Trach )
target_link_libraries(Trach Qt5::Widgets)

target_link_libraries(Trach cpprestsdk::cpprest)