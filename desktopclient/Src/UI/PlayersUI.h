
#ifndef TRACH_PLAYERSUI_H
#define TRACH_PLAYERSUI_H

#include <vector>
#include <QtWidgets/QWidget>
#include <Src/Core/Player.h>
#include <QtWidgets/QVBoxLayout>
#include "PlayerInfoUI.h"

using namespace std;

class PlayersUI : public QWidget
{
Q_OBJECT
public:
    vector<PlayerInfoUI*>playersInfo;
    QVBoxLayout *layout = new QVBoxLayout();
    QWidget* emptySpace = nullptr;
    ServerConnection* connection;

    PlayersUI(QWidget* parent, ServerConnection* connection) : QWidget(parent)
    {
        this->connection = connection;
    }

    void setData(vector<Player>players)
    {
        for (int i = 0; i < (int)playersInfo.size();i++)
        {
            layout->removeWidget(playersInfo[i]);
            delete playersInfo[i];
        }
        if (emptySpace != nullptr)
        {
            layout->removeWidget(emptySpace);
            delete emptySpace;
        }

        playersInfo.clear();

        for (int i=0;i<(int)players.size();i++)
        {
            playersInfo.emplace_back(new PlayerInfoUI(this, players[i], connection));
            playersInfo[i]->move(0, i * 100);
            layout->addWidget(playersInfo[i], 1);
        }
        emptySpace = new QWidget(this);
        layout->addWidget(emptySpace, 100);

    }
};

#endif //TRACH_PLAYERSUI_H
