//#include <QtWidgets>
//#include <UI/PlayerInfoUI.h>
#include <cpprest/ws_client.h>
#include <cpprest/producerconsumerstream.h>
#include <cpprest/json.h>
#include "cpprest/containerstream.h"
#include "cpprest/filestream.h"
#include "ServerConnection.h"
#include <iostream>
#include <sstream>
#include <Src/Core/Requests/GameStateRequest.h>
#include <QtWidgets/QApplication>
#include <Src/UI/PlayerInfoUI.h>
#include <QtWidgets/QWidget>
#include <Src/UI/InGameUI.h>
#include <Src/UI/GUIUpdater.h>
#include <thread>
#include <dirent.h>

using namespace web::websockets::client;
using namespace web::json;

/** waits for new game state and when received, updates whole GUI
  */
void updateGUI(GUIUpdater *updater, ServerConnection* connection, InGameUI *mainUI)
{
    auto gameStateResponse = connection->getCurrentState();
    auto gameState = gameStateResponse->gameState;
    mainUI->isUpdating = true;
    updater->sendUpdate(gameState.get());
    while (mainUI->isUpdating)
    {
        usleep(1000000);
    }
}

/** established connection with server, starts application
  */
int main(int argc, char *argv[])
{
    std::fstream config;
    config.open( "config.txt", std::ios::in);
    std::string address;
    config >> address;
    config >> address;

    ServerConnection* connection = new ServerConnection;
    connection->connect(address);
    connection->startGame();

    auto gameStateResponse = connection->getCurrentState();

    int playerId = connection->getMainPlayerId();
    auto &gameState = gameStateResponse->gameState;

    QApplication app(argc, argv);
    QWidget window;
    window.resize(900, 480);
    window.setWindowTitle("Trach v0.1");

    auto *mainUI = new InGameUI(&window, gameState.get(), playerId, connection);

    auto *updater = new GUIUpdater();
    QObject::connect(updater, SIGNAL(sendUpdate(GameState * )), mainUI, SLOT(setData(GameState * )));
    auto loopTask = pplx::task<void>([&]()
                                 {
                                     while (true)
                                     {
                                         updateGUI(updater, connection, mainUI);
                                     }

                                 });

    window.show();
    return app.exec();

}